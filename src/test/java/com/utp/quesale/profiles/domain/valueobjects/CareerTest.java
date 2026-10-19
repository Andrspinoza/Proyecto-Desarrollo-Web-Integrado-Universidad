package com.utp.quesale.profiles.domain.valueobjects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CareerTest {
    @Test
    void trimsCareerName() {
        assertEquals("Ingenieria de Sistemas", new Career("  Ingenieria de Sistemas  ").value());
    }

    @Test
    void acceptsEightyCharactersAfterTrimming() {
        String value = "a".repeat(80);

        assertEquals(value, new Career(" " + value + " ").value());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void rejectsMissingCareer(String value) {
        assertThrows(IllegalArgumentException.class, () -> new Career(value));
    }

    @Test
    void rejectsMoreThanEightyCharacters() {
        assertThrows(IllegalArgumentException.class, () -> new Career("a".repeat(81)));
    }
}
