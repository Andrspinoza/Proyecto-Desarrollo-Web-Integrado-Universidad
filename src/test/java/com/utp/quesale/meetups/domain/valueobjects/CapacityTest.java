package com.utp.quesale.meetups.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CapacityTest {

    @Test
    @DisplayName("Acepta el cupo mínimo y máximo")
    void acceptsMinimumAndMaximumCapacity() {
        assertEquals(2, new Capacity(2).value());
        assertEquals(50, new Capacity(50).value());
    }

    @Test
    @DisplayName("Rechaza cupos fuera del rango permitido")
    void rejectsCapacityOutsideAllowedRange() {
        assertThrows(IllegalArgumentException.class, () -> new Capacity(1));
        assertThrows(IllegalArgumentException.class, () -> new Capacity(51));
    }
}
