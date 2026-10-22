package com.utp.quesale.profiles.application.assemblers;

import com.utp.quesale.profiles.application.dtos.InterestDto;
import com.utp.quesale.profiles.application.dtos.SkillDto;
import com.utp.quesale.profiles.application.dtos.ProfileDto;
import com.utp.quesale.profiles.application.dtos.StudentDto;
import com.utp.quesale.profiles.domain.aggregates.Student;
import com.utp.quesale.profiles.domain.entities.Profile;
import com.utp.quesale.profiles.domain.aggregates.Interest;
import com.utp.quesale.profiles.domain.aggregates.Skill;

import java.util.Set;

public final class ProfilesDtoAssembler {
    private ProfilesDtoAssembler() {
    }

    public static InterestDto toDto(Interest interest) {
        return new InterestDto(interest.getId(), interest.getName());
    }

    public static SkillDto toDto(Skill skill) {
        return new SkillDto(skill.getId(), skill.getName());
    }

    public static StudentDto toDto(Student student) {
        return new StudentDto(student.getId(), student.getRecordId().studentRecordId(),
                student.getUniversityId(), student.getEmail(), student.getFullName().firstName(),
                student.getFullName().lastName(), student.getCreatedAt(), toDto(student.getProfile()));
    }

    public static StudentDto toReducedDto(Student student) {
        Profile profile = student.getProfile();
        ProfileDto reducedProfile = profile == null ? null
                : new ProfileDto(profile.getCareer().value(), profile.getCycle().value(),
                        null, null, Set.of(), Set.of());
        return new StudentDto(student.getId(), null, null, null,
                student.getFullName().firstName(), student.getFullName().lastName(), null, reducedProfile);
    }

    private static ProfileDto toDto(Profile profile) {
        if (profile == null) return null;
        return new ProfileDto(profile.getCareer().value(), profile.getCycle().value(),
                profile.getDescription(), profile.getVisibility(), profile.getInterestIds(), profile.getSkillIds());
    }
}
