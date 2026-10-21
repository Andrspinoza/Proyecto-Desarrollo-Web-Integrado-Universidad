package com.utp.quesale.connections.infrastructure.persistence.jpa.adapters;

import com.utp.quesale.connections.domain.aggregates.Connection;
import com.utp.quesale.connections.domain.exceptions.ConnectionNotFoundException;
import com.utp.quesale.connections.domain.repositories.ConnectionRepository;
import com.utp.quesale.connections.domain.valueobjects.ConnectionStatus;
import com.utp.quesale.connections.infrastructure.persistence.jpa.entities.ConnectionJpaEntity;
import com.utp.quesale.connections.infrastructure.persistence.jpa.mappers.ConnectionPersistenceMapper;
import com.utp.quesale.connections.infrastructure.persistence.jpa.repositories.ConnectionJpaRepository;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@Transactional
public class ConnectionRepositoryAdapter implements ConnectionRepository {
    private final ConnectionJpaRepository repository;
    private final ConnectionPersistenceMapper mapper;
    private final EntityManager entityManager;

    public ConnectionRepositoryAdapter(ConnectionJpaRepository repository,
                                       ConnectionPersistenceMapper mapper,
                                       EntityManager entityManager) {
        this.repository = repository;
        this.mapper = mapper;
        this.entityManager = entityManager;
    }

    @Override
    public Connection save(Connection connection) {
        StudentJpaEntity requester = entityManager.getReference(
                StudentJpaEntity.class, connection.getRequesterId());
        StudentJpaEntity recipient = entityManager.getReference(
                StudentJpaEntity.class, connection.getRecipientId());
        ConnectionJpaEntity entity = repository.save(mapper.toEntity(connection, requester, recipient));
        return mapper.toDomain(entity);
    }

    @Override
    public Connection update(Connection connection) {
        if (connection.getId() == null || !repository.existsById(connection.getId())) {
            throw new ConnectionNotFoundException(connection.getId());
        }
        return save(connection);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Connection> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Connection> findAllByStudentId(Long studentId) {
        return repository.findAllByStudentId(studentId).stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Connection> findAllByStudentIdAndStatus(Long studentId, ConnectionStatus status) {
        return repository.findAllByStudentIdAndStatus(studentId, status).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBetweenWithStatuses(Long studentId,
                                             Long otherStudentId,
                                             Set<ConnectionStatus> statuses) {
        return repository.existsBetweenWithStatuses(studentId, otherStudentId, statuses);
    }
}
