package com.utp.quesale.connections.interfaces.assemblers;

import com.utp.quesale.connections.application.commands.CreateConnectionCommand;
import com.utp.quesale.connections.application.dtos.ConnectionDto;
import com.utp.quesale.connections.interfaces.resources.ConnectionResource;
import com.utp.quesale.connections.interfaces.resources.CreateConnectionResource;

public final class ConnectionResourceAssembler {

    private ConnectionResourceAssembler() {
    }

    public static CreateConnectionCommand toCreateCommand(Long requesterId, CreateConnectionResource resource) {
        return new CreateConnectionCommand(requesterId, resource.recipientId());
    }

    public static ConnectionResource toResourceFromDto(ConnectionDto connection) {
        return new ConnectionResource(connection.id(), connection.requesterId(),
                connection.recipientId(), connection.status(), connection.createdAt(),
                connection.respondedAt());
    }

}
