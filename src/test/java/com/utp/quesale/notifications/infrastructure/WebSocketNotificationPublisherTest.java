package com.utp.quesale.notifications.infrastructure;

import com.utp.quesale.notifications.domain.valueobjects.MatchNotification;
import com.utp.quesale.notifications.infrastructure.websocket.WebSocketNotificationPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WebSocketNotificationPublisherTest {

    @Test
    @DisplayName("El aviso va solo a la cola de matches del estudiante indicado")
    void publishesToTheStudentQueue() {
        SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
        WebSocketNotificationPublisher publisher = new WebSocketNotificationPublisher(template);
        MatchNotification notification = new MatchNotification(7L, 2L, "Diego", Instant.parse("2026-11-18T12:00:00Z"));

        publisher.publishMatch(1L, notification);

        verify(template).convertAndSendToUser("1", "/queue/matches", notification);
    }
}
