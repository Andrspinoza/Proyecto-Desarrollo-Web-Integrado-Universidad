package com.utp.quesale.profiles.infrastructure.persistence.jpa.mappers;

import com.utp.quesale.iam.infrastructure.persistence.jpa.entities.UniversityJpaEntity;
import com.utp.quesale.profiles.domain.aggregates.Student;
import com.utp.quesale.profiles.domain.valueobjects.FullName;
import com.utp.quesale.profiles.domain.valueobjects.StudentRecordId;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class StudentPersistenceMapper {
    public Student toDomain(StudentJpaEntity entity) {
        return new Student(
                entity.getId(),
                new StudentRecordId(entity.getRecordId()),
                entity.getUniversity().getId(),
                entity.getEmail(),
                new FullName(entity.getFirstName(), entity.getLastName()),
                entity.getCreatedAt()
        );
    }

    public StudentJpaEntity toEntity(Student student, UniversityJpaEntity university) {
        StudentJpaEntity entity = new StudentJpaEntity();
        entity.setId(student.getId());
        entity.setRecordId(student.getRecordId().studentRecordId());
        entity.setUniversity(university);
        entity.setEmail(student.getEmail());
        entity.setFirstName(student.getFullName().firstName());
        entity.setLastName(student.getFullName().lastName());
        entity.setCreatedAt(student.getCreatedAt());
        return entity;
    }
}
