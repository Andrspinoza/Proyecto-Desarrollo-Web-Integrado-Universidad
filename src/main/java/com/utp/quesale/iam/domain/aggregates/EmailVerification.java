package com.utp.quesale.iam.domain.aggregates;

import com.utp.quesale.iam.domain.exceptions.VerificationTokenExpiredException;
import com.utp.quesale.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

public class EmailVerification extends AbstractDomainAggregateRoot<EmailVerification> {

    private final Long id;
    private final Long studentId;
    private final String email;
    private final String tokenHash;
    private final Instant expiresAt;
    private Instant verifiedAt;
    private final Instant createdAt;

    public static EmailVerification issue(Long studentId, String email, String tokenHash, Duration ttl, Instant now) {
        if (ttl == null || ttl.isZero() || ttl.isNegative())
            throw new IllegalArgumentException("Verification lifetime must be positive");
        if (now == null) throw new IllegalArgumentException("Current time is required");
        return new EmailVerification(null, studentId, email, tokenHash, now.plus(ttl), null, now);
    }

    public EmailVerification(Long id, Long studentId, String email, String tokenHash,
                             Instant expiresAt, Instant verifiedAt, Instant createdAt) {
        if (studentId == null || studentId <= 0) throw new IllegalArgumentException("Student id must be positive");
        if (tokenHash == null || tokenHash.isBlank()) throw new IllegalArgumentException("Token hash is required");
        if (expiresAt == null) throw new IllegalArgumentException("Expiration is required");
        if (createdAt == null) throw new IllegalArgumentException("Created at is required");
        this.id = id;
        this.studentId = studentId;
        this.email = validEmail(email);
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.verifiedAt = verifiedAt;
        this.createdAt = createdAt;
    }

    public void confirm(Instant now) {
        if (verifiedAt != null) return;
        if (!now.isBefore(expiresAt)) throw new VerificationTokenExpiredException();
        verifiedAt = now;
    }

    public boolean isVerified() {
        return verifiedAt != null;
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    private static String validEmail(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Email is required");
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (!normalized.contains("@")) throw new IllegalArgumentException("Email format is invalid");
        return normalized;
    }

    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getEmail() {
        return email;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
