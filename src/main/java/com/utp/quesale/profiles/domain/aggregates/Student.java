package com.utp.quesale.profiles.domain.aggregates;

import com.utp.quesale.profiles.domain.entities.Profile;
import com.utp.quesale.profiles.domain.events.StudentRegisteredEvent;
import com.utp.quesale.profiles.domain.valueobjects.Career;
import com.utp.quesale.profiles.domain.valueobjects.StudyCycle;
import com.utp.quesale.profiles.domain.valueobjects.Visibility;
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
    private Profile profile;

    public Student(Long universityId, String email, FullName fullName) {
        this(null, new StudentRecordId(), universityId, email, fullName, Instant.now());
    }

    public Student(Long id, StudentRecordId recordId, Long universityId,
                   String email, FullName fullName, Instant createdAt) {
        this(id, recordId, universityId, email, fullName, createdAt, null);
    }

    public Student(Long id, StudentRecordId recordId, Long universityId,
                   String email, FullName fullName, Instant createdAt, Profile profile) {
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
        this.profile = profile;
    }

    public void markAsRegistered() {
        if (id == null) throw new IllegalStateException("Student must be persisted before it is registered");
        registerDomainEvent(new StudentRegisteredEvent(id, email, fullName.firstName(), Instant.now()));
    }

    public void createProfile(Career career, StudyCycle cycle, String description, Visibility visibility) {
        if (profile != null) throw new IllegalStateException("Student already has a profile");
        profile = new Profile(career, cycle, description, visibility);
    }

    public void updateProfile(Career career, StudyCycle cycle, String description, Visibility visibility) {
        requireProfile().update(career, cycle, description, visibility);
    }

    public void addInterest(Long interestId) { requireProfile().addInterest(interestId); }
    public void removeInterest(Long interestId) { requireProfile().removeInterest(interestId); }
    public void addSkill(Long skillId) { requireProfile().addSkill(skillId); }
    public void removeSkill(Long skillId) { requireProfile().removeSkill(skillId); }

    public boolean isVisibleTo(Long viewerStudentId, boolean connected) {
        if (profile == null) return false;
        return profile.getVisibility() == Visibility.PUBLIC
                || (id != null && id.equals(viewerStudentId)) || connected;
    }

    private Profile requireProfile() {
        if (profile == null) throw new IllegalStateException("Student has no profile");
        return profile;
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
    public Profile getProfile() { return profile; }
}
