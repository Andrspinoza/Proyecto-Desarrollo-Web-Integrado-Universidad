package com.utp.quesale.connections.domain.events;

import java.time.Instant;

public record ConnectionAcceptedEvent(
        Long connectionId,
        Long requesterId,
        Long recipientId,
        Instant occurredAt) {
}
