package com.utp.quesale.notifications.domain;

import com.utp.quesale.notifications.domain.valueobjects.MatchNotification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class MatchNotificationTest {

    private static final Instant NOW = Instant.parse("2026-11-18T12:00:00Z");

    @Test
    @DisplayName("Conserva la conexión, la persona del match y el nombre sin espacios sobrantes")
    void keepsData() {
        MatchNotification notification = new MatchNotification(4L, 2L, "  Diego ", NOW);

        assertEquals(4L, notification.connectionId());
        assertEquals(2L, notification.matchedStudentId());
        assertEquals("Diego", notification.matchedFirstName());
        assertEquals(NOW, notification.matchedAt());
    }

    @Test
    @DisplayName("Sin nombre queda vacío en vez de nulo")
    void missingNameBecomesEmpty() {
        assertEquals("", new MatchNotification(4L, 2L, null, NOW).matchedFirstName());
    }

    @Test
    @DisplayName("Los ids y la fecha son obligatorios")
    void requiredFields() {
        assertThrows(IllegalArgumentException.class, () -> new MatchNotification(null, 2L, "Diego", NOW));
        assertThrows(IllegalArgumentException.class, () -> new MatchNotification(0L, 2L, "Diego", NOW));
        assertThrows(IllegalArgumentException.class, () -> new MatchNotification(4L, null, "Diego", NOW));
        assertThrows(IllegalArgumentException.class, () -> new MatchNotification(4L, 2L, "Diego", null));
    }
}
