package com.utp.quesale.messaging.application.dtos;

import java.time.Instant;

// Lo que recibe el destinatario por WebSocket en /user/queue/messages
public record ChatMessageDto(Long messageId, Long senderId, Long recipientId, String content, Instant sentAt) {
}
