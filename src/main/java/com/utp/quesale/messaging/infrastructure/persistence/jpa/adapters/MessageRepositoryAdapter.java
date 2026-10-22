package com.utp.quesale.messaging.infrastructure.persistence.jpa.adapters;

import com.utp.quesale.messaging.domain.aggregates.Message;
import com.utp.quesale.messaging.domain.repositories.MessageRepository;
import com.utp.quesale.messaging.infrastructure.persistence.jpa.entities.MessageJpaEntity;
import com.utp.quesale.messaging.infrastructure.persistence.jpa.mappers.MessagePersistenceMapper;
import com.utp.quesale.messaging.infrastructure.persistence.jpa.repositories.MessageJpaRepository;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class MessageRepositoryAdapter implements MessageRepository {
    private final MessageJpaRepository repository;
    private final MessagePersistenceMapper mapper;
    private final EntityManager entityManager;

    public MessageRepositoryAdapter(
            MessageJpaRepository repository,
            MessagePersistenceMapper mapper,
            EntityManager entityManager) {
        this.repository = repository;
        this.mapper = mapper;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public Message save(Message message) {
        MessageJpaEntity entity = mapper.toEntity(
                message,
                entityManager.getReference(StudentJpaEntity.class, message.getSenderId()),
                entityManager.getReference(StudentJpaEntity.class, message.getRecipientId()));
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Message> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> findConversation(Long studentId, Long otherStudentId) {
        return repository.findConversation(studentId, otherStudentId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
