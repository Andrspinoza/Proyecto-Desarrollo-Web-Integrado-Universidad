package com.utp.quesale.messaging.application.eventhandlers;

import com.utp.quesale.messaging.application.dtos.ChatMessageDto;
import com.utp.quesale.messaging.domain.events.MessageSentEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

// El mensaje ya está guardado (el despachador publica después del commit): aquí solo se entrega al
// destinatario por WebSocket. Sin reglas de negocio; si el destinatario no está conectado, lo verá por REST.
@Component
public class MessageSentEventHandler {

    static final String DESTINATION = "/queue/messages";

    private final SimpMessagingTemplate messagingTemplate;

    public MessageSentEventHandler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void on(MessageSentEvent event) {
        ChatMessageDto payload = new ChatMessageDto(event.messageId(), event.senderId(), event.recipientId(),
                event.content(), event.occurredAt());
        messagingTemplate.convertAndSendToUser(String.valueOf(event.recipientId()), DESTINATION, payload);
    }
}
