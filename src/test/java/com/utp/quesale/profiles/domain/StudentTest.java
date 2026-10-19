package com.utp.quesale.profiles.domain;

import com.utp.quesale.profiles.domain.aggregates.Student;
import com.utp.quesale.profiles.domain.entities.Profile;
import com.utp.quesale.profiles.domain.valueobjects.Career;
import com.utp.quesale.profiles.domain.valueobjects.StudyCycle;
import com.utp.quesale.profiles.domain.valueobjects.Visibility;
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

    private Student savedStudent() {
        return new Student(7L, new StudentRecordId(), 1L, "fernando@utp.edu.pe", name, Instant.now());
    }

    @Test
    void createsOnlyOneProfileAndUpdatesWithoutLosingCatalogs() {
        Student student = savedStudent();
        assertNull(student.getProfile());
        student.createProfile(new Career("Sistemas"), new StudyCycle(6), "Hola", Visibility.PUBLIC);
        student.addInterest(1L);
        student.addSkill(2L);
        assertThrows(IllegalStateException.class,
                () -> student.createProfile(new Career("Arquitectura"), new StudyCycle(1), "", Visibility.PRIVATE));

        student.updateProfile(new Career("Arquitectura"), new StudyCycle(7), "Actualizado", Visibility.PRIVATE);

        assertEquals(new Career("Arquitectura"), student.getProfile().getCareer());
        assertEquals(new StudyCycle(7), student.getProfile().getCycle());
        assertEquals("Actualizado", student.getProfile().getDescription());
        assertEquals(Visibility.PRIVATE, student.getProfile().getVisibility());
        assertTrue(student.getProfile().getInterestIds().contains(1L));
        assertTrue(student.getProfile().getSkillIds().contains(2L));
        student.removeInterest(1L);
        student.removeSkill(2L);
        assertTrue(student.getProfile().getInterestIds().isEmpty());
        assertTrue(student.getProfile().getSkillIds().isEmpty());
    }

    @Test
    void rejectsProfileOperationsBeforeCreation() {
        Student student = savedStudent();
        assertFalse(student.isVisibleTo(7L, false));
        assertThrows(IllegalStateException.class,
                () -> student.updateProfile(new Career("Sistemas"), new StudyCycle(6), "", Visibility.PUBLIC));
        assertThrows(IllegalStateException.class, () -> student.addInterest(1L));
        assertThrows(IllegalStateException.class, () -> student.removeInterest(1L));
        assertThrows(IllegalStateException.class, () -> student.addSkill(1L));
        assertThrows(IllegalStateException.class, () -> student.removeSkill(1L));
    }

    @Test
    void privateProfileIsVisibleOnlyToOwnerOrConnections() {
        Student student = savedStudent();
        student.createProfile(new Career("Sistemas"), new StudyCycle(6), "", Visibility.PRIVATE);

        assertTrue(student.isVisibleTo(7L, false));
        assertTrue(student.isVisibleTo(8L, true));
        assertFalse(student.isVisibleTo(8L, false));
        assertFalse(student.isVisibleTo(null, false));
    }

    @Test
    void publicProfileIsVisibleToOtherStudents() {
        Student student = savedStudent();
        student.createProfile(new Career("Sistemas"), new StudyCycle(6), "", Visibility.PUBLIC);
        assertTrue(student.isVisibleTo(8L, false));
    }

    @Test
    void unsavedStudentDoesNotTreatNullViewerAsOwner() {
        Student student = new Student(1L, "fernando@utp.edu.pe", name);
        student.createProfile(new Career("Sistemas"), new StudyCycle(6), "", Visibility.PRIVATE);
        assertFalse(student.isVisibleTo(null, false));
    }

    @Test
    void rehydratesStudentWithExistingProfileWithoutEvents() {
        Profile profile = new Profile(new Career("Sistemas"), new StudyCycle(6), "", Visibility.PRIVATE);
        profile.addInterest(1L);
        Student student = new Student(7L, new StudentRecordId(), 1L, "fernando@utp.edu.pe", name,
                Instant.parse("2026-10-01T12:00:00Z"), profile);

        assertEquals(profile.getCareer(), student.getProfile().getCareer());
        assertTrue(student.getProfile().getInterestIds().contains(1L));
        assertTrue(student.pullDomainEvents().isEmpty());
    }

    @Test
    void enforcesCatalogLimitsThroughAggregate() {
        Student student = savedStudent();
        student.createProfile(new Career("Sistemas"), new StudyCycle(6), "", Visibility.PUBLIC);
        for (long id = 1; id <= 15; id++) {
            student.addInterest(id);
            student.addSkill(id);
        }
        assertThrows(IllegalStateException.class, () -> student.addInterest(16L));
        assertThrows(IllegalStateException.class, () -> student.addSkill(16L));
        assertThrows(IllegalStateException.class, () -> student.addInterest(1L));
        assertThrows(IllegalStateException.class, () -> student.addSkill(1L));
    }

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
