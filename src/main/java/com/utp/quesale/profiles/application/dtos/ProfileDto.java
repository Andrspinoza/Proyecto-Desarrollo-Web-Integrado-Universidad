package com.utp.quesale.profiles.application.dtos;

import com.utp.quesale.profiles.domain.valueobjects.Gender;
import com.utp.quesale.profiles.domain.valueobjects.Visibility;

import java.time.LocalDate;
import java.util.Set;

public record ProfileDto(String career, int cycle, String description, Visibility visibility,
                         Set<Long> interestIds, Set<Long> skillIds,
                         Gender gender, Integer age, String campus, LocalDate birthDate) {
    public ProfileDto {
        interestIds = Set.copyOf(interestIds);
        skillIds = Set.copyOf(skillIds);
    }

    public ProfileDto(String career, int cycle, String description, Visibility visibility,
                      Set<Long> interestIds, Set<Long> skillIds) {
        this(career, cycle, description, visibility, interestIds, skillIds, null, null, null, null);
    }
}
