package com.utp.quesale.connections.infrastructure.persistence.jpa.mappers;

import com.utp.quesale.connections.domain.aggregates.Connection;
import com.utp.quesale.connections.infrastructure.persistence.jpa.entities.ConnectionJpaEntity;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ConnectionPersistenceMapper {

    public Connection toDomain(ConnectionJpaEntity entity) {
        return new Connection(
                entity.getId(),
                entity.getRequester().getId(),
                entity.getRecipient().getId(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getRespondedAt()
        );
    }

    public ConnectionJpaEntity toEntity(Connection connection,
                                        StudentJpaEntity requester,
                                        StudentJpaEntity recipient) {
        ConnectionJpaEntity entity = new ConnectionJpaEntity();
        entity.setId(connection.getId());
        entity.setRequester(requester);
        entity.setRecipient(recipient);
        entity.setStatus(connection.getStatus());
        entity.setCreatedAt(connection.getCreatedAt());
        entity.setRespondedAt(connection.getRespondedAt());
        return entity;
    }
}
