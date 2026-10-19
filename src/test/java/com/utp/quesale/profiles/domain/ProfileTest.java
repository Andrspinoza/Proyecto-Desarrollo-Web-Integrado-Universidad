package com.utp.quesale.profiles.domain;

import com.utp.quesale.profiles.domain.entities.Profile;
import com.utp.quesale.profiles.domain.valueobjects.Career;
import com.utp.quesale.profiles.domain.valueobjects.StudyCycle;
import com.utp.quesale.profiles.domain.valueobjects.Visibility;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;

class ProfileTest {
    private final Career career = new Career("Ingenieria de Sistemas");
    private final StudyCycle cycle = new StudyCycle(6);

    private Profile profile() {
        return new Profile(career, cycle, "Estudiante", Visibility.PUBLIC);
    }

    @Test
    void createsProfileWithEmptyCatalogs() {
        Profile profile = profile();
        assertEquals(career, profile.getCareer());
        assertEquals(cycle, profile.getCycle());
        assertEquals("Estudiante", profile.getDescription());
        assertEquals(Visibility.PUBLIC, profile.getVisibility());
        assertTrue(profile.getInterestIds().isEmpty());
        assertTrue(profile.getSkillIds().isEmpty());
    }

    @Test
    void acceptsOptionalDescriptionAndFiveHundredCharacters() {
        assertNull(new Profile(career, cycle, null, Visibility.PRIVATE).getDescription());
        assertEquals("", new Profile(career, cycle, "", Visibility.PUBLIC).getDescription());
        assertEquals(500, new Profile(career, cycle, "a".repeat(500), Visibility.PUBLIC).getDescription().length());
        assertThrows(IllegalArgumentException.class,
                () -> new Profile(career, cycle, "a".repeat(501), Visibility.PUBLIC));
    }

    @Test
    void rejectsMissingRequiredFields() {
        assertThrows(IllegalArgumentException.class, () -> new Profile(null, cycle, "", Visibility.PUBLIC));
        assertThrows(IllegalArgumentException.class, () -> new Profile(career, null, "", Visibility.PUBLIC));
        assertThrows(IllegalArgumentException.class, () -> new Profile(career, cycle, "", null));
    }

    @Test
    void updatesDataWithoutRemovingCatalogsAndKeepsStateOnInvalidUpdate() {
        Profile profile = profile();
        profile.addInterest(1L);
        profile.addSkill(2L);
        Career changed = new Career("Arquitectura");
        profile.update(changed, new StudyCycle(7), "Nueva descripcion", Visibility.PRIVATE);

        assertEquals(changed, profile.getCareer());
        assertEquals(new StudyCycle(7), profile.getCycle());
        assertEquals("Nueva descripcion", profile.getDescription());
        assertEquals(Visibility.PRIVATE, profile.getVisibility());
        assertEquals(1, profile.getInterestIds().size());
        assertEquals(1, profile.getSkillIds().size());
        assertThrows(IllegalArgumentException.class,
                () -> profile.update(career, cycle, "a".repeat(501), Visibility.PUBLIC));
        assertEquals(changed, profile.getCareer());
        assertEquals(Visibility.PRIVATE, profile.getVisibility());
    }

    @Test
    void enforcesSeparateLimitsAndAllowsAddingAfterRemoval() {
        Profile profile = profile();
        for (long id = 1; id <= 15; id++) {
            profile.addInterest(id);
            profile.addSkill(id);
        }
        assertThrows(IllegalStateException.class, () -> profile.addInterest(16L));
        assertThrows(IllegalStateException.class, () -> profile.addSkill(16L));
        assertEquals(15, profile.getInterestIds().size());
        assertEquals(15, profile.getSkillIds().size());
        profile.removeInterest(1L);
        profile.removeSkill(1L);
        profile.addInterest(16L);
        profile.addSkill(16L);
        assertTrue(profile.getInterestIds().contains(16L));
        assertTrue(profile.getSkillIds().contains(16L));
    }

    @Test
    void rejectsDuplicatesAndInvalidCatalogIds() {
        Profile profile = profile();
        profile.addInterest(1L);
        profile.addSkill(1L);
        assertThrows(IllegalStateException.class, () -> profile.addInterest(1L));
        assertThrows(IllegalStateException.class, () -> profile.addSkill(1L));
        for (Long id : Arrays.asList(null, 0L, -1L)) {
            assertThrows(IllegalArgumentException.class, () -> profile.addInterest(id));
            assertThrows(IllegalArgumentException.class, () -> profile.addSkill(id));
            assertThrows(IllegalArgumentException.class, () -> profile.removeInterest(id));
            assertThrows(IllegalArgumentException.class, () -> profile.removeSkill(id));
        }
    }

    @Test
    void copiesIncomingCollectionsAndExposesReadOnlySets() {
        List<Long> ids = new ArrayList<>(List.of(1L));
        Profile profile = new Profile(career, cycle, "", Visibility.PUBLIC, ids, ids);
        ids.add(2L);
        assertEquals(1, profile.getInterestIds().size());
        assertEquals(1, profile.getSkillIds().size());
        assertThrows(UnsupportedOperationException.class, () -> profile.getInterestIds().add(3L));
        assertThrows(UnsupportedOperationException.class, () -> profile.getSkillIds().clear());
    }

    @Test
    void rejectsInvalidCollectionsWhenRehydrating() {
        List<Long> tooMany = LongStream.rangeClosed(1, 16).boxed().toList();
        List<List<Long>> invalid = Arrays.asList(null, List.of(1L, 1L), tooMany, Arrays.asList((Long) null), List.of(0L));
        for (List<Long> ids : invalid) {
            assertThrows(IllegalArgumentException.class,
                    () -> new Profile(career, cycle, "", Visibility.PUBLIC, ids, List.of()));
            assertThrows(IllegalArgumentException.class,
                    () -> new Profile(career, cycle, "", Visibility.PUBLIC, List.of(), ids));
        }
    }
}
