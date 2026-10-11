package com.utp.quesale.iam.infrastructure.persistence.jpa.repositories;

import com.utp.quesale.iam.infrastructure.persistence.jpa.entities.UniversityJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityJpaRepository extends JpaRepository<UniversityJpaEntity, Long> {
    boolean existsByEmailDomainIgnoreCase(String emailDomain);
}
