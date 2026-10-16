package com.utp.quesale.profiles.domain;

import com.utp.quesale.profiles.domain.aggregates.Student;
import com.utp.quesale.profiles.domain.valueobjects.FullName;
import com.utp.quesale.profiles.domain.valueobjects.StudentRecordId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class StudentTest {
    private final FullName name = new FullName("Fernando", "Escobedo");

    @Test
    void createsStudentWithNormalizedEmailAndGeneratedIdentity() {
        Instant before = Instant.now();
        Student student = new Student(1L, "  Fernando@UTP.edu.pe  ", name);

        assertNull(student.getId());
        assertNotNull(student.getRecordId());
        assertEquals(1L, student.getUniversityId());
        assertEquals("fernando@utp.edu.pe", student.getEmail());
        assertEquals(name, student.getFullName());
        assertFalse(student.getCreatedAt().isBefore(before));
        assertFalse(student.getCreatedAt().isAfter(Instant.now()));
    }

    @Test
    void rehydratesOriginalIdentityAndTimestampWithoutEvents() {
        StudentRecordId recordId = new StudentRecordId();
        Instant createdAt = Instant.parse("2026-10-01T12:00:00Z");
        Student student = new Student(7L, recordId, 1L, "fernando@utp.edu.pe", name, createdAt);

        assertEquals(7L, student.getId());
        assertEquals(recordId, student.getRecordId());
        assertEquals(createdAt, student.getCreatedAt());
        assertTrue(student.pullDomainEvents().isEmpty());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0, -1})
    void rejectsMissingOrInvalidUniversity(Long universityId) {
        assertThrows(IllegalArgumentException.class,
                () -> new Student(universityId, "fernando@utp.edu.pe", name));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "fernando", "@utp.edu.pe", "fernando@", "a@@utp.edu.pe", "a b@utp.edu.pe"})
    void rejectsMissingOrMalformedEmail(String email) {
        assertThrows(IllegalArgumentException.class, () -> new Student(1L, email, name));
    }

    @Test
    void rejectsMissingName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Student(1L, "fernando@utp.edu.pe", null));
    }

    @Test
    void rejectsMissingRecordIdWhenRehydrating() {
        assertThrows(IllegalArgumentException.class,
                () -> new Student(7L, null, 1L, "fernando@utp.edu.pe", name, Instant.now()));
    }

    @Test
    void rejectsMissingTimestampWhenRehydrating() {
        assertThrows(IllegalArgumentException.class,
                () -> new Student(7L, new StudentRecordId(), 1L, "fernando@utp.edu.pe", name, null));
    }
}
