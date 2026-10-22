package com.utp.quesale.communities.domain.entities;

import com.utp.quesale.communities.domain.valueobjects.MemberRole;

import java.time.Instant;

public class Membership {

    private Long id;
    private final Long studentId;
    private final MemberRole role;
    private final Instant joinedAt;

    public Membership(Long studentId, MemberRole role) {
        this(null, studentId, role, Instant.now());
    }

    public Membership(Long id, Long studentId, MemberRole role, Instant joinedAt) {
        if (studentId == null) throw new IllegalArgumentException("Student id is required");
        if (role == null) throw new IllegalArgumentException("Member role is required");
        if (joinedAt == null) throw new IllegalArgumentException("Joined at is required");
        this.id = id;
        this.studentId = studentId;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public boolean isAdmin() {
        return role == MemberRole.ADMIN;
    }

    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public MemberRole getRole() {
        return role;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }
}
