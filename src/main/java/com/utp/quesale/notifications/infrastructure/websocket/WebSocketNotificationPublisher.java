package com.utp.quesale.notifications.infrastructure.websocket;

import com.utp.quesale.notifications.domain.services.NotificationPublisher;
import com.utp.quesale.notifications.domain.valueobjects.MatchNotification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketNotificationPublisher implements NotificationPublisher {

    static final String MATCHES_DESTINATION = "/queue/matches";

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void publishMatch(Long recipientStudentId, MatchNotification notification) {
        messagingTemplate.convertAndSendToUser(String.valueOf(recipientStudentId), MATCHES_DESTINATION, notification);
    }
}
