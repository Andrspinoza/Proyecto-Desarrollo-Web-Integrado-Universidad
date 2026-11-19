package com.utp.quesale.iam.infrastructure.persistence.jpa.repositories;

import com.utp.quesale.iam.infrastructure.persistence.jpa.entities.EmailVerificationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationJpaRepository extends JpaRepository<EmailVerificationJpaEntity, Long> {

    Optional<EmailVerificationJpaEntity> findByTokenHash(String tokenHash);

    Optional<EmailVerificationJpaEntity> findFirstByStudent_IdOrderByCreatedAtDescIdDesc(Long studentId);

    Optional<EmailVerificationJpaEntity> findFirstByStudent_IdAndVerifiedAtIsNotNull(Long studentId);
}
