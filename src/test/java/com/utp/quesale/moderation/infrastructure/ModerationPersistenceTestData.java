package com.utp.quesale.moderation.infrastructure;

import com.utp.quesale.iam.infrastructure.persistence.jpa.entities.UniversityJpaEntity;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import jakarta.persistence.EntityManager;

import java.time.Instant;
import java.util.UUID;

final class ModerationPersistenceTestData {

    private ModerationPersistenceTestData() {
    }

    static StudentIds createStudents(EntityManager entityManager) {
        UniversityJpaEntity university = new UniversityJpaEntity();
        university.setName("Test University " + UUID.randomUUID());
        university.setEmailDomain(UUID.randomUUID() + ".test.edu");
        university.setActive(true);
        university.setCreatedAt(Instant.now());
        entityManager.persist(university);

        StudentJpaEntity first = createStudent(university, "first");
        StudentJpaEntity second = createStudent(university, "second");
        entityManager.persist(first);
        entityManager.persist(second);
        entityManager.flush();

        return new StudentIds(first.getId(), second.getId());
    }

    private static StudentJpaEntity createStudent(UniversityJpaEntity university, String name) {
        StudentJpaEntity student = new StudentJpaEntity();
        student.setRecordId(UUID.randomUUID().toString());
        student.setUniversity(university);
        student.setEmail(name + "-" + UUID.randomUUID() + "@test.edu");
        student.setFirstName(name);
        student.setLastName("Student");
        student.setCreatedAt(Instant.now());
        return student;
    }

    record StudentIds(Long first, Long second) {
    }
}
