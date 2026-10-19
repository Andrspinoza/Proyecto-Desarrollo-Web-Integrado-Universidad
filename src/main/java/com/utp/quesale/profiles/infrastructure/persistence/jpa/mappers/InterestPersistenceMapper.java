package com.utp.quesale.profiles.infrastructure.persistence.jpa.mappers;

import com.utp.quesale.profiles.domain.aggregates.Interest;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.InterestJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class InterestPersistenceMapper {

    public Interest toDomain(InterestJpaEntity entity) {
        return new Interest(entity.getId(), entity.getName());
    }

    public InterestJpaEntity toEntity(Interest interest) {
        InterestJpaEntity entity = new InterestJpaEntity();
        entity.setId(interest.getId());
        entity.setName(interest.getName());
        return entity;
    }
}
