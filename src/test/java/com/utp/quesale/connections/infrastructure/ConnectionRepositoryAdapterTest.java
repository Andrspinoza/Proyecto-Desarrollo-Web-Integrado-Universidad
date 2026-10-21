package com.utp.quesale.connections.infrastructure;

import com.utp.quesale.connections.domain.aggregates.Connection;
import com.utp.quesale.connections.domain.exceptions.ConnectionNotFoundException;
import com.utp.quesale.connections.domain.valueobjects.ConnectionStatus;
import com.utp.quesale.connections.infrastructure.persistence.jpa.adapters.ConnectionRepositoryAdapter;
import com.utp.quesale.connections.infrastructure.persistence.jpa.mappers.ConnectionPersistenceMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import({ConnectionRepositoryAdapter.class, ConnectionPersistenceMapper.class})
class ConnectionRepositoryAdapterTest {

    @Autowired
    private ConnectionRepositoryAdapter connectionRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesFindsAndListsConnectionsForEitherParticipant() {
        ConnectionsPersistenceTestData.StudentIds students =
                ConnectionsPersistenceTestData.createStudents(entityManager);
        Connection saved = connectionRepository.save(new Connection(students.first(), students.second()));

        Connection found = connectionRepository.findById(saved.getId()).orElseThrow();

        assertEquals(students.first(), found.getRequesterId());
        assertEquals(students.second(), found.getRecipientId());
        assertEquals(ConnectionStatus.PENDING, found.getStatus());
        assertEquals(saved.getCreatedAt(), found.getCreatedAt());
        assertEquals(1, connectionRepository.findAllByStudentId(students.first()).size());
        assertEquals(1, connectionRepository.findAllByStudentId(students.second()).size());
        assertTrue(connectionRepository.findAllByStudentId(students.third()).isEmpty());
    }

    @Test
    void updatesConnectionAndFiltersByStatus() {
        ConnectionsPersistenceTestData.StudentIds students =
                ConnectionsPersistenceTestData.createStudents(entityManager);
        Connection saved = connectionRepository.save(new Connection(students.first(), students.second()));
        saved.accept(students.second());

        Connection updated = connectionRepository.update(saved);

        assertEquals(ConnectionStatus.ACCEPTED, updated.getStatus());
        assertEquals(saved.getRespondedAt(), updated.getRespondedAt());
        assertTrue(connectionRepository.findAllByStudentIdAndStatus(
                students.first(), ConnectionStatus.ACCEPTED).stream()
                .anyMatch(connection -> connection.getId().equals(saved.getId())));
        assertTrue(connectionRepository.findAllByStudentIdAndStatus(
                students.first(), ConnectionStatus.PENDING).isEmpty());
    }

    @Test
    void detectsConnectionsInEitherDirectionOnlyForRequestedStatuses() {
        ConnectionsPersistenceTestData.StudentIds students =
                ConnectionsPersistenceTestData.createStudents(entityManager);
        connectionRepository.save(new Connection(students.first(), students.second()));

        assertTrue(connectionRepository.existsBetweenWithStatuses(
                students.second(), students.first(), Set.of(ConnectionStatus.PENDING)));
        assertFalse(connectionRepository.existsBetweenWithStatuses(
                students.second(), students.first(), Set.of(ConnectionStatus.ACCEPTED)));
        assertFalse(connectionRepository.existsBetweenWithStatuses(
                students.first(), students.third(), Set.of(ConnectionStatus.PENDING)));
    }

    @Test
    void rejectsUpdatingAnUnsavedConnection() {
        Connection unsaved = new Connection(1L, 2L);

        assertThrows(ConnectionNotFoundException.class, () -> connectionRepository.update(unsaved));
    }
}
