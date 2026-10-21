package com.utp.quesale.connections.application.dtos;

import com.utp.quesale.connections.domain.valueobjects.ConnectionStatus;

import java.time.Instant;

public record ConnectionDto(
        Long id,
        Long requesterId,
        Long recipientId,
        ConnectionStatus status,
        Instant createdAt,
        Instant respondedAt) {
}
