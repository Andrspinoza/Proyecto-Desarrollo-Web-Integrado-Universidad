package com.utp.quesale.profiles.domain;

import com.utp.quesale.profiles.domain.aggregates.Interest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class InterestTest {
    @Test
    void createsInterestWithTrimmedName() {
        Interest interest = new Interest("  Juegos de mesa  ");

        assertNull(interest.getId());
        assertEquals("Juegos de mesa", interest.getName());
    }

    @Test
    void rehydratesIdentityWithoutEvents() {
        Interest interest = new Interest(7L, "Lectura");

        assertEquals(7L, interest.getId());
        assertEquals("Lectura", interest.getName());
        assertTrue(interest.pullDomainEvents().isEmpty());
    }

    @Test
    void acceptsFortyCharactersAfterTrimming() {
        String name = "a".repeat(40);
        assertEquals(name, new Interest(" " + name + " ").getName());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void rejectsMissingName(String name) {
        assertThrows(IllegalArgumentException.class, () -> new Interest(name));
    }

    @Test
    void rejectsMoreThanFortyCharacters() {
        assertThrows(IllegalArgumentException.class, () -> new Interest("a".repeat(41)));
    }
}
