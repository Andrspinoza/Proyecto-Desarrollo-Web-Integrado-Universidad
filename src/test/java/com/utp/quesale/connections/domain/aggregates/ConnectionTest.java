package com.utp.quesale.connections.domain.aggregates;

import com.utp.quesale.connections.domain.events.ConnectionAcceptedEvent;
import com.utp.quesale.connections.domain.events.ConnectionRequestedEvent;
import com.utp.quesale.connections.domain.exceptions.ConnectionOperationForbiddenException;
import com.utp.quesale.connections.domain.valueobjects.ConnectionStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConnectionTest {

    @Test
    void createsPendingConnectionWithoutResponseTime() {
        Connection connection = new Connection(1L, 2L);

        assertEquals(1L, connection.getRequesterId());
        assertEquals(2L, connection.getRecipientId());
        assertEquals(ConnectionStatus.PENDING, connection.getStatus());
        assertNotNull(connection.getCreatedAt());
        assertNull(connection.getRespondedAt());
    }

    @Test
    void rejectsNullIdsAndSelfConnections() {
        assertThrows(IllegalArgumentException.class, () -> new Connection(null, 2L));
        assertThrows(IllegalArgumentException.class, () -> new Connection(1L, null));
        assertThrows(IllegalArgumentException.class, () -> new Connection(1L, 1L));
        assertThrows(IllegalArgumentException.class, () -> new Connection(0L, 2L));
        assertThrows(IllegalArgumentException.class, () -> new Connection(1L, -2L));
    }

    @Test
    void rejectsInvalidRehydratedState() {
        Instant createdAt = Instant.now();

        assertThrows(IllegalArgumentException.class, () -> new Connection(
                3L, 1L, 2L, ConnectionStatus.PENDING, createdAt, createdAt));
        assertThrows(IllegalArgumentException.class, () -> new Connection(
                3L, 1L, 2L, ConnectionStatus.ACCEPTED, createdAt, null));
        assertThrows(IllegalArgumentException.class, () -> new Connection(
                3L, 1L, 2L, ConnectionStatus.REJECTED, createdAt, createdAt.minusSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> new Connection(
                0L, 1L, 2L, ConnectionStatus.PENDING, createdAt, null));
    }

    @Test
    void marksSavedConnectionAsCreatedAndEmitsRequestEventOnlyOnce() {
        Connection connection = new Connection(3L, 1L, 2L, ConnectionStatus.PENDING, Instant.now(), null);

        connection.markAsCreated();

        Object event = connection.pullDomainEvents().get(0);
        assertInstanceOf(ConnectionRequestedEvent.class, event);
        assertEquals(3L, ((ConnectionRequestedEvent) event).connectionId());
        assertThrows(IllegalStateException.class, connection::markAsCreated);
    }

    @Test
    void requiresSavedConnectionBeforeRegisteringCreationEvent() {
        Connection connection = new Connection(1L, 2L);

        assertThrows(IllegalStateException.class, connection::markAsCreated);
    }

    @Test
    void requiresSavedConnectionBeforeAcceptanceEvent() {
        Connection connection = new Connection(1L, 2L);

        assertThrows(IllegalStateException.class, () -> connection.accept(2L));
        assertEquals(ConnectionStatus.PENDING, connection.getStatus());
        assertNull(connection.getRespondedAt());
        assertEquals(0, connection.pullDomainEvents().size());
    }

    @Test
    void acceptsPendingConnectionOnlyByRecipientAndEmitsMatchEvent() {
        Connection connection = new Connection(3L, 1L, 2L, ConnectionStatus.PENDING, Instant.now(), null);

        connection.accept(2L);

        assertEquals(ConnectionStatus.ACCEPTED, connection.getStatus());
        assertNotNull(connection.getRespondedAt());
        Object event = connection.pullDomainEvents().get(0);
        assertInstanceOf(ConnectionAcceptedEvent.class, event);
        assertEquals(1L, ((ConnectionAcceptedEvent) event).requesterId());
        assertEquals(2L, ((ConnectionAcceptedEvent) event).recipientId());
    }

    @Test
    void rejectsResponseByStudentOtherThanRecipient() {
        Connection connection = new Connection(1L, 2L);

        assertThrows(ConnectionOperationForbiddenException.class, () -> connection.accept(1L));
        assertThrows(ConnectionOperationForbiddenException.class, () -> connection.reject(1L));
    }

    @Test
    void rejectsPendingConnectionOnlyByRecipient() {
        Connection connection = new Connection(1L, 2L);

        connection.reject(2L);

        assertEquals(ConnectionStatus.REJECTED, connection.getStatus());
        assertNotNull(connection.getRespondedAt());
        assertThrows(IllegalStateException.class, () -> connection.reject(2L));
    }

    @Test
    void cancelsPendingConnectionOnlyByRequester() {
        Connection connection = new Connection(1L, 2L);

        connection.cancel(1L);

        assertEquals(ConnectionStatus.CANCELLED, connection.getStatus());
        assertNotNull(connection.getRespondedAt());
        assertThrows(IllegalStateException.class, () -> connection.cancel(1L));
    }

    @Test
    void rejectsCancellationByStudentOtherThanRequester() {
        Connection connection = new Connection(1L, 2L);

        assertThrows(ConnectionOperationForbiddenException.class, () -> connection.cancel(2L));
    }

    @Test
    void rejectsChangingResolvedConnectionsAndMissingActorIds() {
        Connection connection = new Connection(3L, 1L, 2L, ConnectionStatus.ACCEPTED,
                Instant.now().minusSeconds(2), Instant.now());

        assertThrows(IllegalStateException.class, () -> connection.accept(2L));
        assertThrows(IllegalStateException.class, () -> connection.reject(2L));
        assertThrows(IllegalStateException.class, () -> connection.cancel(1L));

        Connection pending = new Connection(1L, 2L);
        assertThrows(IllegalArgumentException.class, () -> pending.accept(null));
        assertThrows(IllegalArgumentException.class, () -> pending.reject(null));
        assertThrows(IllegalArgumentException.class, () -> pending.cancel(null));
    }
}
