package com.utp.quesale.meetups.interfaces.resources;

import com.utp.quesale.meetups.domain.valueobjects.ParticipationStatus;

import java.time.Instant;

public record ParticipationResource(
        Long id,
        Long studentId,
        ParticipationStatus status,
        Instant registeredAt) {
}
