package com.utp.quesale.connections.application.acl;

import com.utp.quesale.connections.domain.repositories.ConnectionRepository;
import com.utp.quesale.connections.domain.valueobjects.ConnectionStatus;
import com.utp.quesale.connections.interfaces.acl.ConnectionsContextFacade;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class ConnectionsContextFacadeImpl implements ConnectionsContextFacade {

    private final ConnectionRepository connectionRepository;

    public ConnectionsContextFacadeImpl(ConnectionRepository connectionRepository) {
        this.connectionRepository = connectionRepository;
    }

    @Override
    public boolean areConnected(Long studentId, Long otherStudentId) {
        if (studentId == null || otherStudentId == null || studentId <= 0 || otherStudentId <= 0) {
            return false;
        }
        return connectionRepository.existsBetweenWithStatuses(
                studentId, otherStudentId, Set.of(ConnectionStatus.ACCEPTED));
    }

    @Override
    public Set<Long> relatedStudentIds(Long studentId) {
        Set<Long> related = new HashSet<>();
        if (studentId == null || studentId <= 0) return related;
        connectionRepository.findAllByStudentId(studentId).stream()
                .filter(connection -> connection.getStatus() == ConnectionStatus.ACCEPTED
                        || connection.getStatus() == ConnectionStatus.PENDING)
                .forEach(connection -> related.add(connection.getRequesterId().equals(studentId)
                        ? connection.getRecipientId() : connection.getRequesterId()));
        return related;
    }
}
