package com.utp.quesale.messaging.domain.events;

import java.time.Instant;

public record MessageSentEvent(Long messageId, Long senderId, Long recipientId, String content, Instant occurredAt) {
}
