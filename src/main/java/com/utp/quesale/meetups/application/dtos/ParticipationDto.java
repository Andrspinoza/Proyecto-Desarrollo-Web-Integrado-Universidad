package com.utp.quesale.meetups.application.dtos;

import com.utp.quesale.meetups.domain.valueobjects.ParticipationStatus;

import java.time.Instant;

public record ParticipationDto(Long id, Long studentId, ParticipationStatus status, Instant registeredAt) {
}
