package com.utp.quesale.meetups.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlaceTest {

    @Test
    @DisplayName("Recorta los espacios y acepta los máximos permitidos")
    void trimsValuesAndAcceptsMaximumLengths() {
        Place place = new Place(
                " " + "N".repeat(120) + " ",
                " " + "A".repeat(200) + " ",
                " " + "D".repeat(60) + " ");

        assertEquals("N".repeat(120), place.name());
        assertEquals("A".repeat(200), place.address());
        assertEquals("D".repeat(60), place.district());
    }

    @Test
    @DisplayName("Requiere nombre, dirección y distrito")
    void rejectsMissingComponents() {
        assertThrows(IllegalArgumentException.class, () -> new Place(null, "Calle 1", "Centro"));
        assertThrows(IllegalArgumentException.class, () -> new Place("Campus", " ", "Centro"));
        assertThrows(IllegalArgumentException.class, () -> new Place("Campus", "Calle 1", ""));
    }

    @Test
    @DisplayName("No permite que sus componentes superen sus límites")
    void rejectsComponentsLongerThanTheirMaximum() {
        assertThrows(IllegalArgumentException.class,
                () -> new Place("N".repeat(121), "Calle 1", "Centro"));
        assertThrows(IllegalArgumentException.class,
                () -> new Place("Campus", "A".repeat(201), "Centro"));
        assertThrows(IllegalArgumentException.class,
                () -> new Place("Campus", "Calle 1", "D".repeat(61)));
    }
}
