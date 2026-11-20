package com.utp.quesale.profiles.application;

import com.utp.quesale.connections.interfaces.acl.ConnectionsContextFacade;
import com.utp.quesale.iam.interfaces.acl.IamContextFacade;
import com.utp.quesale.moderation.interfaces.acl.ModerationContextFacade;
import com.utp.quesale.profiles.application.exceptions.StudentNotFoundException;
import com.utp.quesale.profiles.application.handlers.queries.RecommendStudentsQueryHandler;
import com.utp.quesale.profiles.domain.aggregates.Interest;
import com.utp.quesale.profiles.domain.aggregates.Student;
import com.utp.quesale.profiles.domain.entities.Profile;
import com.utp.quesale.profiles.domain.repositories.InterestRepository;
import com.utp.quesale.profiles.domain.repositories.SkillRepository;
import com.utp.quesale.profiles.domain.repositories.StudentRepository;
import com.utp.quesale.profiles.domain.valueobjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecommendStudentsQueryHandlerTest {
    private final StudentRepository students = mock(StudentRepository.class);
    private final InterestRepository interests = mock(InterestRepository.class);
    private final SkillRepository skills = mock(SkillRepository.class);
    private final Set<Long> blocked = new HashSet<>();
    private final Set<Long> unverified = new HashSet<>();
    private final Set<Long> related = new HashSet<>();
    private final List<Student> all = new ArrayList<>();
    private RecommendStudentsQueryHandler handler;

    private static Student student(long id, long university, String career, int cycle, String campus,
                                   Visibility visibility, List<Long> interestIds) {
        Profile profile = new Profile(new Career(career), new StudyCycle(cycle), "bio " + id, visibility,
                interestIds, List.of());
        profile.restoreDetails(Gender.FEMALE, LocalDate.of(2003, 5, 10), campus);
        return new Student(id, new StudentRecordId(), university, "u" + id + "@utp.edu.pe",
                new FullName("Nombre" + id, "Apellido"), Instant.parse("2026-01-01T00:00:00Z"), profile);
    }

    @BeforeEach
    void setUp() {
        ModerationContextFacade moderation = (viewer, other) -> blocked.contains(other);
        IamContextFacade iam = new IamContextFacade() {
            public boolean isActiveUniversity(Long id) { return true; }
            public boolean universityAcceptsEmail(Long id, String email) { return true; }
            public boolean isEmailVerified(Long id) { return !unverified.contains(id); }
        };
        ConnectionsContextFacade connections = new ConnectionsContextFacade() {
            public boolean areConnected(Long a, Long b) { return false; }
            public Set<Long> relatedStudentIds(Long id) { return related; }
        };
        when(students.findAll()).thenReturn(all);
        when(students.findById(anyLong())).thenAnswer(call -> all.stream()
                .filter(item -> item.getId().equals(call.getArgument(0))).findFirst());
        when(interests.findAll()).thenReturn(List.of(new Interest(1L, "Café"), new Interest(2L, "Vóley"), new Interest(3L, "Anime")));
        when(skills.findAll()).thenReturn(List.of());
        handler = new RecommendStudentsQueryHandler(students, interests, skills, moderation, iam, connections,
                Clock.fixed(Instant.parse("2026-11-20T12:00:00Z"), ZoneOffset.UTC));
    }

    @Test void ordersByAffinityAndExplainsWhyUsingNames() {
        all.add(student(1, 10, "Sistemas", 7, "Lima Centro", Visibility.PUBLIC, List.of(1L, 2L)));
        all.add(student(2, 10, "Derecho", 3, "Arequipa", Visibility.PUBLIC, List.of(3L)));
        all.add(student(3, 10, "Sistemas", 7, "Lima Centro", Visibility.PUBLIC, List.of(1L, 2L)));
        var result = handler.handle(1L, 20);
        assertEquals(List.of(3L, 2L), result.stream().map(item -> item.id()).toList());
        var best = result.get(0);
        assertEquals(66, best.affinityPercent());
        assertEquals(List.of("Café", "Vóley"), best.sharedInterests());
        assertTrue(best.sameCareer());
        assertTrue(best.sameCampus());
        assertEquals(23, best.age());
        assertEquals(0, result.get(1).affinityPercent());
    }

    @Test void excludesMyselfOtherUniversitiesPrivateAndProfilelessCandidates() {
        all.add(student(1, 10, "Sistemas", 7, "Lima", Visibility.PUBLIC, List.of(1L)));
        all.add(student(2, 99, "Sistemas", 7, "Lima", Visibility.PUBLIC, List.of(1L)));
        all.add(student(3, 10, "Sistemas", 7, "Lima", Visibility.PRIVATE, List.of(1L)));
        all.add(new Student(4L, new StudentRecordId(), 10L, "u4@utp.edu.pe", new FullName("Sin", "Perfil"),
                Instant.parse("2026-01-01T00:00:00Z")));
        all.add(student(5, 10, "Sistemas", 7, "Lima", Visibility.PUBLIC, List.of(1L)));
        assertEquals(List.of(5L), handler.handle(1L, 20).stream().map(item -> item.id()).toList());
    }

    @Test void excludesBlockedUnverifiedAndAlreadyRelatedStudents() {
        all.add(student(1, 10, "Sistemas", 7, "Lima", Visibility.PUBLIC, List.of(1L)));
        all.add(student(2, 10, "Sistemas", 7, "Lima", Visibility.PUBLIC, List.of(1L)));
        all.add(student(3, 10, "Sistemas", 7, "Lima", Visibility.PUBLIC, List.of(1L)));
        all.add(student(4, 10, "Sistemas", 7, "Lima", Visibility.PUBLIC, List.of(1L)));
        all.add(student(5, 10, "Sistemas", 7, "Lima", Visibility.PUBLIC, List.of(1L)));
        blocked.add(2L);
        unverified.add(3L);
        related.add(4L);
        assertEquals(List.of(5L), handler.handle(1L, 20).stream().map(item -> item.id()).toList());
    }

    @Test void theLimitIsRespectedAndClampedBetweenOneAndFifty() {
        all.add(student(1, 10, "Sistemas", 7, "Lima", Visibility.PUBLIC, List.of()));
        for (long id = 2; id <= 60; id++) all.add(student(id, 10, "Sistemas", 7, "Lima", Visibility.PUBLIC, List.of()));
        assertEquals(3, handler.handle(1L, 3).size());
        assertEquals(1, handler.handle(1L, 0).size());
        assertEquals(50, handler.handle(1L, 1000).size());
    }

    @Test void aViewerWithoutProfileGetsNoRecommendationsAndAnUnknownViewerFails() {
        all.add(new Student(1L, new StudentRecordId(), 10L, "u1@utp.edu.pe", new FullName("Sin", "Perfil"),
                Instant.parse("2026-01-01T00:00:00Z")));
        all.add(student(2, 10, "Sistemas", 7, "Lima", Visibility.PUBLIC, List.of(1L)));
        assertTrue(handler.handle(1L, 20).isEmpty());
        assertThrows(StudentNotFoundException.class, () -> handler.handle(99L, 20));
        assertThrows(IllegalArgumentException.class, () -> handler.handle(0L, 20));
    }
}
