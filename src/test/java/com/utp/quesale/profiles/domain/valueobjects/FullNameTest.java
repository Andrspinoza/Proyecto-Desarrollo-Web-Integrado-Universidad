package com.utp.quesale.profiles.domain.valueobjects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FullNameTest {
    @Test
    void trimsNamesAndPreservesCompoundNames() {
        FullName name = new FullName("  Bruno Fernando  ", "  Escobedo  ");

        assertEquals("Bruno Fernando", name.firstName());
        assertEquals("Escobedo", name.lastName());
    }

    @Test
    void acceptsSixtyCharactersInEachPart() {
        String value = "a".repeat(60);

        FullName name = new FullName(value, value);

        assertEquals(value, name.firstName());
        assertEquals(value, name.lastName());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void rejectsMissingFirstName(String value) {
        assertThrows(IllegalArgumentException.class, () -> new FullName(value, "Escobedo"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void rejectsMissingLastName(String value) {
        assertThrows(IllegalArgumentException.class, () -> new FullName("Fernando", value));
    }

    @Test
    void rejectsFirstNameLongerThanSixtyCharacters() {
        assertThrows(IllegalArgumentException.class,
                () -> new FullName("a".repeat(61), "Escobedo"));
    }

    @Test
    void rejectsLastNameLongerThanSixtyCharacters() {
        assertThrows(IllegalArgumentException.class,
                () -> new FullName("Fernando", "a".repeat(61)));
    }
}
