package com.utp.quesale.profiles.application;

import com.utp.quesale.connections.interfaces.acl.ConnectionsContextFacade;
import com.utp.quesale.profiles.application.exceptions.StudentNotFoundException;
import com.utp.quesale.profiles.application.handlers.queries.GetStudentQueryHandler;
import com.utp.quesale.profiles.application.queries.GetStudentQuery;
import com.utp.quesale.profiles.domain.aggregates.Student;
import com.utp.quesale.profiles.domain.repositories.StudentRepository;
import com.utp.quesale.profiles.domain.valueobjects.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetStudentQueryHandlerTest {
    private final StudentRepository repository = mock(StudentRepository.class);
    private final ConnectionsContextFacade connections = mock(ConnectionsContextFacade.class);
    private final GetStudentQueryHandler handler = new GetStudentQueryHandler(repository, connections);

    @Test
    void ownerReceivesCompletePrivateProfile() {
        storeStudent(Visibility.PRIVATE);

        var dto = handler.handle(new GetStudentQuery(7L, 7L));

        assertEquals("Descripcion privada", dto.profile().description());
        assertEquals(Set.of(9L), dto.profile().interestIds());
        verifyNoInteractions(connections);
    }

    @Test
    void publicProfileIsCompleteWithoutCheckingConnections() {
        storeStudent(Visibility.PUBLIC);

        var dto = handler.handle(new GetStudentQuery(7L, 8L));

        assertEquals("Descripcion privada", dto.profile().description());
        verifyNoInteractions(connections);
    }

    @Test
    void connectionReceivesCompletePrivateProfile() {
        storeStudent(Visibility.PRIVATE);
        when(connections.areConnected(8L, 7L)).thenReturn(true);

        var dto = handler.handle(new GetStudentQuery(7L, 8L));

        assertEquals("Descripcion privada", dto.profile().description());
        assertEquals(Set.of(9L), dto.profile().interestIds());
    }

    @Test
    void strangerReceivesOnlyReducedPrivateProfile() {
        storeStudent(Visibility.PRIVATE);

        var dto = handler.handle(new GetStudentQuery(7L, 8L));

        assertEquals(7L, dto.id());
        assertEquals("Ana", dto.firstName());
        assertEquals("Perez", dto.lastName());
        assertEquals("Sistemas", dto.profile().career());
        assertEquals(6, dto.profile().cycle());
        assertNull(dto.email());
        assertNull(dto.recordId());
        assertNull(dto.createdAt());
        assertNull(dto.universityId());
        assertNull(dto.profile().description());
        assertNull(dto.profile().visibility());
        assertTrue(dto.profile().interestIds().isEmpty());
        assertTrue(dto.profile().skillIds().isEmpty());
    }

    @Test
    void handlesStudentWithoutProfileForOwnerAndStranger() {
        when(repository.findById(7L)).thenReturn(Optional.of(student()));

        var owner = handler.handle(new GetStudentQuery(7L, 7L));
        var stranger = handler.handle(new GetStudentQuery(7L, 8L));

        assertEquals("ana@utp.edu.pe", owner.email());
        assertNull(owner.profile());
        assertNull(stranger.email());
        assertNull(stranger.profile());
        verifyNoInteractions(connections);
    }

    @Test
    void missingStudentRaisesNotFoundWithoutCheckingConnections() {
        assertThrows(StudentNotFoundException.class, () -> handler.handle(new GetStudentQuery(7L, 8L)));
        verifyNoInteractions(connections);
    }

    @Test
    void invalidIdsAreRejectedBeforeCallingDependencies() {
        for (Long invalid : new Long[]{null, 0L, -1L}) {
            assertThrows(IllegalArgumentException.class, () -> handler.handle(new GetStudentQuery(invalid, 8L)));
            assertThrows(IllegalArgumentException.class, () -> handler.handle(new GetStudentQuery(7L, invalid)));
        }
        verifyNoInteractions(repository, connections);
    }

    private void storeStudent(Visibility visibility) {
        Student student = student();
        student.createProfile(new Career("Sistemas"), new StudyCycle(6), "Descripcion privada", visibility);
        student.addInterest(9L);
        when(repository.findById(7L)).thenReturn(Optional.of(student));
    }

    private Student student() {
        return new Student(7L, new StudentRecordId(), 1L, "ana@utp.edu.pe",
                new FullName("Ana", "Perez"), Instant.parse("2026-10-21T12:00:00Z"));
    }
}
