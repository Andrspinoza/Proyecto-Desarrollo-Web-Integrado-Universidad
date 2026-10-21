package com.utp.quesale.connections.application.assemblers;

import com.utp.quesale.connections.application.dtos.ConnectionDto;
import com.utp.quesale.connections.domain.aggregates.Connection;

public final class ConnectionsDtoAssembler {

    private ConnectionsDtoAssembler() {
    }

    public static ConnectionDto toDto(Connection connection) {
        return new ConnectionDto(
                connection.getId(),
                connection.getRequesterId(),
                connection.getRecipientId(),
                connection.getStatus(),
                connection.getCreatedAt(),
                connection.getRespondedAt()
        );
    }
}
