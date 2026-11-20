package com.utp.quesale.calls.infrastructure.websocket;

import com.utp.quesale.calls.domain.services.CallSignalPublisher;
import com.utp.quesale.calls.domain.valueobjects.CallSignal;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class WebSocketCallSignalPublisher implements CallSignalPublisher {

    static final String DESTINATION = "/queue/calls";

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketCallSignalPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void publish(CallSignal signal) {
        CallSignalMessage message = new CallSignalMessage(signal.fromStudentId(), signal.type().wire(),
                signal.payload(), signal.sentAt());
        messagingTemplate.convertAndSendToUser(String.valueOf(signal.toStudentId()), DESTINATION, message);
    }

    public record CallSignalMessage(Long fromStudentId, String type, String payload, Instant sentAt) {
    }
}
