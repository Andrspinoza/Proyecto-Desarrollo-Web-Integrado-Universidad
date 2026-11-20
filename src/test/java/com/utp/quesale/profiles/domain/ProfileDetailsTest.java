package com.utp.quesale.profiles.domain;

import com.utp.quesale.profiles.domain.entities.Profile;
import com.utp.quesale.profiles.domain.valueobjects.Career;
import com.utp.quesale.profiles.domain.valueobjects.Gender;
import com.utp.quesale.profiles.domain.valueobjects.StudyCycle;
import com.utp.quesale.profiles.domain.valueobjects.Visibility;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProfileDetailsTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 20);

    private Profile profile() {
        return new Profile(new Career("Ingenieria de Sistemas"), new StudyCycle(7), "Hola", Visibility.PUBLIC);
    }

    @Test void aNewProfileStartsWithUndisclosedGenderAndNoBirthDateOrCampus() {
        var profile = profile();
        assertEquals(Gender.UNDISCLOSED, profile.getGender());
        assertNull(profile.getBirthDate());
        assertNull(profile.getCampus());
        assertNull(profile.ageOn(TODAY));
    }

    @Test void detailsAreStoredAndTheAgeIsComputedFromTheBirthDate() {
        var profile = profile();
        profile.updateDetails(Gender.FEMALE, LocalDate.of(2003, 11, 20), "  Lima Centro ", TODAY);
        assertEquals(Gender.FEMALE, profile.getGender());
        assertEquals("Lima Centro", profile.getCampus());
        assertEquals(23, profile.ageOn(TODAY));
        assertEquals(22, profile.ageOn(LocalDate.of(2026, 11, 19)));
    }

    @Test void aNullGenderBecomesUndisclosedAndABlankCampusBecomesNull() {
        var profile = profile();
        profile.updateDetails(null, null, "   ", TODAY);
        assertEquals(Gender.UNDISCLOSED, profile.getGender());
        assertNull(profile.getCampus());
    }

    @Test void birthDatesInTheFutureOrOutsideSixteenToOneHundredAreRejected() {
        var profile = profile();
        assertThrows(IllegalArgumentException.class, () -> profile.updateDetails(Gender.MALE, TODAY, null, TODAY));
        assertThrows(IllegalArgumentException.class, () -> profile.updateDetails(Gender.MALE, TODAY.plusDays(1), null, TODAY));
        assertThrows(IllegalArgumentException.class, () -> profile.updateDetails(Gender.MALE, TODAY.minusYears(15), null, TODAY));
        assertThrows(IllegalArgumentException.class, () -> profile.updateDetails(Gender.MALE, TODAY.minusYears(101), null, TODAY));
        assertDoesNotThrow(() -> profile.updateDetails(Gender.MALE, TODAY.minusYears(16), null, TODAY));
        assertDoesNotThrow(() -> profile.updateDetails(Gender.MALE, TODAY.minusYears(100), null, TODAY));
    }

    @Test void aCampusLongerThanSixtyCharactersIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> profile().updateDetails(Gender.OTHER, null, "x".repeat(61), TODAY));
    }

    @Test void interestsAndSkillsAreReplacedNotAppended() {
        var profile = profile();
        profile.replaceInterests(List.of(1L, 2L));
        profile.replaceInterests(List.of(3L));
        assertEquals(java.util.Set.of(3L), profile.getInterestIds());
        profile.replaceSkills(List.of(5L, 6L));
        profile.replaceSkills(List.of());
        assertTrue(profile.getSkillIds().isEmpty());
    }

    @Test void replacingWithRepeatedOrTooManyIdsFailsAndKeepsThePreviousOnes() {
        var profile = profile();
        profile.replaceInterests(List.of(1L));
        assertThrows(IllegalArgumentException.class, () -> profile.replaceInterests(List.of(2L, 2L)));
        assertThrows(IllegalArgumentException.class, () -> profile.replaceInterests(
                java.util.stream.LongStream.rangeClosed(1, 16).boxed().toList()));
        assertEquals(java.util.Set.of(1L), profile.getInterestIds());
    }
}
