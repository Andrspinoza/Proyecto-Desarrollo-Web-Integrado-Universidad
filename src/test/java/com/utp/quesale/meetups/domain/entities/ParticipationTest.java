package com.utp.quesale.meetups.domain.entities;

import com.utp.quesale.meetups.domain.enums.ParticipationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParticipationTest {

    @Test
    @DisplayName("Una solicitud nueva queda pendiente y registra su fecha")
    void startsAsRequested() {
        Participation participation = new Participation(42L);

        assertEquals(42L, participation.getStudentId());
        assertEquals(ParticipationStatus.REQUESTED, participation.getStatus());
        assertNotNull(participation.getRegisteredAt());
    }

    @Test
    @DisplayName("Una solicitud pendiente puede confirmarse una sola vez")
    void confirmsRequestedParticipation() {
        Participation participation = new Participation(42L);

        participation.confirm();

        assertEquals(ParticipationStatus.CONFIRMED, participation.getStatus());
        assertThrows(IllegalStateException.class, participation::confirm);
    }

    @Test
    @DisplayName("Una solicitud o confirmación puede retirarse")
    void withdrawsRequestedOrConfirmedParticipation() {
        Participation requested = new Participation(42L);
        Participation confirmed = new Participation(43L, ParticipationStatus.CONFIRMED, Instant.now());

        requested.withdraw();
        confirmed.withdraw();

        assertEquals(ParticipationStatus.WITHDRAWN, requested.getStatus());
        assertEquals(ParticipationStatus.WITHDRAWN, confirmed.getStatus());
    }

    @Test
    @DisplayName("No se puede retirar dos veces")
    void cannotWithdrawTwice() {
        Participation participation = new Participation(42L);
        participation.withdraw();

        assertThrows(IllegalStateException.class, participation::withdraw);
    }

    @Test
    @DisplayName("Requiere un estudiante, estado y fecha válidos")
    void rejectsInvalidData() {
        assertThrows(IllegalArgumentException.class, () -> new Participation(null));
        assertThrows(IllegalArgumentException.class, () -> new Participation(0L));
        assertThrows(IllegalArgumentException.class,
                () -> new Participation(42L, null, Instant.now()));
        assertThrows(IllegalArgumentException.class,
                () -> new Participation(42L, ParticipationStatus.REQUESTED, null));
    }
}
