package com.utp.quesale.connections.domain.repositories;

import com.utp.quesale.connections.domain.aggregates.Connection;
import com.utp.quesale.connections.domain.valueobjects.ConnectionStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ConnectionRepository {
    Connection save(Connection connection);

    Connection update(Connection connection);

    Optional<Connection> findById(Long id);

    List<Connection> findAllByStudentId(Long studentId);

    List<Connection> findAllByStudentIdAndStatus(Long studentId, ConnectionStatus status);

    boolean existsBetweenWithStatuses(Long studentId, Long otherStudentId, Set<ConnectionStatus> statuses);
}
