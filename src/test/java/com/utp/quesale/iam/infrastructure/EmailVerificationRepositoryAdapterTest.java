package com.utp.quesale.iam.infrastructure;

import com.utp.quesale.iam.domain.aggregates.EmailVerification;
import com.utp.quesale.iam.infrastructure.persistence.jpa.adapters.EmailVerificationRepositoryAdapter;
import com.utp.quesale.iam.infrastructure.persistence.jpa.entities.UniversityJpaEntity;
import com.utp.quesale.iam.infrastructure.persistence.jpa.mappers.EmailVerificationPersistenceMapper;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({EmailVerificationRepositoryAdapter.class, EmailVerificationPersistenceMapper.class})
class EmailVerificationRepositoryAdapterTest {

    private static final Instant NOW = Instant.parse("2026-11-18T12:00:00Z");

    @Autowired
    private EmailVerificationRepositoryAdapter repository;

    @Autowired
    private EntityManager entityManager;

    private Long studentId;

    @BeforeEach
    void setUp() {
        UniversityJpaEntity university = new UniversityJpaEntity();
        university.setName("Universidad Tecnológica del Perú");
        university.setEmailDomain("utp.edu.pe");
        university.setActive(true);
        university.setCreatedAt(NOW);
        entityManager.persist(university);

        StudentJpaEntity student = new StudentJpaEntity();
        student.setRecordId(UUID.randomUUID().toString());
        student.setUniversity(university);
        student.setEmail("camila@utp.edu.pe");
        student.setFirstName("Camila");
        student.setLastName("Quispe");
        student.setCreatedAt(NOW);
        entityManager.persist(student);
        entityManager.flush();
        studentId = student.getId();
    }

    private EmailVerification issue(String tokenHash, Instant at) {
        return EmailVerification.issue(studentId, "camila@utp.edu.pe", tokenHash, Duration.ofHours(24), at);
    }

    @Test
    @DisplayName("Se guarda con el estudiante y se recupera por el hash del token")
    void savesAndFindsByTokenHash() {
        EmailVerification saved = repository.save(issue("hash-1", NOW));

        assertNotNull(saved.getId());
        EmailVerification found = repository.findByTokenHash("hash-1").orElseThrow();
        assertEquals(studentId, found.getStudentId());
        assertEquals("camila@utp.edu.pe", found.getEmail());
        assertEquals(NOW.plus(Duration.ofHours(24)), found.getExpiresAt());
        assertFalse(found.isVerified());
        assertTrue(repository.findByTokenHash("otro").isEmpty());
    }

    @Test
    @DisplayName("Confirmar y guardar de nuevo actualiza la misma fila")
    void confirmingUpdatesTheSameRow() {
        EmailVerification saved = repository.save(issue("hash-1", NOW));
        saved.confirm(NOW.plusSeconds(30));
        EmailVerification updated = repository.save(saved);
        entityManager.flush();
        entityManager.clear();

        assertEquals(saved.getId(), updated.getId());
        EmailVerification verified = repository.findVerifiedByStudentId(studentId).orElseThrow();
        assertEquals(NOW.plusSeconds(30), verified.getVerifiedAt());
        assertTrue(verified.isVerified());
    }

    @Test
    @DisplayName("El último es el emitido más recientemente")
    void latestIsTheMostRecentlyIssued() {
        repository.save(issue("hash-1", NOW));
        repository.save(issue("hash-2", NOW.plusSeconds(120)));
        entityManager.flush();
        entityManager.clear();

        assertEquals("hash-2", repository.findLatestByStudentId(studentId).orElseThrow().getTokenHash());
        assertTrue(repository.findLatestByStudentId(studentId + 100).isEmpty());
    }

    @Test
    @DisplayName("Sin confirmar no aparece como verificado")
    void pendingIsNotVerified() {
        repository.save(issue("hash-1", NOW));

        assertTrue(repository.findVerifiedByStudentId(studentId).isEmpty());
    }
}
