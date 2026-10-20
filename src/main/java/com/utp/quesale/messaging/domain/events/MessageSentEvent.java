package com.utp.quesale.messaging.domain.events;

import java.time.Instant;

// Lenguaje publicado de messaging: el WebSocket lo usa para empujar el mensaje al destinatario
public record MessageSentEvent(Long messageId, Long senderId, Long recipientId, String content, Instant occurredAt) {
}
