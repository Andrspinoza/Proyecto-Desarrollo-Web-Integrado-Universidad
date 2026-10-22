package com.utp.quesale.messaging.infrastructure.persistence.jpa.mappers;

import com.utp.quesale.messaging.domain.aggregates.Message;
import com.utp.quesale.messaging.infrastructure.persistence.jpa.entities.MessageJpaEntity;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class MessagePersistenceMapper {

    // Usa el constructor de rehidratación: nunca registra eventos
    public Message toDomain(MessageJpaEntity entity) {
        return new Message(
                entity.getId(),
                entity.getSender().getId(),
                entity.getRecipient().getId(),
                entity.getContent(),
                entity.getSentAt());
    }

    public MessageJpaEntity toEntity(Message message, StudentJpaEntity sender, StudentJpaEntity recipient) {
        MessageJpaEntity entity = new MessageJpaEntity();
        entity.setId(message.getId());
        entity.setSender(sender);
        entity.setRecipient(recipient);
        entity.setContent(message.getContent());
        entity.setSentAt(message.getSentAt());
        return entity;
    }
}
