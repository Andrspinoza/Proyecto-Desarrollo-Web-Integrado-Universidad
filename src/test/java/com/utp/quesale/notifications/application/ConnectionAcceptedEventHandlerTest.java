package com.utp.quesale.notifications.application;

import com.utp.quesale.connections.domain.events.ConnectionAcceptedEvent;
import com.utp.quesale.notifications.application.eventhandlers.ConnectionAcceptedEventHandler;
import com.utp.quesale.notifications.domain.services.NotificationPublisher;
import com.utp.quesale.notifications.domain.valueobjects.MatchNotification;
import com.utp.quesale.profiles.interfaces.acl.ProfilesContextFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ConnectionAcceptedEventHandlerTest {

    private static final Instant NOW = Instant.parse("2026-11-18T12:00:00Z");
    private static final ConnectionAcceptedEvent EVENT = new ConnectionAcceptedEvent(7L, 1L, 2L, NOW);

    private ProfilesContextFacade profiles;
    private NotificationPublisher publisher;
    private ConnectionAcceptedEventHandler handler;

    @BeforeEach
    void setUp() {
        profiles = mock(ProfilesContextFacade.class);
        publisher = mock(NotificationPublisher.class);
        handler = new ConnectionAcceptedEventHandler(profiles, publisher);
        when(profiles.firstNameOfStudent(1L)).thenReturn(Optional.of("Camila"));
        when(profiles.firstNameOfStudent(2L)).thenReturn(Optional.of("Diego"));
    }

    @Test
    @DisplayName("Al aceptarse una conexión, cada estudiante recibe el aviso con el nombre del otro")
    void bothStudentsAreNotified() {
        handler.on(EVENT);

        ArgumentCaptor<MatchNotification> toRequester = ArgumentCaptor.forClass(MatchNotification.class);
        ArgumentCaptor<MatchNotification> toRecipient = ArgumentCaptor.forClass(MatchNotification.class);
        verify(publisher).publishMatch(eq(1L), toRequester.capture());
        verify(publisher).publishMatch(eq(2L), toRecipient.capture());

        assertEquals(2L, toRequester.getValue().matchedStudentId());
        assertEquals("Diego", toRequester.getValue().matchedFirstName());
        assertEquals(1L, toRecipient.getValue().matchedStudentId());
        assertEquals("Camila", toRecipient.getValue().matchedFirstName());
        assertEquals(7L, toRequester.getValue().connectionId());
        assertEquals(NOW, toRecipient.getValue().matchedAt());
    }

    @Test
    @DisplayName("Si no hay nombre disponible el aviso sale igual, con el nombre vacío")
    void missingNameStillNotifies() {
        when(profiles.firstNameOfStudent(any())).thenReturn(Optional.empty());

        handler.on(EVENT);

        ArgumentCaptor<MatchNotification> captor = ArgumentCaptor.forClass(MatchNotification.class);
        verify(publisher, times(2)).publishMatch(any(), captor.capture());
        assertTrue(captor.getAllValues().stream().allMatch(n -> n.matchedFirstName().isEmpty()));
    }

    @Test
    @DisplayName("Si falla el aviso a uno, el otro se notifica y nada se propaga")
    void failureDoesNotBreakTheOther() {
        doThrow(new IllegalStateException("sin canal")).when(publisher).publishMatch(eq(1L), any());

        assertDoesNotThrow(() -> handler.on(EVENT));

        verify(publisher).publishMatch(eq(2L), any());
    }
}
