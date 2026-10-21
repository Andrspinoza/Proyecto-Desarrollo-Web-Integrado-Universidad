package com.utp.quesale.profiles.application;

import com.utp.quesale.profiles.application.acl.ProfilesContextFacadeImpl;
import com.utp.quesale.profiles.domain.aggregates.Student;
import com.utp.quesale.profiles.domain.repositories.StudentRepository;
import com.utp.quesale.profiles.domain.valueobjects.FullName;
import com.utp.quesale.profiles.interfaces.acl.ProfilesContextFacade;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfilesContextFacadeImplTest {
    private final StudentRepository students = mock(StudentRepository.class);
    private final ProfilesContextFacade facade = new ProfilesContextFacadeImpl(students);

    @Test
    void checksStudentExistence() {
        when(students.existsById(7L)).thenReturn(true);

        assertTrue(facade.existsStudent(7L));
        assertFalse(facade.existsStudent(8L));
    }

    @Test
    void findsUniversityThroughStudent() {
        when(students.findById(7L)).thenReturn(Optional.of(
                new Student(3L, "estudiante@utp.edu.pe", new FullName("Estudiante", "Prueba"))));
        when(students.findById(8L)).thenReturn(Optional.empty());

        assertEquals(Optional.of(3L), facade.universityOfStudent(7L));
        assertTrue(facade.universityOfStudent(8L).isEmpty());
    }

    @Test
    void invalidStudentIdsDoNotQueryRepository() {
        for (Long id : new Long[]{null, 0L, -1L}) {
            assertFalse(facade.existsStudent(id));
            assertTrue(facade.universityOfStudent(id).isEmpty());
        }
        verifyNoInteractions(students);
    }

    @Test
    void doesNotTreatAccountIdAsStudentId() {
        assertTrue(facade.studentIdOfAccount(7L).isEmpty());
        assertTrue(facade.studentIdOfAccount(null).isEmpty());
        verifyNoInteractions(students);
    }

    @Test
    void registersInjectableFacadeBean() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(StudentRepository.class, () -> students);
            context.scan("com.utp.quesale.profiles.application.acl");
            context.refresh();

            assertInstanceOf(ProfilesContextFacadeImpl.class, context.getBean(ProfilesContextFacade.class));
        }
    }
}
