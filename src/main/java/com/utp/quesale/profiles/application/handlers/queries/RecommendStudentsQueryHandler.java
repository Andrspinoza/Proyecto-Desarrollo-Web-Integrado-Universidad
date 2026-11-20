package com.utp.quesale.profiles.application.handlers.queries;

import com.utp.quesale.connections.interfaces.acl.ConnectionsContextFacade;
import com.utp.quesale.iam.interfaces.acl.IamContextFacade;
import com.utp.quesale.moderation.interfaces.acl.ModerationContextFacade;
import com.utp.quesale.profiles.application.dtos.RecommendationDto;
import com.utp.quesale.profiles.application.exceptions.StudentNotFoundException;
import com.utp.quesale.profiles.domain.aggregates.Interest;
import com.utp.quesale.profiles.domain.aggregates.Skill;
import com.utp.quesale.profiles.domain.aggregates.Student;
import com.utp.quesale.profiles.domain.entities.Profile;
import com.utp.quesale.profiles.domain.repositories.InterestRepository;
import com.utp.quesale.profiles.domain.repositories.SkillRepository;
import com.utp.quesale.profiles.domain.repositories.StudentRepository;
import com.utp.quesale.profiles.domain.services.AffinityCalculator;
import com.utp.quesale.profiles.domain.valueobjects.AffinityScore;
import com.utp.quesale.profiles.domain.valueobjects.Visibility;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RecommendStudentsQueryHandler {
    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final int MAX_LIMIT = 50;
    private static final int MAX_INTERESTS_SHOWN = 8;

    private final StudentRepository students;
    private final InterestRepository interests;
    private final SkillRepository skills;
    private final ModerationContextFacade moderation;
    private final IamContextFacade iam;
    private final ConnectionsContextFacade connections;
    private final Clock clock;

    public RecommendStudentsQueryHandler(StudentRepository students, InterestRepository interests, SkillRepository skills,
                                         ModerationContextFacade moderation, IamContextFacade iam,
                                         ConnectionsContextFacade connections, Clock clock) {
        this.students = students;
        this.interests = interests;
        this.skills = skills;
        this.moderation = moderation;
        this.iam = iam;
        this.connections = connections;
        this.clock = clock;
    }

    public List<RecommendationDto> handle(Long viewerId, int limit) {
        if (viewerId == null || viewerId <= 0) throw new IllegalArgumentException("Student id must be positive");
        Student viewer = students.findById(viewerId).orElseThrow(() -> new StudentNotFoundException(viewerId));
        Profile mine = viewer.getProfile();
        if (mine == null) return List.of();
        int size = Math.max(1, Math.min(limit, MAX_LIMIT));
        Set<Long> related = connections.relatedStudentIds(viewerId);
        Map<Long, String> interestNames = interests.findAll().stream()
                .collect(Collectors.toMap(Interest::getId, Interest::getName));
        Map<Long, String> skillNames = skills.findAll().stream()
                .collect(Collectors.toMap(Skill::getId, Skill::getName));
        LocalDate today = LocalDate.ofInstant(clock.instant(), LIMA);

        return students.findAll().stream()
                .filter(candidate -> !candidate.getId().equals(viewerId))
                .filter(candidate -> candidate.getUniversityId().equals(viewer.getUniversityId()))
                .filter(candidate -> candidate.getProfile() != null
                        && candidate.getProfile().getVisibility() == Visibility.PUBLIC)
                .filter(candidate -> !related.contains(candidate.getId()))
                .filter(candidate -> iam.isEmailVerified(candidate.getId()))
                .filter(candidate -> !moderation.isBlocked(viewerId, candidate.getId()))
                .map(candidate -> new Scored(candidate, AffinityCalculator.calculate(mine, candidate.getProfile())))
                .sorted(Comparator.comparingInt((Scored item) -> item.score().percent()).reversed()
                        .thenComparing(item -> item.student().getId()))
                .limit(size)
                .map(item -> toDto(item, interestNames, skillNames, today))
                .toList();
    }

    private static RecommendationDto toDto(Scored item, Map<Long, String> interestNames,
                                           Map<Long, String> skillNames, LocalDate today) {
        Student student = item.student();
        Profile profile = student.getProfile();
        AffinityScore score = item.score();
        return new RecommendationDto(student.getId(), student.getFullName().firstName(), student.getFullName().lastName(),
                profile.ageOn(today), profile.getGender(), profile.getCareer().value(), profile.getCycle().value(),
                profile.getCampus(), profile.getDescription(), score.percent(),
                names(score.sharedInterestIds(), interestNames), names(score.sharedSkillIds(), skillNames),
                score.sameCareer(), score.sameCampus(), score.cycleDifference(),
                names(profile.getInterestIds(), interestNames).stream().limit(MAX_INTERESTS_SHOWN).toList());
    }

    private static List<String> names(Set<Long> ids, Map<Long, String> catalog) {
        return ids.stream().map(catalog::get).filter(java.util.Objects::nonNull).sorted().toList();
    }

    private record Scored(Student student, AffinityScore score) {
    }
}
