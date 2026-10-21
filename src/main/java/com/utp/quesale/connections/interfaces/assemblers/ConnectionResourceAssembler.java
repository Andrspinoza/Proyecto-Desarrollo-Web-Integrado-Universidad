package com.utp.quesale.connections.interfaces.assemblers;

import com.utp.quesale.connections.domain.aggregates.Connection;
import com.utp.quesale.connections.interfaces.resources.ConnectionResource;
import com.utp.quesale.connections.interfaces.resources.CreateConnectionResource;

public final class ConnectionResourceAssembler {

    private ConnectionResourceAssembler() {
    }

    public static Connection toDomainFromResource(Long requesterId, CreateConnectionResource resource) {
        return new Connection(requesterId, resource.recipientId());
    }

    public static ConnectionResource toResourceFromEntity(Connection connection) {
        return new ConnectionResource(connection.getId(), connection.getRequesterId(),
                connection.getRecipientId(), connection.getStatus(), connection.getCreatedAt(),
                connection.getRespondedAt());
    }
}
