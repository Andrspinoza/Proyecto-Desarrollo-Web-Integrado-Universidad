package com.utp.quesale.profiles.application.dtos;

import com.utp.quesale.profiles.domain.valueobjects.Visibility;

import java.util.Set;

public record ProfileDto(String career, int cycle, String description, Visibility visibility,
                         Set<Long> interestIds, Set<Long> skillIds) {
    public ProfileDto {
        interestIds = Set.copyOf(interestIds);
        skillIds = Set.copyOf(skillIds);
    }
}
