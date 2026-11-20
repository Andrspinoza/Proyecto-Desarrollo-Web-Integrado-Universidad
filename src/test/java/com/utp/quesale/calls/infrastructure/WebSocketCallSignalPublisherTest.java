package com.utp.quesale.calls.infrastructure;

import com.utp.quesale.calls.domain.valueobjects.CallSignal;
import com.utp.quesale.calls.domain.valueobjects.CallSignalType;
import com.utp.quesale.calls.infrastructure.websocket.WebSocketCallSignalPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WebSocketCallSignalPublisherTest {

    private static final Instant NOW = Instant.parse("2026-11-18T12:00:00Z");

    @Test
    @DisplayName("La señal se envía solo a la cola del destinatario con el tipo en formato del cliente")
    void publishesToRecipientQueue() {
        SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
        WebSocketCallSignalPublisher publisher = new WebSocketCallSignalPublisher(template);

        publisher.publish(new CallSignal(1L, 2L, CallSignalType.ICE_CANDIDATE, "{\"c\":1}", NOW));

        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(template).convertAndSendToUser(eq("2"), eq("/queue/calls"), payload.capture());
        WebSocketCallSignalPublisher.CallSignalMessage message =
                (WebSocketCallSignalPublisher.CallSignalMessage) payload.getValue();
        assertEquals(1L, message.fromStudentId());
        assertEquals("ice-candidate", message.type());
        assertEquals("{\"c\":1}", message.payload());
        assertEquals(NOW, message.sentAt());
    }
}
