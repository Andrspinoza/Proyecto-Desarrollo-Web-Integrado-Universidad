package com.utp.quesale.connections.infrastructure;

import com.utp.quesale.iam.infrastructure.persistence.jpa.entities.UniversityJpaEntity;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import jakarta.persistence.EntityManager;

import java.time.Instant;
import java.util.UUID;

final class ConnectionsPersistenceTestData {

    private ConnectionsPersistenceTestData() {
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
        StudentJpaEntity third = createStudent(university, "third");
        entityManager.persist(first);
        entityManager.persist(second);
        entityManager.persist(third);
        entityManager.flush();

        return new StudentIds(first.getId(), second.getId(), third.getId());
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

    record StudentIds(Long first, Long second, Long third) {
    }
}
