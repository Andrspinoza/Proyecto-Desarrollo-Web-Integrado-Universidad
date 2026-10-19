package com.utp.quesale.profiles.infrastructure.persistence.jpa.repositories;

import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.SkillJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillJpaRepository extends JpaRepository<SkillJpaEntity, Long> {
    boolean existsByNameIgnoreCase(String name);
}
