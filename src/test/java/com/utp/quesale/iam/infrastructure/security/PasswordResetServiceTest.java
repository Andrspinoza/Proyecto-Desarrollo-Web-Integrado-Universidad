package com.utp.quesale.iam.infrastructure.security;

import com.utp.quesale.iam.infrastructure.mail.PasswordResetEmailSender;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {
    private static final Instant NOW = Instant.parse("2026-11-20T10:00:00Z");
    private final AccountJpaRepository accounts = mock(AccountJpaRepository.class);
    private final PasswordResetJpaRepository resets = mock(PasswordResetJpaRepository.class);
    private final PasswordResetEmailSender sender = mock(PasswordResetEmailSender.class);
    private final AuthRateLimiter limiter = mock(AuthRateLimiter.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final PasswordResetService service =
            new PasswordResetService(accounts, resets, sender, limiter, Clock.fixed(NOW, ZoneOffset.UTC), 60, "https://que-sale.me/");
    private final AccountEntity account = new AccountEntity("ana@utp.edu.pe", encoder.encode("Vieja-clave-1"), 7L);

    @Test void unknownEmailDoesNothingAndRevealsNothing() {
        when(accounts.findByEmailIgnoreCase("nadie@utp.edu.pe")).thenReturn(Optional.empty());
        service.request("nadie@utp.edu.pe");
        verifyNoInteractions(resets, sender);
    }

    @Test void knownEmailStoresOnlyTheHashAndSendsTheLinkWithTheRawToken() {
        when(accounts.findByEmailIgnoreCase("ana@utp.edu.pe")).thenReturn(Optional.of(account));
        when(resets.findByStudentIdAndUsedAtIsNull(7L)).thenReturn(List.of());
        service.request(" ana@utp.edu.pe ");
        var saved = ArgumentCaptor.forClass(PasswordResetEntity.class);
        verify(resets).save(saved.capture());
        var link = ArgumentCaptor.forClass(String.class);
        verify(sender).send(eq("ana@utp.edu.pe"), eq("ana"), link.capture());
        assertTrue(link.getValue().startsWith("https://que-sale.me/restablecer?token="));
        String raw = link.getValue().substring(link.getValue().indexOf("token=") + 6);
        assertEquals(64, saved.getValue().getTokenHash().length());
        assertNotEquals(raw, saved.getValue().getTokenHash());
        assertEquals(NOW.plus(Duration.ofMinutes(60)), saved.getValue().getExpiresAt());
    }

    @Test void aNewRequestInvalidatesThePendingOnesAfterTheCooldown() {
        var old = new PasswordResetEntity(7L, "a".repeat(64), NOW.minusSeconds(600), NOW.plusSeconds(3000));
        when(accounts.findByEmailIgnoreCase("ana@utp.edu.pe")).thenReturn(Optional.of(account));
        when(resets.findByStudentIdAndUsedAtIsNull(7L)).thenReturn(List.of(old));
        service.request("ana@utp.edu.pe");
        assertEquals(NOW, old.getUsedAt());
        verify(resets).save(any(PasswordResetEntity.class));
    }

    @Test void requestsWithinTheCooldownAreIgnored() {
        var recent = new PasswordResetEntity(7L, "b".repeat(64), NOW.minusSeconds(10), NOW.plusSeconds(3500));
        when(accounts.findByEmailIgnoreCase("ana@utp.edu.pe")).thenReturn(Optional.of(account));
        when(resets.findByStudentIdAndUsedAtIsNull(7L)).thenReturn(List.of(recent));
        service.request("ana@utp.edu.pe");
        verify(resets, never()).save(any());
        verifyNoInteractions(sender);
    }

    @Test void aMailFailureDoesNotBreakTheRequest() {
        when(accounts.findByEmailIgnoreCase("ana@utp.edu.pe")).thenReturn(Optional.of(account));
        when(resets.findByStudentIdAndUsedAtIsNull(7L)).thenReturn(List.of());
        doThrow(new IllegalStateException("smtp")).when(sender).send(anyString(), anyString(), anyString());
        assertDoesNotThrow(() -> service.request("ana@utp.edu.pe"));
    }

    @Test void resetChangesThePasswordConsumesTheTokenAndUnlocksTheEmail() {
        var token = com.utp.quesale.iam.domain.valueobjects.VerificationToken.generate();
        var reset = new PasswordResetEntity(7L, token.hash(), NOW.minusSeconds(60), NOW.plusSeconds(3000));
        when(resets.findByTokenHash(token.hash())).thenReturn(Optional.of(reset));
        when(accounts.findByStudentId(7L)).thenReturn(Optional.of(account));
        when(resets.findByStudentIdAndUsedAtIsNull(7L)).thenReturn(List.of(reset));
        service.reset(token.raw(), "Nueva-clave-2");
        assertTrue(encoder.matches("Nueva-clave-2", account.getPasswordHash()));
        assertFalse(encoder.matches("Vieja-clave-1", account.getPasswordHash()));
        assertEquals(NOW, reset.getUsedAt());
        verify(limiter).clearFailures("ana@utp.edu.pe");
    }

    @Test void unknownExpiredAndUsedTokensAreRejectedWith400() {
        var expired = com.utp.quesale.iam.domain.valueobjects.VerificationToken.generate();
        var used = com.utp.quesale.iam.domain.valueobjects.VerificationToken.generate();
        var expiredEntity = new PasswordResetEntity(7L, expired.hash(), NOW.minusSeconds(7200), NOW.minusSeconds(3600));
        var usedEntity = new PasswordResetEntity(7L, used.hash(), NOW.minusSeconds(60), NOW.plusSeconds(3000));
        usedEntity.markUsed(NOW.minusSeconds(30));
        when(resets.findByTokenHash(expired.hash())).thenReturn(Optional.of(expiredEntity));
        when(resets.findByTokenHash(used.hash())).thenReturn(Optional.of(usedEntity));
        for (String raw : List.of("desconocido", expired.raw(), used.raw()))
            assertEquals(400, assertThrows(ResponseStatusException.class,
                    () -> service.reset(raw, "Nueva-clave-2")).getStatusCode().value());
        verify(accounts, never()).save(any());
    }
}
