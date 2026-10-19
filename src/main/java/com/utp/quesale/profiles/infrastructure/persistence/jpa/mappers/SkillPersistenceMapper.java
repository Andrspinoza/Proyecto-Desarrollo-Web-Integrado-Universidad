package com.utp.quesale.profiles.infrastructure.persistence.jpa.mappers;

import com.utp.quesale.profiles.domain.aggregates.Skill;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.SkillJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class SkillPersistenceMapper {

    public Skill toDomain(SkillJpaEntity entity) {
        return new Skill(entity.getId(), entity.getName());
    }

    public SkillJpaEntity toEntity(Skill skill) {
        SkillJpaEntity entity = new SkillJpaEntity();
        entity.setId(skill.getId());
        entity.setName(skill.getName());
        return entity;
    }
}
