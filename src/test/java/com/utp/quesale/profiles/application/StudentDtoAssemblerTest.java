package com.utp.quesale.profiles.application;

import com.utp.quesale.profiles.application.assemblers.ProfilesDtoAssembler;
import com.utp.quesale.profiles.application.dtos.StudentDto;
import com.utp.quesale.profiles.domain.aggregates.Student;
import com.utp.quesale.profiles.domain.valueobjects.Career;
import com.utp.quesale.profiles.domain.valueobjects.FullName;
import com.utp.quesale.profiles.domain.valueobjects.StudyCycle;
import com.utp.quesale.profiles.domain.valueobjects.Visibility;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StudentDtoAssemblerTest {
    @Test
    void mapsCompleteStudentAndProfile() {
        Student student = studentWithProfile();

        StudentDto dto = ProfilesDtoAssembler.toDto(student);

        assertEquals(student.getRecordId().studentRecordId(), dto.recordId());
        assertEquals(1L, dto.universityId());
        assertEquals("student@utp.edu.pe", dto.email());
        assertEquals("Ana", dto.firstName());
        assertEquals("Perez", dto.lastName());
        assertEquals(student.getCreatedAt(), dto.createdAt());
        assertEquals("Sistemas", dto.profile().career());
        assertEquals(6, dto.profile().cycle());
        assertEquals("Descripcion privada", dto.profile().description());
        assertEquals(Visibility.PRIVATE, dto.profile().visibility());
        assertEquals(Set.of(7L), dto.profile().interestIds());
        assertEquals(Set.of(9L), dto.profile().skillIds());
    }

    @Test
    void reducedViewOmitsPrivateData() {
        StudentDto dto = ProfilesDtoAssembler.toReducedDto(studentWithProfile());

        assertEquals("Ana", dto.firstName());
        assertEquals("Perez", dto.lastName());
        assertEquals("Sistemas", dto.profile().career());
        assertEquals(6, dto.profile().cycle());
        assertNull(dto.recordId());
        assertNull(dto.universityId());
        assertNull(dto.email());
        assertNull(dto.createdAt());
        assertNull(dto.profile().description());
        assertNull(dto.profile().visibility());
        assertTrue(dto.profile().interestIds().isEmpty());
        assertTrue(dto.profile().skillIds().isEmpty());
    }

    @Test
    void mapsStudentWithoutProfileInBothViews() {
        Student student = new Student(1L, "student@utp.edu.pe", new FullName("Ana", "Perez"));

        assertNull(ProfilesDtoAssembler.toDto(student).profile());
        assertNull(ProfilesDtoAssembler.toReducedDto(student).profile());
    }

    @Test
    void dtoCollectionsAreImmutableSnapshots() {
        Student student = studentWithProfile();
        StudentDto dto = ProfilesDtoAssembler.toDto(student);

        student.addInterest(8L);
        student.addSkill(10L);

        assertEquals(Set.of(7L), dto.profile().interestIds());
        assertEquals(Set.of(9L), dto.profile().skillIds());
        assertThrows(UnsupportedOperationException.class, () -> dto.profile().interestIds().add(11L));
        assertThrows(UnsupportedOperationException.class, () -> dto.profile().skillIds().add(12L));
    }

    private Student studentWithProfile() {
        Student student = new Student(1L, "student@utp.edu.pe", new FullName("Ana", "Perez"));
        student.createProfile(new Career("Sistemas"), new StudyCycle(6),
                "Descripcion privada", Visibility.PRIVATE);
        student.addInterest(7L);
        student.addSkill(9L);
        return student;
    }
}
