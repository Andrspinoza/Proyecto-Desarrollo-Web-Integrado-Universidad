package com.utp.quesale.iam.domain.aggregates;

import com.utp.quesale.iam.domain.valueobjects.EmailDomain;
import com.utp.quesale.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Instant;
import java.util.Locale;

public class University extends AbstractDomainAggregateRoot<University> {

    private static final int NAME_MAX_LENGTH = 120;

    private Long id;
    private String name;
    private final EmailDomain emailDomain;
    private boolean active;
    private final Instant createdAt;

    public University(String name, EmailDomain emailDomain) {
        this(null, name, emailDomain, true, Instant.now());
    }

    public University(Long id, String name, EmailDomain emailDomain, boolean active, Instant createdAt) {
        if (emailDomain == null) throw new IllegalArgumentException("Email domain is required");
        if (createdAt == null) throw new IllegalArgumentException("Created at is required");
        this.id = id;
        this.name = validName(name);
        this.emailDomain = emailDomain;
        this.active = active;
        this.createdAt = createdAt;
    }

    public boolean accepts(String email) {
        if (email == null || !email.contains("@")) return false;
        String domain = email.substring(email.lastIndexOf('@') + 1).trim().toLowerCase(Locale.ROOT);
        return domain.equals(emailDomain.value());
    }

    public void rename(String name) {
        this.name = validName(name);
    }

    public void deactivate() {
        if (!active) throw new IllegalStateException("University is already inactive");
        active = false;
    }

    private static String validName(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("University name is required");
        String trimmed = value.trim();
        if (trimmed.length() > NAME_MAX_LENGTH)
            throw new IllegalArgumentException("University name must be at most %d characters".formatted(NAME_MAX_LENGTH));
        return trimmed;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public EmailDomain getEmailDomain() {
        return emailDomain;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
