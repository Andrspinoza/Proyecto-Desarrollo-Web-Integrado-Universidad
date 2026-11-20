package com.utp.quesale.profiles.application;

import com.utp.quesale.profiles.application.acl.ProfilesContextFacadeImpl;
import com.utp.quesale.profiles.domain.aggregates.Student;
import com.utp.quesale.profiles.domain.repositories.StudentRepository;
import com.utp.quesale.profiles.domain.valueobjects.FullName;
import com.utp.quesale.profiles.interfaces.acl.ProfilesContextFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfilesFacadeFirstNameTest {

    private final StudentRepository students = mock(StudentRepository.class);
    private final ProfilesContextFacade facade = new ProfilesContextFacadeImpl(students);

    @Test
    @DisplayName("Devuelve el nombre de pila del estudiante")
    void returnsFirstName() {
        when(students.findById(7L)).thenReturn(Optional.of(
                new Student(3L, "diego@utp.edu.pe", new FullName("Diego", "Ramos"))));

        assertEquals(Optional.of("Diego"), facade.firstNameOfStudent(7L));
    }

    @Test
    @DisplayName("Un estudiante inexistente no tiene nombre")
    void unknownStudentHasNoName() {
        when(students.findById(8L)).thenReturn(Optional.empty());

        assertTrue(facade.firstNameOfStudent(8L).isEmpty());
    }

    @Test
    @DisplayName("Un id inválido no consulta el repositorio")
    void invalidIdsDoNotQuery() {
        for (Long id : new Long[]{null, 0L, -1L}) {
            assertTrue(facade.firstNameOfStudent(id).isEmpty());
        }
        verifyNoInteractions(students);
    }
}
