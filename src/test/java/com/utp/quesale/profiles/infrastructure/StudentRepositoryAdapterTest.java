package com.utp.quesale.profiles.infrastructure;

import com.utp.quesale.iam.domain.aggregates.University;
import com.utp.quesale.iam.domain.valueobjects.EmailDomain;
import com.utp.quesale.iam.infrastructure.persistence.jpa.adapters.UniversityRepositoryAdapter;
import com.utp.quesale.iam.infrastructure.persistence.jpa.mappers.UniversityPersistenceMapper;
import com.utp.quesale.profiles.domain.aggregates.Student;
import com.utp.quesale.profiles.domain.valueobjects.FullName;
import com.utp.quesale.profiles.domain.valueobjects.StudentRecordId;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.adapters.StudentRepositoryAdapter;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.mappers.StudentPersistenceMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({StudentRepositoryAdapter.class, StudentPersistenceMapper.class,
        UniversityRepositoryAdapter.class, UniversityPersistenceMapper.class})
class StudentRepositoryAdapterTest {
    @Autowired
    private StudentRepositoryAdapter studentRepository;

    @Autowired
    private UniversityRepositoryAdapter universityRepository;

    @Autowired
    private EntityManager entityManager;

    private Long createUniversity() {
        return universityRepository.save(new University("UTP", new EmailDomain("utp.edu.pe"))).getId();
    }

    @Test
    void savesAndReadsAllStudentData() {
        Long universityId = createUniversity();
        Student original = new Student(null, new StudentRecordId(), universityId, "fernando@utp.edu.pe",
                new FullName("Bruno Fernando", "Escobedo"), Instant.parse("2026-10-01T12:00:00Z"));
        Student saved = studentRepository.save(original);
        entityManager.flush();
        entityManager.clear();

        Student found = studentRepository.findById(saved.getId()).orElseThrow();

        assertNotNull(saved.getId());
        assertEquals(saved.getId(), found.getId());
        assertEquals(original.getRecordId(), found.getRecordId());
        assertEquals(universityId, found.getUniversityId());
        assertEquals(original.getEmail(), found.getEmail());
        assertEquals(original.getFullName(), found.getFullName());
        assertEquals(original.getCreatedAt(), found.getCreatedAt());
        assertTrue(found.pullDomainEvents().isEmpty());
    }

    @Test
    void findsEmailIgnoringCase() {
        studentRepository.save(new Student(createUniversity(), "fernando@utp.edu.pe",
                new FullName("Fernando", "Escobedo")));

        assertTrue(studentRepository.existsByEmail("FERNANDO@UTP.EDU.PE"));
        assertFalse(studentRepository.existsByEmail("otro@utp.edu.pe"));
    }

    @Test
    void listsStudentsAndChecksTheirExistence() {
        Long universityId = createUniversity();
        Student first = studentRepository.save(new Student(universityId, "primero@utp.edu.pe",
                new FullName("Primer", "Estudiante")));
        Student second = studentRepository.save(new Student(universityId, "segundo@utp.edu.pe",
                new FullName("Segundo", "Estudiante")));

        assertEquals(2, studentRepository.findAll().size());
        assertTrue(studentRepository.existsById(first.getId()));
        assertTrue(studentRepository.existsById(second.getId()));
        assertFalse(studentRepository.existsById(Long.MAX_VALUE));
        assertTrue(studentRepository.findById(Long.MAX_VALUE).isEmpty());
    }

    @Test
    void updatesWithoutCreatingAnotherStudent() {
        Student saved = studentRepository.save(new Student(createUniversity(), "fernando@utp.edu.pe",
                new FullName("Fernando", "Escobedo")));
        Student changed = new Student(saved.getId(), saved.getRecordId(), saved.getUniversityId(),
                saved.getEmail(), new FullName("Bruno Fernando", "Escobedo"), saved.getCreatedAt());

        studentRepository.update(changed);
        entityManager.flush();
        entityManager.clear();
        Student found = studentRepository.findById(saved.getId()).orElseThrow();

        assertEquals(changed.getFullName(), found.getFullName());
        assertEquals(saved.getRecordId(), found.getRecordId());
        assertEquals(1, studentRepository.findAll().size());
    }

    @Test
    void rejectsUnknownUniversity() {
        Student student = new Student(Long.MAX_VALUE, "fernando@utp.edu.pe",
                new FullName("Fernando", "Escobedo"));

        assertThrows(IllegalArgumentException.class, () -> studentRepository.save(student));
        assertTrue(studentRepository.findAll().isEmpty());
    }

    @Test
    void rejectsDuplicateEmail() {
        Long universityId = createUniversity();
        studentRepository.save(new Student(universityId, "fernando@utp.edu.pe",
                new FullName("Fernando", "Escobedo")));
        Student duplicate = new Student(universityId, "fernando@utp.edu.pe",
                new FullName("Otro", "Estudiante"));

        assertThrows(DataIntegrityViolationException.class, () -> studentRepository.save(duplicate));
    }
}
