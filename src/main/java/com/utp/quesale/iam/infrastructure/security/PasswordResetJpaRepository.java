package com.utp.quesale.iam.infrastructure.security;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetJpaRepository extends JpaRepository<PasswordResetEntity, Long> {
    Optional<PasswordResetEntity> findByTokenHash(String tokenHash);

    List<PasswordResetEntity> findByStudentIdAndUsedAtIsNull(Long studentId);
}
