package com.utp.quesale.profiles.domain.aggregates;

import com.utp.quesale.profiles.domain.valueobjects.FullName;
import com.utp.quesale.profiles.domain.valueobjects.StudentRecordId;
import com.utp.quesale.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Instant;
import java.util.Locale;

public class Student extends AbstractDomainAggregateRoot<Student> {
    private final Long id;
    private final StudentRecordId recordId;
    private final Long universityId;
    private final String email;
    private final FullName fullName;
    private final Instant createdAt;

    public Student(Long universityId, String email, FullName fullName) {
        this(null, new StudentRecordId(), universityId, email, fullName, Instant.now());
    }

    // El mapper recupera la identidad y fecha originales sin generar eventos.
    public Student(Long id, StudentRecordId recordId, Long universityId,
                   String email, FullName fullName, Instant createdAt) {
        if (recordId == null) throw new IllegalArgumentException("Student record id is required");
        if (universityId == null || universityId <= 0)
            throw new IllegalArgumentException("University id must be positive");
        if (fullName == null) throw new IllegalArgumentException("Full name is required");
        if (createdAt == null) throw new IllegalArgumentException("Created at is required");
        this.id = id;
        this.recordId = recordId;
        this.universityId = universityId;
        this.email = validEmail(email);
        this.fullName = fullName;
        this.createdAt = createdAt;
    }

    private static String validEmail(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Email is required");
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new IllegalArgumentException("Email format is invalid");
        return normalized;
    }

    public Long getId() { return id; }
    public StudentRecordId getRecordId() { return recordId; }
    public Long getUniversityId() { return universityId; }
    public String getEmail() { return email; }
    public FullName getFullName() { return fullName; }
    public Instant getCreatedAt() { return createdAt; }
}
