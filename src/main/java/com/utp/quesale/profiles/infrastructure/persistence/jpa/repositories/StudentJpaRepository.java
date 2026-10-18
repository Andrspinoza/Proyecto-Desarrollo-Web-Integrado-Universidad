package com.utp.quesale.profiles.infrastructure.persistence.jpa.repositories;

import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentJpaRepository extends JpaRepository<StudentJpaEntity, Long> {
    boolean existsByEmailIgnoreCase(String email);
}
