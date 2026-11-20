package com.utp.quesale.profiles.domain.services;

import com.utp.quesale.profiles.domain.entities.Profile;
import com.utp.quesale.profiles.domain.valueobjects.AffinityScore;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public final class AffinityCalculator {
    static final int POINTS_PER_SHARED_INTEREST = 8;
    static final int MAX_SHARED_INTERESTS_COUNTED = 5;
    static final int POINTS_PER_SHARED_SKILL = 2;
    static final int MAX_SHARED_SKILLS_COUNTED = 5;
    static final int POINTS_SAME_CAREER = 25;
    static final int POINTS_SAME_CAMPUS = 10;

    private AffinityCalculator() {
    }

    public static AffinityScore calculate(Profile viewer, Profile other) {
        if (viewer == null || other == null) throw new IllegalArgumentException("Both profiles are required");
        Set<Long> sharedInterests = intersection(viewer.getInterestIds(), other.getInterestIds());
        Set<Long> sharedSkills = intersection(viewer.getSkillIds(), other.getSkillIds());
        boolean sameCareer = same(viewer.getCareer().value(), other.getCareer().value());
        boolean sameCampus = same(viewer.getCampus(), other.getCampus());
        int cycleDifference = Math.abs(viewer.getCycle().value() - other.getCycle().value());

        int points = Math.min(sharedInterests.size(), MAX_SHARED_INTERESTS_COUNTED) * POINTS_PER_SHARED_INTEREST
                + Math.min(sharedSkills.size(), MAX_SHARED_SKILLS_COUNTED) * POINTS_PER_SHARED_SKILL
                + (sameCareer ? POINTS_SAME_CAREER : 0)
                + (sameCampus ? POINTS_SAME_CAMPUS : 0)
                + cyclePoints(cycleDifference);
        return new AffinityScore(Math.min(points, 100), sharedInterests, sharedSkills,
                sameCareer, sameCampus, cycleDifference);
    }

    private static int cyclePoints(int difference) {
        return switch (difference) {
            case 0 -> 15;
            case 1 -> 10;
            case 2 -> 5;
            default -> 0;
        };
    }

    private static boolean same(String a, String b) {
        if (a == null || b == null) return false;
        return a.strip().toLowerCase(Locale.ROOT).equals(b.strip().toLowerCase(Locale.ROOT));
    }

    private static Set<Long> intersection(Set<Long> a, Set<Long> b) {
        Set<Long> result = new LinkedHashSet<>(a);
        result.retainAll(b);
        return result;
    }
}
