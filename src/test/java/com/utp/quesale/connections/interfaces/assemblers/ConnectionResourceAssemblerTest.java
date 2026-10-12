package com.utp.quesale.connections.interfaces.assemblers;

import com.utp.quesale.connections.domain.aggregates.Connection;
import com.utp.quesale.connections.domain.valueobjects.ConnectionStatus;
import com.utp.quesale.connections.interfaces.resources.ConnectionResource;
import com.utp.quesale.connections.interfaces.resources.CreateConnectionResource;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConnectionResourceAssemblerTest {

    @Test
    void createsDomainConnectionFromResourceAndRequesterId() {
        Connection connection = ConnectionResourceAssembler.toDomainFromResource(
                3L, new CreateConnectionResource(7L));

        assertEquals(3L, connection.getRequesterId());
        assertEquals(7L, connection.getRecipientId());
        assertEquals(ConnectionStatus.PENDING, connection.getStatus());
        assertNull(connection.getRespondedAt());
    }

    @Test
    void createsResourceFromDomainConnection() {
        Instant createdAt = Instant.parse("2026-10-11T12:00:00Z");
        Instant respondedAt = Instant.parse("2026-10-11T12:30:00Z");
        Connection connection = new Connection(5L, 3L, 7L, ConnectionStatus.ACCEPTED,
                createdAt, respondedAt);

        ConnectionResource resource = ConnectionResourceAssembler.toResourceFromEntity(connection);

        assertEquals(connection.getId(), resource.id());
        assertEquals(connection.getRequesterId(), resource.requesterId());
        assertEquals(connection.getRecipientId(), resource.recipientId());
        assertEquals(connection.getStatus(), resource.status());
        assertEquals(createdAt, resource.createdAt());
        assertEquals(respondedAt, resource.respondedAt());
    }
}
