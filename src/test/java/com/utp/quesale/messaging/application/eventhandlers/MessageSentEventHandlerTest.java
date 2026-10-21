package com.utp.quesale.messaging.application.eventhandlers;

import com.utp.quesale.messaging.application.dtos.ChatMessageDto;
import com.utp.quesale.messaging.domain.events.MessageSentEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class MessageSentEventHandlerTest {

    private final SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
    private final MessageSentEventHandler handler = new MessageSentEventHandler(template);

    @Test
    void pushesTheMessageToTheRecipientQueue() {
        Instant sentAt = Instant.parse("2026-10-21T15:00:00Z");

        handler.on(new MessageSentEvent(10L, 1L, 2L, "Hola, ¿vamos por un café?", sentAt));

        ArgumentCaptor<ChatMessageDto> payload = ArgumentCaptor.forClass(ChatMessageDto.class);
        verify(template).convertAndSendToUser(eq("2"), eq("/queue/messages"), payload.capture());
        assertEquals(new ChatMessageDto(10L, 1L, 2L, "Hola, ¿vamos por un café?", sentAt), payload.getValue());
        verifyNoMoreInteractions(template);
    }

    @Test
    void targetsOnlyTheRecipientNotTheSender() {
        handler.on(new MessageSentEvent(11L, 5L, 6L, "ok", Instant.now()));

        verify(template).convertAndSendToUser(eq("6"), eq("/queue/messages"), any(Object.class));
        verifyNoMoreInteractions(template);
    }
}
