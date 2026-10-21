package com.utp.quesale.connections.application.acl;

import com.utp.quesale.connections.domain.repositories.ConnectionRepository;
import com.utp.quesale.connections.domain.valueobjects.ConnectionStatus;
import com.utp.quesale.connections.interfaces.acl.ConnectionsContextFacade;
import org.springframework.stereotype.Service;

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
}
