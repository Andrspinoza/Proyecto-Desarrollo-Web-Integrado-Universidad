package com.utp.quesale.connections.application;

import com.utp.quesale.connections.application.acl.ConnectionsContextFacadeImpl;
import com.utp.quesale.connections.domain.aggregates.Connection;
import com.utp.quesale.connections.domain.repositories.ConnectionRepository;
import com.utp.quesale.connections.domain.valueobjects.ConnectionStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RelatedStudentIdsTest {
    private final ConnectionRepository repository = mock(ConnectionRepository.class);
    private final ConnectionsContextFacadeImpl facade = new ConnectionsContextFacadeImpl(repository);

    private static Connection connection(long id, long requester, long recipient, ConnectionStatus status) {
        Instant now = Instant.parse("2026-11-20T10:00:00Z");
        return new Connection(id, requester, recipient, status, now,
                status == ConnectionStatus.PENDING ? null : now.plusSeconds(60));
    }

    @Test void returnsTheOtherSideOfAcceptedAndPendingConnectionsInBothDirections() {
        when(repository.findAllByStudentId(1L)).thenReturn(List.of(
                connection(1, 1, 2, ConnectionStatus.PENDING),
                connection(2, 3, 1, ConnectionStatus.ACCEPTED),
                connection(3, 1, 4, ConnectionStatus.REJECTED),
                connection(4, 5, 1, ConnectionStatus.CANCELLED)));
        assertEquals(Set.of(2L, 3L), facade.relatedStudentIds(1L));
    }

    @Test void anInvalidIdOrNoConnectionsGivesAnEmptySet() {
        when(repository.findAllByStudentId(9L)).thenReturn(List.of());
        assertTrue(facade.relatedStudentIds(9L).isEmpty());
        assertTrue(facade.relatedStudentIds(null).isEmpty());
        assertTrue(facade.relatedStudentIds(0L).isEmpty());
    }
}
