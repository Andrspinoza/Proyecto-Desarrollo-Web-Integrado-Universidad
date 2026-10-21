package com.utp.quesale.profiles.application;

import com.utp.quesale.iam.interfaces.acl.IamContextFacade;
import com.utp.quesale.profiles.application.commands.CreateStudentCommand;
import com.utp.quesale.profiles.application.dtos.StudentDto;
import com.utp.quesale.profiles.application.exceptions.StudentUniversityNotFoundException;
import com.utp.quesale.profiles.application.handlers.commands.CreateStudentCommandHandler;
import com.utp.quesale.profiles.domain.aggregates.Student;
import com.utp.quesale.profiles.domain.repositories.StudentRepository;
import com.utp.quesale.shared.domain.events.DomainEventDispatcher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateStudentCommandHandlerTest {
    private final StudentRepository repository = mock(StudentRepository.class);
    private final IamContextFacade iam = mock(IamContextFacade.class);
    private final DomainEventDispatcher events = mock(DomainEventDispatcher.class);
    private final CreateStudentCommandHandler handler = new CreateStudentCommandHandler(repository, iam, events);
    private final CreateStudentCommand command = new CreateStudentCommand(1L, "  ANA@UTP.EDU.PE  ", " Ana ", " Perez ");

    @Test
    void createsNormalizedStudentAndDispatchesAfterSaving() {
        acceptUniversityAndEmail();
        when(repository.save(any(Student.class))).thenAnswer(invocation -> {
            Student student = invocation.getArgument(0);
            return new Student(7L, student.getRecordId(), student.getUniversityId(), student.getEmail(),
                    student.getFullName(), student.getCreatedAt());
        });

        StudentDto dto = handler.handle(command);

        assertEquals(7L, dto.id());
        assertEquals(1L, dto.universityId());
        assertEquals("ana@utp.edu.pe", dto.email());
        assertEquals("Ana", dto.firstName());
        assertEquals("Perez", dto.lastName());
        assertNotNull(dto.recordId());
        assertNotNull(dto.createdAt());
        assertNull(dto.profile());
        var order = inOrder(iam, repository, events);
        order.verify(iam).isActiveUniversity(1L);
        order.verify(iam).universityAcceptsEmail(1L, "ana@utp.edu.pe");
        order.verify(repository).existsByEmail("ana@utp.edu.pe");
        order.verify(repository).save(any(Student.class));
        order.verify(events).dispatch(argThat(aggregate -> aggregate instanceof Student saved
                && Long.valueOf(7L).equals(saved.getId())));
    }

    @Test
    void rejectsMissingOrInactiveUniversity() {
        assertThrows(StudentUniversityNotFoundException.class, () -> handler.handle(command));

        verify(iam, never()).universityAcceptsEmail(any(), any());
        verifyNoInteractions(repository, events);
    }

    @Test
    void rejectsNonInstitutionalEmail() {
        when(iam.isActiveUniversity(1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));

        verifyNoInteractions(repository, events);
    }

    @Test
    void rejectsDuplicateEmail() {
        acceptUniversityAndEmail();
        when(repository.existsByEmail("ana@utp.edu.pe")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> handler.handle(command));

        verify(repository, never()).save(any());
        verifyNoInteractions(events);
    }

    @Test
    void rejectsInvalidDataBeforeCallingDependencies() {
        assertThrows(IllegalArgumentException.class,
                () -> handler.handle(new CreateStudentCommand(1L, "invalid", "Ana", "Perez")));
        assertThrows(IllegalArgumentException.class,
                () -> handler.handle(new CreateStudentCommand(1L, "ana@utp.edu.pe", " ", "Perez")));
        assertThrows(IllegalArgumentException.class,
                () -> handler.handle(new CreateStudentCommand(null, "ana@utp.edu.pe", "Ana", "Perez")));

        verifyNoInteractions(repository, iam, events);
    }

    @Test
    void doesNotDispatchWhenSavingFails() {
        acceptUniversityAndEmail();
        when(repository.save(any())).thenThrow(new IllegalStateException("Save failed"));

        assertThrows(IllegalStateException.class, () -> handler.handle(command));

        verifyNoInteractions(events);
    }

    private void acceptUniversityAndEmail() {
        when(iam.isActiveUniversity(1L)).thenReturn(true);
        when(iam.universityAcceptsEmail(1L, "ana@utp.edu.pe")).thenReturn(true);
    }
}
