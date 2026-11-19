package com.utp.quesale.iam.infrastructure.security;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, Long> {
    Optional<AccountEntity> findByEmailIgnoreCase(String email);
    boolean existsByStudentId(Long studentId);
}
