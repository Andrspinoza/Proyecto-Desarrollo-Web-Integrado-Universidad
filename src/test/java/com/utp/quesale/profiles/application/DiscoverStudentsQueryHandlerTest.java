package com.utp.quesale.profiles.application;

import com.utp.quesale.iam.interfaces.acl.IamContextFacade;
import com.utp.quesale.moderation.interfaces.acl.ModerationContextFacade;
import com.utp.quesale.profiles.application.handlers.queries.DiscoverStudentsQueryHandler;
import com.utp.quesale.profiles.domain.aggregates.Student;
import com.utp.quesale.profiles.domain.repositories.StudentRepository;
import com.utp.quesale.profiles.domain.valueobjects.FullName;
import com.utp.quesale.profiles.domain.valueobjects.StudentRecordId;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiscoverStudentsQueryHandlerTest {
    @Test void limitsResultsToVerifiedUnblockedStudentsOfSameUniversity() {
        var repository = mock(StudentRepository.class);
        var moderation = mock(ModerationContextFacade.class);
        var iam = mock(IamContextFacade.class);
        var viewer = student(1L, 1L, "Andres");
        when(repository.findById(1L)).thenReturn(Optional.of(viewer));
        when(repository.findAll()).thenReturn(List.of(viewer, student(2L, 1L, "Maria"),
                student(3L, 2L, "Maria"), student(4L, 1L, "Maria"), student(5L, 1L, "Maria")));
        when(iam.isEmailVerified(2L)).thenReturn(true);
        when(iam.isEmailVerified(5L)).thenReturn(true);
        when(moderation.isBlocked(1L, 5L)).thenReturn(true);
        var results = new DiscoverStudentsQueryHandler(repository, moderation, iam).handle(1L, "mArIa");
        assertEquals(List.of(2L), results.stream().map(DiscoverStudentsQueryHandler.Result::id).toList());
        verify(iam, never()).isEmailVerified(3L);
    }

    private Student student(Long id, Long university, String name) {
        return new Student(id, new StudentRecordId(), university, "a" + id + "@uni.edu.pe", new FullName(name, "Apellido"), Instant.now());
    }
}
