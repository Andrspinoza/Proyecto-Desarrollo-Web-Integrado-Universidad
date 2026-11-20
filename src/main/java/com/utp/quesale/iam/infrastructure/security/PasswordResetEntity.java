package com.utp.quesale.iam.infrastructure.security;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "password_resets",
        uniqueConstraints = @UniqueConstraint(name = "uk_password_reset_hash", columnNames = "token_hash"),
        indexes = @Index(name = "ix_password_reset_student", columnList = "student_id"))
public class PasswordResetEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "student_id", nullable = false) private Long studentId;
    @Column(name = "token_hash", nullable = false, length = 64) private String tokenHash;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "used_at") private Instant usedAt;

    protected PasswordResetEntity() {}

    public PasswordResetEntity(Long studentId, String tokenHash, Instant createdAt, Instant expiresAt) {
        this.studentId = studentId;
        this.tokenHash = tokenHash;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public boolean isUsable(Instant now) {
        return usedAt == null && now.isBefore(expiresAt);
    }

    public void markUsed(Instant now) {
        this.usedAt = now;
    }

    public Long getStudentId() { return studentId; }
    public String getTokenHash() { return tokenHash; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getUsedAt() { return usedAt; }
}
