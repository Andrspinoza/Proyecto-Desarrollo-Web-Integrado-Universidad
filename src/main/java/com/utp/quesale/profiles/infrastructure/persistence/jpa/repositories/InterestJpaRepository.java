package com.utp.quesale.profiles.infrastructure.persistence.jpa.repositories;

import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.InterestJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterestJpaRepository extends JpaRepository<InterestJpaEntity, Long> {
    boolean existsByNameIgnoreCase(String name);
}
