package com.utp.quesale.profiles.domain;

import com.utp.quesale.profiles.domain.aggregates.Student;
import com.utp.quesale.profiles.domain.events.StudentRegisteredEvent;
import com.utp.quesale.profiles.domain.valueobjects.FullName;
import com.utp.quesale.profiles.domain.valueobjects.StudentRecordId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StudentRegistrationEventTest {

    private Student student(Long id) {
        return new Student(id, new StudentRecordId(), 1L, "camila@utp.edu.pe",
                new FullName("Camila", "Quispe"), Instant.now());
    }

    @Test
    @DisplayName("Registrar a un estudiante guardado emite el evento con su id, correo y nombre")
    void registeredStudentEmitsEvent() {
        Student student = student(9L);

        student.markAsRegistered();

        List<Object> events = student.pullDomainEvents();
        assertEquals(1, events.size());
        StudentRegisteredEvent event = assertInstanceOf(StudentRegisteredEvent.class, events.get(0));
        assertEquals(9L, event.studentId());
        assertEquals("camila@utp.edu.pe", event.email());
        assertEquals("Camila", event.firstName());
        assertNotNull(event.occurredAt());
        assertTrue(student.pullDomainEvents().isEmpty());
    }

    @Test
    @DisplayName("No se puede registrar a un estudiante que aún no tiene id")
    void unsavedStudentCannotBeRegistered() {
        assertThrows(IllegalStateException.class, () -> student(null).markAsRegistered());
    }
}
