package com.utp.quesale.connections.domain.events;

import java.time.Instant;

public record ConnectionRequestedEvent(
        Long connectionId,
        Long requesterId,
        Long recipientId,
        Instant occurredAt) {
}
