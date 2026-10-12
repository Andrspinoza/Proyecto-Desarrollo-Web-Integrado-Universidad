package com.utp.quesale.iam.infrastructure.persistence.jpa.mappers;

import com.utp.quesale.iam.domain.aggregates.University;
import com.utp.quesale.iam.domain.valueobjects.EmailDomain;
import com.utp.quesale.iam.infrastructure.persistence.jpa.entities.UniversityJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UniversityPersistenceMapper {

    public University toDomain(UniversityJpaEntity entity) {
        return new University(
                entity.getId(),
                entity.getName(),
                new EmailDomain(entity.getEmailDomain()),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }

    public UniversityJpaEntity toEntity(University university) {
        UniversityJpaEntity entity = new UniversityJpaEntity();
        entity.setId(university.getId());
        entity.setName(university.getName());
        entity.setEmailDomain(university.getEmailDomain().value());
        entity.setActive(university.isActive());
        entity.setCreatedAt(university.getCreatedAt());
        return entity;
    }
}
