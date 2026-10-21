package com.utp.quesale.connections.application;

import com.utp.quesale.connections.application.commands.AcceptConnectionCommand;
import com.utp.quesale.connections.application.commands.CancelConnectionCommand;
import com.utp.quesale.connections.application.commands.CreateConnectionCommand;
import com.utp.quesale.connections.application.commands.RejectConnectionCommand;
import com.utp.quesale.connections.application.dtos.ConnectionDto;
import com.utp.quesale.connections.application.acl.ConnectionsContextFacadeImpl;
import com.utp.quesale.connections.application.handlers.commands.ConnectionCommandHandler;
import com.utp.quesale.connections.application.handlers.queries.ListConnectionsQueryHandler;
import com.utp.quesale.connections.application.queries.ListConnectionsQuery;
import com.utp.quesale.connections.domain.aggregates.Connection;
import com.utp.quesale.connections.domain.exceptions.ConnectionNotFoundException;
import com.utp.quesale.connections.domain.exceptions.ConnectionOperationForbiddenException;
import com.utp.quesale.connections.domain.valueobjects.ConnectionStatus;
import com.utp.quesale.moderation.interfaces.acl.ModerationContextFacade;
import com.utp.quesale.profiles.interfaces.acl.ProfilesContextFacade;
import com.utp.quesale.shared.domain.events.DomainEventDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectionApplicationHandlersTest {

    private static final Long REQUESTER_ID = 11L;
    private static final Long RECIPIENT_ID = 12L;
    private static final Long THIRD_STUDENT_ID = 13L;
    private static final Long UNIVERSITY_ID = 5L;

    private InMemoryConnectionRepository repository;
    private TestProfilesFacade profiles;
    private TestModerationFacade moderation;
    private AtomicInteger dispatchedAggregates;
    private ConnectionCommandHandler commandHandler;
    private ListConnectionsQueryHandler listHandler;

    @BeforeEach
    void setUp() {
        repository = new InMemoryConnectionRepository();
        profiles = new TestProfilesFacade(Map.of(
                REQUESTER_ID, UNIVERSITY_ID,
                RECIPIENT_ID, UNIVERSITY_ID,
                THIRD_STUDENT_ID, 6L));
        moderation = new TestModerationFacade();
        dispatchedAggregates = new AtomicInteger();
        DomainEventDispatcher events = aggregate -> dispatchedAggregates.incrementAndGet();
        commandHandler = new ConnectionCommandHandler(repository, events, profiles, moderation);
        listHandler = new ListConnectionsQueryHandler(repository);
    }

    @Test
    void createsConnectionOnlyForStudentsInSameUniversityAndDispatchesEvent() {
        ConnectionDto created = commandHandler.handle(
                new CreateConnectionCommand(REQUESTER_ID, RECIPIENT_ID));

        assertEquals(REQUESTER_ID, created.requesterId());
        assertEquals(RECIPIENT_ID, created.recipientId());
        assertEquals(ConnectionStatus.PENDING, created.status());
        assertEquals(1, dispatchedAggregates.get());
        assertEquals(1, listHandler.handle(new ListConnectionsQuery(REQUESTER_ID, null)).size());
    }

    @Test
    void rejectsRequestsAcrossUniversitiesOrBetweenBlockedStudents() {
        assertThrows(ConnectionOperationForbiddenException.class,
                () -> commandHandler.handle(new CreateConnectionCommand(REQUESTER_ID, THIRD_STUDENT_ID)));
        moderation.block(REQUESTER_ID, RECIPIENT_ID);
        assertThrows(ConnectionOperationForbiddenException.class,
                () -> commandHandler.handle(new CreateConnectionCommand(REQUESTER_ID, RECIPIENT_ID)));
        assertEquals(0, dispatchedAggregates.get());
    }

    @Test
    void rejectsDuplicateActiveConnectionsInEitherDirection() {
        commandHandler.handle(new CreateConnectionCommand(REQUESTER_ID, RECIPIENT_ID));

        assertThrows(IllegalStateException.class,
                () -> commandHandler.handle(new CreateConnectionCommand(RECIPIENT_ID, REQUESTER_ID)));
        assertEquals(1, repository.findAllByStudentId(REQUESTER_ID).size());
    }

    @Test
    void acceptsConnectionOnlyByRecipientAndDispatchesAcceptanceEvent() {
        ConnectionDto created = commandHandler.handle(
                new CreateConnectionCommand(REQUESTER_ID, RECIPIENT_ID));

        ConnectionsContextFacadeImpl facade = new ConnectionsContextFacadeImpl(repository);
        assertFalse(facade.areConnected(REQUESTER_ID, RECIPIENT_ID));
        ConnectionDto accepted = commandHandler.handle(
                new AcceptConnectionCommand(created.id(), RECIPIENT_ID));

        assertEquals(ConnectionStatus.ACCEPTED, accepted.status());
        assertEquals(2, dispatchedAggregates.get());
        assertTrue(facade.areConnected(REQUESTER_ID, RECIPIENT_ID));
        assertFalse(facade.areConnected(null, RECIPIENT_ID));
    }

    @Test
    void rejectsResponseByNonRecipientAndMissingConnection() {
        ConnectionDto created = commandHandler.handle(
                new CreateConnectionCommand(REQUESTER_ID, RECIPIENT_ID));

        assertThrows(ConnectionOperationForbiddenException.class,
                () -> commandHandler.handle(new AcceptConnectionCommand(created.id(), REQUESTER_ID)));
        assertThrows(ConnectionNotFoundException.class,
                () -> commandHandler.handle(new AcceptConnectionCommand(999L, RECIPIENT_ID)));
    }

    @Test
    void rejectsAndCancelsRequestsByTheCorrectParticipant() {
        ConnectionDto rejected = commandHandler.handle(
                new CreateConnectionCommand(REQUESTER_ID, RECIPIENT_ID));
        assertEquals(ConnectionStatus.REJECTED, commandHandler.handle(
                new RejectConnectionCommand(rejected.id(), RECIPIENT_ID)).status());

        ConnectionDto cancelled = commandHandler.handle(
                new CreateConnectionCommand(REQUESTER_ID, RECIPIENT_ID));
        assertEquals(ConnectionStatus.CANCELLED, commandHandler.handle(
                new CancelConnectionCommand(cancelled.id(), REQUESTER_ID)).status());
        assertEquals(2, dispatchedAggregates.get());
    }

    @Test
    void listsConnectionsWithAnOptionalStatusFilter() {
        commandHandler.handle(new CreateConnectionCommand(REQUESTER_ID, RECIPIENT_ID));
        repository.save(new Connection(REQUESTER_ID, THIRD_STUDENT_ID));

        assertEquals(2, listHandler.handle(new ListConnectionsQuery(REQUESTER_ID, null)).size());
        assertEquals(2, listHandler.handle(
                new ListConnectionsQuery(REQUESTER_ID, ConnectionStatus.PENDING)).size());
        assertTrue(listHandler.handle(
                new ListConnectionsQuery(REQUESTER_ID, ConnectionStatus.ACCEPTED)).isEmpty());
    }

    private static class TestProfilesFacade implements ProfilesContextFacade {
        private final Map<Long, Long> universitiesByStudent;

        private TestProfilesFacade(Map<Long, Long> universitiesByStudent) {
            this.universitiesByStudent = Map.copyOf(universitiesByStudent);
        }

        @Override
        public boolean existsStudent(Long studentId) {
            return universitiesByStudent.containsKey(studentId);
        }

        @Override
        public Optional<Long> studentIdOfAccount(Long accountId) {
            return Optional.empty();
        }

        @Override
        public Optional<Long> universityOfStudent(Long studentId) {
            return Optional.ofNullable(universitiesByStudent.get(studentId));
        }
    }

    private static class TestModerationFacade implements ModerationContextFacade {
        private final Set<String> blockedPairs = new java.util.HashSet<>();

        @Override
        public boolean isBlocked(Long studentId, Long otherStudentId) {
            return blockedPairs.contains(pair(studentId, otherStudentId))
                    || blockedPairs.contains(pair(otherStudentId, studentId));
        }

        private void block(Long studentId, Long otherStudentId) {
            blockedPairs.add(pair(studentId, otherStudentId));
        }

        private String pair(Long studentId, Long otherStudentId) {
            return studentId + ":" + otherStudentId;
        }
    }

}
