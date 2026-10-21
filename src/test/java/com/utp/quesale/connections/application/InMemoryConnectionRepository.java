package com.utp.quesale.connections.application;

import com.utp.quesale.connections.domain.aggregates.Connection;
import com.utp.quesale.connections.domain.repositories.ConnectionRepository;
import com.utp.quesale.connections.domain.valueobjects.ConnectionStatus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

class InMemoryConnectionRepository implements ConnectionRepository {

    private final Map<Long, Connection> connections = new LinkedHashMap<>();
    private long nextId = 1;

    @Override
    public Connection save(Connection connection) {
        Long id = connection.getId() == null ? nextId++ : connection.getId();
        Connection saved = copy(id, connection);
        connections.put(id, saved);
        return copy(id, saved);
    }

    @Override
    public Connection update(Connection connection) {
        if (connection.getId() == null || !connections.containsKey(connection.getId())) {
            throw new IllegalArgumentException("Connection does not exist");
        }
        connections.put(connection.getId(), copy(connection.getId(), connection));
        return copy(connection.getId(), connection);
    }

    @Override
    public Optional<Connection> findById(Long id) {
        return Optional.ofNullable(connections.get(id)).map(connection -> copy(connection.getId(), connection));
    }

    @Override
    public List<Connection> findAllByStudentId(Long studentId) {
        return connections.values().stream()
                .filter(connection -> connection.getRequesterId().equals(studentId)
                        || connection.getRecipientId().equals(studentId))
                .map(connection -> copy(connection.getId(), connection))
                .toList();
    }

    @Override
    public List<Connection> findAllByStudentIdAndStatus(Long studentId, ConnectionStatus status) {
        return findAllByStudentId(studentId).stream()
                .filter(connection -> connection.getStatus() == status)
                .toList();
    }

    @Override
    public boolean existsBetweenWithStatuses(
            Long studentId, Long otherStudentId, Set<ConnectionStatus> statuses) {
        return connections.values().stream()
                .anyMatch(connection -> ((connection.getRequesterId().equals(studentId)
                        && connection.getRecipientId().equals(otherStudentId))
                        || (connection.getRequesterId().equals(otherStudentId)
                        && connection.getRecipientId().equals(studentId)))
                        && statuses.contains(connection.getStatus()));
    }

    private Connection copy(Long id, Connection connection) {
        return new Connection(
                id,
                connection.getRequesterId(),
                connection.getRecipientId(),
                connection.getStatus(),
                connection.getCreatedAt(),
                connection.getRespondedAt());
    }
}
