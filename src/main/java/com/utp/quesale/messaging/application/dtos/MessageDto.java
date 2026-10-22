package com.utp.quesale.messaging.application.dtos;

import java.time.Instant;

public record MessageDto(Long id, Long senderId, Long recipientId, String content, Instant sentAt) {
}
