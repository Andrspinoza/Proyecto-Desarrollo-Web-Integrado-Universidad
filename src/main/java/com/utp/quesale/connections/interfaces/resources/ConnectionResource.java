package com.utp.quesale.connections.interfaces.resources;

import com.utp.quesale.connections.domain.valueobjects.ConnectionStatus;

import java.time.Instant;

public record ConnectionResource(
        Long id,
        Long requesterId,
        Long recipientId,
        ConnectionStatus status,
        Instant createdAt,
        Instant respondedAt) {
}
