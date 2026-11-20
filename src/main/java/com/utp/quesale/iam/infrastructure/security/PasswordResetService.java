package com.utp.quesale.iam.infrastructure.security;

import com.utp.quesale.iam.domain.valueobjects.VerificationToken;
import com.utp.quesale.iam.infrastructure.mail.PasswordResetEmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class PasswordResetService {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final Duration COOLDOWN = Duration.ofSeconds(60);
    private static final String INVALID_LINK = "El enlace no es válido o ya venció";

    private final AccountJpaRepository accounts;
    private final PasswordResetJpaRepository resets;
    private final PasswordResetEmailSender sender;
    private final AuthRateLimiter limiter;
    private final Clock clock;
    private final Duration ttl;
    private final String baseUrl;
    private final BCryptPasswordEncoder passwords = new BCryptPasswordEncoder();

    public PasswordResetService(AccountJpaRepository accounts, PasswordResetJpaRepository resets,
                                PasswordResetEmailSender sender, AuthRateLimiter limiter, Clock clock,
                                @Value("${app.password-reset.ttl-minutes:60}") long ttlMinutes,
                                @Value("${app.verification.base-url:http://localhost:8080}") String baseUrl) {
        this.accounts = accounts;
        this.resets = resets;
        this.sender = sender;
        this.limiter = limiter;
        this.clock = clock;
        this.ttl = Duration.ofMinutes(ttlMinutes);
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @Transactional
    public void request(String rawEmail) {
        var account = accounts.findByEmailIgnoreCase(rawEmail.trim()).orElse(null);
        if (account == null) return;
        Instant now = clock.instant();
        var pending = resets.findByStudentIdAndUsedAtIsNull(account.getStudentId());
        boolean tooSoon = pending.stream().anyMatch(item -> item.getCreatedAt().plus(COOLDOWN).isAfter(now));
        if (tooSoon) return;
        pending.forEach(item -> item.markUsed(now));
        VerificationToken token = VerificationToken.generate();
        resets.save(new PasswordResetEntity(account.getStudentId(), token.hash(), now, now.plus(ttl)));
        String link = baseUrl + "/restablecer?token=" + token.raw();
        try {
            sender.send(account.getEmail(), account.getEmail().split("@", 2)[0], link);
        } catch (RuntimeException ex) {
            log.warn("No se pudo enviar el enlace de restablecimiento a la cuenta {}", account.getStudentId());
        }
    }

    @Transactional
    public void reset(String rawToken, String newPassword) {
        Instant now = clock.instant();
        var reset = resets.findByTokenHash(VerificationToken.hashOf(rawToken)).orElse(null);
        if (reset == null || !reset.isUsable(now))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_LINK);
        var account = accounts.findByStudentId(reset.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_LINK));
        account.changePasswordHash(passwords.encode(newPassword));
        accounts.save(account);
        resets.findByStudentIdAndUsedAtIsNull(reset.getStudentId()).forEach(item -> item.markUsed(now));
        limiter.clearFailures(account.getEmail());
    }
}
