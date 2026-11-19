package com.utp.quesale.iam.domain;

import com.utp.quesale.iam.domain.aggregates.EmailVerification;
import com.utp.quesale.iam.domain.exceptions.VerificationTokenExpiredException;
import com.utp.quesale.iam.domain.valueobjects.VerificationToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class EmailVerificationTest {

    private static final Instant NOW = Instant.parse("2026-11-18T12:00:00Z");

    private EmailVerification issue() {
        return EmailVerification.issue(5L, " Camila@UTP.edu.pe ", "hash", Duration.ofHours(24), NOW);
    }

    @Test
    @DisplayName("Al emitir queda pendiente, con el correo normalizado y vencimiento a 24 horas")
    void issuedVerificationIsPending() {
        EmailVerification verification = issue();

        assertFalse(verification.isVerified());
        assertNull(verification.getVerifiedAt());
        assertEquals("camila@utp.edu.pe", verification.getEmail());
        assertEquals(NOW.plus(Duration.ofHours(24)), verification.getExpiresAt());
        assertEquals(NOW, verification.getCreatedAt());
    }

    @Test
    @DisplayName("Confirmar antes de vencer la deja verificada")
    void confirmBeforeExpiry() {
        EmailVerification verification = issue();

        verification.confirm(NOW.plusSeconds(60));

        assertTrue(verification.isVerified());
        assertEquals(NOW.plusSeconds(60), verification.getVerifiedAt());
    }

    @Test
    @DisplayName("Confirmar justo al vencer ya no se permite")
    void confirmAtExpiryIsRejected() {
        EmailVerification verification = issue();

        assertThrows(VerificationTokenExpiredException.class,
                () -> verification.confirm(NOW.plus(Duration.ofHours(24))));
        assertFalse(verification.isVerified());
        assertTrue(verification.isExpired(NOW.plus(Duration.ofHours(24))));
        assertFalse(verification.isExpired(NOW.plusSeconds(1)));
    }

    @Test
    @DisplayName("Confirmar de nuevo conserva la fecha original")
    void confirmTwiceKeepsFirstDate() {
        EmailVerification verification = issue();
        verification.confirm(NOW.plusSeconds(10));

        verification.confirm(NOW.plus(Duration.ofDays(3)));

        assertEquals(NOW.plusSeconds(10), verification.getVerifiedAt());
    }

    @Test
    @DisplayName("Los datos obligatorios se validan")
    void invalidDataIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> EmailVerification.issue(null, "a@utp.edu.pe", "hash", Duration.ofHours(1), NOW));
        assertThrows(IllegalArgumentException.class,
                () -> EmailVerification.issue(0L, "a@utp.edu.pe", "hash", Duration.ofHours(1), NOW));
        assertThrows(IllegalArgumentException.class,
                () -> EmailVerification.issue(1L, "sin-arroba", "hash", Duration.ofHours(1), NOW));
        assertThrows(IllegalArgumentException.class,
                () -> EmailVerification.issue(1L, " ", "hash", Duration.ofHours(1), NOW));
        assertThrows(IllegalArgumentException.class,
                () -> EmailVerification.issue(1L, "a@utp.edu.pe", " ", Duration.ofHours(1), NOW));
        assertThrows(IllegalArgumentException.class,
                () -> EmailVerification.issue(1L, "a@utp.edu.pe", "hash", Duration.ZERO, NOW));
        assertThrows(IllegalArgumentException.class,
                () -> EmailVerification.issue(1L, "a@utp.edu.pe", "hash", Duration.ofHours(1), null));
    }

    @Test
    @DisplayName("El token generado es único y su hash es estable")
    void tokenIsRandomAndHashIsStable() {
        VerificationToken first = VerificationToken.generate();
        VerificationToken second = VerificationToken.generate();

        assertNotEquals(first.raw(), second.raw());
        assertEquals(43, first.raw().length());
        assertEquals(64, first.hash().length());
        assertEquals(first.hash(), VerificationToken.hashOf(first.raw()));
        assertEquals(first.hash(), VerificationToken.hashOf("  " + first.raw() + " "));
        assertNotEquals(first.raw(), first.hash());
        assertThrows(IllegalArgumentException.class, () -> VerificationToken.hashOf(null));
        assertThrows(IllegalArgumentException.class, () -> VerificationToken.hashOf(" "));
    }
}
