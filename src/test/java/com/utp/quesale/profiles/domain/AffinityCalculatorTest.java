package com.utp.quesale.profiles.domain;

import com.utp.quesale.profiles.domain.entities.Profile;
import com.utp.quesale.profiles.domain.services.AffinityCalculator;
import com.utp.quesale.profiles.domain.valueobjects.Career;
import com.utp.quesale.profiles.domain.valueobjects.Gender;
import com.utp.quesale.profiles.domain.valueobjects.StudyCycle;
import com.utp.quesale.profiles.domain.valueobjects.Visibility;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AffinityCalculatorTest {
    private static Profile profile(String career, int cycle, String campus, List<Long> interests, List<Long> skills) {
        Profile profile = new Profile(new Career(career), new StudyCycle(cycle), null, Visibility.PUBLIC, interests, skills);
        profile.restoreDetails(Gender.UNDISCLOSED, null, campus);
        return profile;
    }

    @Test void twoStrangersWithNothingInCommonScoreZero() {
        var score = AffinityCalculator.calculate(
                profile("Derecho", 1, "Lima Norte", List.of(1L), List.of(1L)),
                profile("Medicina", 9, "Arequipa", List.of(2L), List.of(2L)));
        assertEquals(0, score.percent());
        assertFalse(score.sameCareer());
        assertFalse(score.sameCampus());
        assertEquals(8, score.cycleDifference());
        assertTrue(score.sharedInterestIds().isEmpty());
    }

    @Test void everythingInCommonCapsAtOneHundred() {
        var all = List.of(1L, 2L, 3L, 4L, 5L, 6L);
        var score = AffinityCalculator.calculate(
                profile("Ingenieria de Sistemas", 7, "Lima Centro", all, all),
                profile("Ingenieria de Sistemas", 7, "Lima Centro", all, all));
        assertEquals(100, score.percent());
        assertTrue(score.sameCareer());
        assertTrue(score.sameCampus());
        assertEquals(0, score.cycleDifference());
    }

    @Test void eachSharedInterestIsWorthEightPointsUpToFive() {
        var viewer = profile("A", 1, null, List.of(1L, 2L, 3L), List.of());
        var other = profile("B", 10, null, List.of(1L, 2L, 3L), List.of());
        assertEquals(24, AffinityCalculator.calculate(viewer, other).percent());
        var many = profile("A", 1, null, List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L), List.of());
        var many2 = profile("B", 10, null, List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L), List.of());
        assertEquals(40, AffinityCalculator.calculate(many, many2).percent());
    }

    @Test void theCycleDistanceGivesFifteenTenFiveOrNothing() {
        int[] expected = {15, 10, 5, 0, 0};
        for (int difference = 0; difference < expected.length; difference++) {
            var score = AffinityCalculator.calculate(
                    profile("A", 5, null, List.of(), List.of()),
                    profile("B", 5 + difference, null, List.of(), List.of()));
            assertEquals(expected[difference], score.percent(), "ciclos de diferencia: " + difference);
        }
    }

    @Test void careerAndCampusComparisonsIgnoreCaseAndSpacesAndNeverMatchOnNull() {
        var score = AffinityCalculator.calculate(
                profile("Ingenieria de Sistemas", 7, " lima centro ", List.of(), List.of()),
                profile("  INGENIERIA DE SISTEMAS", 7, "Lima Centro", List.of(), List.of()));
        assertTrue(score.sameCareer());
        assertTrue(score.sameCampus());
        var nulls = AffinityCalculator.calculate(
                profile("A", 1, null, List.of(), List.of()), profile("B", 1, null, List.of(), List.of()));
        assertFalse(nulls.sameCampus());
    }

    @Test void theSharedIdsAreReportedForTheExplanation() {
        var score = AffinityCalculator.calculate(
                profile("A", 3, null, List.of(1L, 2L, 3L), List.of(7L, 8L)),
                profile("B", 3, null, List.of(2L, 3L, 4L), List.of(8L, 9L)));
        assertEquals(Set.of(2L, 3L), score.sharedInterestIds());
        assertEquals(Set.of(8L), score.sharedSkillIds());
    }

    @Test void missingProfilesAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> AffinityCalculator.calculate(null, profile("A", 1, null, List.of(), List.of())));
    }
}
