package com.utp.quesale.messaging.interfaces.resources;

import java.time.Instant;

public record MessageResource(Long id, Long senderId, Long recipientId, String content, Instant sentAt) {
}
