package com.utp.quesale.profiles.interfaces.resources;

import com.utp.quesale.profiles.domain.valueobjects.Visibility;

import java.util.Set;

public record ProfileResource(String career, int cycle, String description, Visibility visibility,
                              Set<Long> interestIds, Set<Long> skillIds) {
    public ProfileResource {
        interestIds = Set.copyOf(interestIds);
        skillIds = Set.copyOf(skillIds);
    }
}
