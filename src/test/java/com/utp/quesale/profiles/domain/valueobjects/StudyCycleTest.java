package com.utp.quesale.profiles.domain.valueobjects;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StudyCycleTest {
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
    void acceptsCyclesFromOneToTen(int value) {
        assertEquals(value, new StudyCycle(value).value());
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -1, 0, 11, Integer.MAX_VALUE})
    void rejectsCyclesOutsideAllowedRange(int value) {
        assertThrows(IllegalArgumentException.class, () -> new StudyCycle(value));
    }
}
