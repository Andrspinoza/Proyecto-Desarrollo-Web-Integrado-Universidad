package com.utp.quesale.calls.domain;

import com.utp.quesale.calls.domain.valueobjects.CallSignal;
import com.utp.quesale.calls.domain.valueobjects.CallSignalType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class CallSignalTest {

    private static final Instant NOW = Instant.parse("2026-11-18T12:00:00Z");

    @Test
    @DisplayName("Una señal válida conserva sus datos")
    void validSignal() {
        CallSignal signal = new CallSignal(1L, 2L, CallSignalType.OFFER, "{\"sdp\":\"x\"}", NOW);

        assertEquals(1L, signal.fromStudentId());
        assertEquals(2L, signal.toStudentId());
        assertEquals(CallSignalType.OFFER, signal.type());
        assertEquals("{\"sdp\":\"x\"}", signal.payload());
    }

    @Test
    @DisplayName("Un estudiante no puede llamarse a sí mismo")
    void selfCallIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new CallSignal(1L, 1L, CallSignalType.OFFER, null, NOW));
    }

    @Test
    @DisplayName("Los ids, el tipo y la fecha son obligatorios")
    void requiredFields() {
        assertThrows(IllegalArgumentException.class, () -> new CallSignal(null, 2L, CallSignalType.OFFER, null, NOW));
        assertThrows(IllegalArgumentException.class, () -> new CallSignal(1L, 0L, CallSignalType.OFFER, null, NOW));
        assertThrows(IllegalArgumentException.class, () -> new CallSignal(1L, 2L, null, null, NOW));
        assertThrows(IllegalArgumentException.class, () -> new CallSignal(1L, 2L, CallSignalType.OFFER, null, null));
    }

    @Test
    @DisplayName("El contenido tiene un tamaño máximo y el contenido en blanco se descarta")
    void payloadRules() {
        String tooLong = "x".repeat(CallSignal.PAYLOAD_MAX_LENGTH + 1);
        assertThrows(IllegalArgumentException.class,
                () -> new CallSignal(1L, 2L, CallSignalType.OFFER, tooLong, NOW));
        assertNull(new CallSignal(1L, 2L, CallSignalType.HANGUP, "   ", NOW).payload());
        assertDoesNotThrow(() -> new CallSignal(1L, 2L, CallSignalType.OFFER,
                "x".repeat(CallSignal.PAYLOAD_MAX_LENGTH), NOW));
    }

    @Test
    @DisplayName("Los tipos se leen y se escriben en el formato del cliente")
    void typesParseFromWire() {
        assertEquals(CallSignalType.ICE_CANDIDATE, CallSignalType.fromWire("ice-candidate"));
        assertEquals(CallSignalType.OFFER, CallSignalType.fromWire(" Offer "));
        assertEquals("ice-candidate", CallSignalType.ICE_CANDIDATE.wire());
        assertEquals("hangup", CallSignalType.HANGUP.wire());
        assertThrows(IllegalArgumentException.class, () -> CallSignalType.fromWire("grito"));
        assertThrows(IllegalArgumentException.class, () -> CallSignalType.fromWire(null));
        assertThrows(IllegalArgumentException.class, () -> CallSignalType.fromWire(" "));
    }

    @Test
    @DisplayName("Colgar y rechazar son señales de cierre")
    void terminalTypes() {
        assertTrue(CallSignalType.HANGUP.isTerminal());
        assertTrue(CallSignalType.REJECT.isTerminal());
        assertFalse(CallSignalType.OFFER.isTerminal());
        assertFalse(CallSignalType.ANSWER.isTerminal());
        assertFalse(CallSignalType.ICE_CANDIDATE.isTerminal());
    }
}
