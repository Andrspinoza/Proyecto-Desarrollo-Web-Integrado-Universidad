package com.utp.quesale.connections.interfaces.assemblers;

import com.utp.quesale.connections.application.commands.CreateConnectionCommand;
import com.utp.quesale.connections.application.dtos.ConnectionDto;
import com.utp.quesale.connections.domain.valueobjects.ConnectionStatus;
import com.utp.quesale.connections.interfaces.resources.ConnectionResource;
import com.utp.quesale.connections.interfaces.resources.CreateConnectionResource;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConnectionResourceAssemblerTest {

    @Test
    void createsCommandFromResourceAndRequesterId() {
        CreateConnectionCommand command = ConnectionResourceAssembler.toCreateCommand(
                3L, new CreateConnectionResource(7L));

        assertEquals(3L, command.requesterId());
        assertEquals(7L, command.recipientId());
    }

    @Test
    void createsResourceFromApplicationDto() {
        Instant createdAt = Instant.parse("2026-10-11T12:00:00Z");
        Instant respondedAt = Instant.parse("2026-10-11T12:30:00Z");
        ConnectionDto connection = new ConnectionDto(5L, 3L, 7L, ConnectionStatus.ACCEPTED,
                createdAt, respondedAt);

        ConnectionResource resource = ConnectionResourceAssembler.toResourceFromDto(connection);

        assertEquals(connection.id(), resource.id());
        assertEquals(connection.requesterId(), resource.requesterId());
        assertEquals(connection.recipientId(), resource.recipientId());
        assertEquals(connection.status(), resource.status());
        assertEquals(createdAt, resource.createdAt());
        assertEquals(respondedAt, resource.respondedAt());
    }
}
