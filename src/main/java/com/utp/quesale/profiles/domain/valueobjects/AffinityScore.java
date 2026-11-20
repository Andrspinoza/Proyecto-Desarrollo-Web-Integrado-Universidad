package com.utp.quesale.profiles.domain.valueobjects;

import java.util.Set;

public record AffinityScore(int percent, Set<Long> sharedInterestIds, Set<Long> sharedSkillIds,
                            boolean sameCareer, boolean sameCampus, int cycleDifference) {
    public AffinityScore {
        if (percent < 0 || percent > 100) throw new IllegalArgumentException("Affinity must be between 0 and 100");
        sharedInterestIds = Set.copyOf(sharedInterestIds);
        sharedSkillIds = Set.copyOf(sharedSkillIds);
    }
}
