package com.utp.quesale.meetups.domain.entities;

import com.utp.quesale.meetups.domain.valueobjects.ParticipationStatus;

import java.time.Instant;

public class Participation {

    private final Long id;
    private final Long studentId;
    private ParticipationStatus status;
    private final Instant registeredAt;

    public Participation(Long studentId) {
        this(studentId, ParticipationStatus.REQUESTED, Instant.now());
    }

    public Participation(Long studentId, ParticipationStatus status, Instant registeredAt) {
        this(null, studentId, status, registeredAt);
    }

    // Constructor de rehidratación: conserva el id de la fila de participación.
    public Participation(Long id, Long studentId, ParticipationStatus status, Instant registeredAt) {
        if (id != null && id <= 0)
            throw new IllegalArgumentException("Participation ID must be positive");
        if (studentId == null || studentId <= 0)
            throw new IllegalArgumentException("Student ID must be positive");
        if (status == null)
            throw new IllegalArgumentException("Participation status is required");
        if (registeredAt == null)
            throw new IllegalArgumentException("Registration date is required");

        this.id = id;
        this.studentId = studentId;
        this.status = status;
        this.registeredAt = registeredAt;
    }

    public void confirm() {
        if (status != ParticipationStatus.REQUESTED)
            throw new IllegalStateException("Only requested participation can be confirmed");
        status = ParticipationStatus.CONFIRMED;
    }

    public void acceptInvitation() {
        if (status != ParticipationStatus.INVITED)
            throw new IllegalStateException("Only an invited student can accept the invitation");
        status = ParticipationStatus.CONFIRMED;
    }

    public void declineInvitation() {
        if (status != ParticipationStatus.INVITED)
            throw new IllegalStateException("Only an invited student can decline the invitation");
        status = ParticipationStatus.DECLINED;
    }

    public void withdraw() {
        if (status != ParticipationStatus.REQUESTED && status != ParticipationStatus.CONFIRMED)
            throw new IllegalStateException("Only requested or confirmed participation can be withdrawn");
        status = ParticipationStatus.WITHDRAWN;
    }

    public Long getStudentId() {
        return studentId;
    }

    public Long getId() {
        return id;
    }

    public ParticipationStatus getStatus() {
        return status;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }
}
