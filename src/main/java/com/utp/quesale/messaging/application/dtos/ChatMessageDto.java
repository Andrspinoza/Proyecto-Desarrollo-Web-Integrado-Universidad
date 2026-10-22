package com.utp.quesale.messaging.application.dtos;

import java.time.Instant;

public record ChatMessageDto(Long messageId, Long senderId, Long recipientId, String content, Instant sentAt) {
}
