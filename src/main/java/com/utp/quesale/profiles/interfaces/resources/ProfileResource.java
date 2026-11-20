package com.utp.quesale.profiles.interfaces.resources;

import com.utp.quesale.profiles.domain.valueobjects.Gender;
import com.utp.quesale.profiles.domain.valueobjects.Visibility;

import java.time.LocalDate;
import java.util.Set;

public record ProfileResource(String career, int cycle, String description, Visibility visibility,
                              Set<Long> interestIds, Set<Long> skillIds,
                              Gender gender, Integer age, String campus, LocalDate birthDate) {
    public ProfileResource {
        interestIds = Set.copyOf(interestIds);
        skillIds = Set.copyOf(skillIds);
    }

    public ProfileResource(String career, int cycle, String description, Visibility visibility,
                           Set<Long> interestIds, Set<Long> skillIds) {
        this(career, cycle, description, visibility, interestIds, skillIds, null, null, null, null);
    }
}
