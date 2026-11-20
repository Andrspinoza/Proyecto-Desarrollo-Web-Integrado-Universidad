package com.utp.quesale.notifications.application.eventhandlers;

import com.utp.quesale.connections.domain.events.ConnectionAcceptedEvent;
import com.utp.quesale.notifications.domain.services.NotificationPublisher;
import com.utp.quesale.notifications.domain.valueobjects.MatchNotification;
import com.utp.quesale.profiles.interfaces.acl.ProfilesContextFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ConnectionAcceptedEventHandler {

    private static final Logger log = LoggerFactory.getLogger(ConnectionAcceptedEventHandler.class);

    private final ProfilesContextFacade profiles;
    private final NotificationPublisher publisher;

    public ConnectionAcceptedEventHandler(ProfilesContextFacade profiles, NotificationPublisher publisher) {
        this.profiles = profiles;
        this.publisher = publisher;
    }

    @EventListener
    public void on(ConnectionAcceptedEvent event) {
        notifyMatch(event, event.requesterId(), event.recipientId());
        notifyMatch(event, event.recipientId(), event.requesterId());
    }

    private void notifyMatch(ConnectionAcceptedEvent event, Long toStudentId, Long matchedStudentId) {
        try {
            String firstName = profiles.firstNameOfStudent(matchedStudentId).orElse("");
            publisher.publishMatch(toStudentId, new MatchNotification(event.connectionId(), matchedStudentId,
                    firstName, event.occurredAt()));
        } catch (RuntimeException ex) {
            log.warn("No se pudo avisar el match de la conexión {} al estudiante {}: {}",
                    event.connectionId(), toStudentId, ex.getMessage());
        }
    }
}
