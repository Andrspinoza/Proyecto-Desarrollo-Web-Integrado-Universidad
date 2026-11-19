package com.utp.quesale.iam.domain.repositories;

import com.utp.quesale.iam.domain.aggregates.EmailVerification;

import java.util.Optional;

public interface EmailVerificationRepository {

    EmailVerification save(EmailVerification verification);

    Optional<EmailVerification> findByTokenHash(String tokenHash);

    Optional<EmailVerification> findLatestByStudentId(Long studentId);

    Optional<EmailVerification> findVerifiedByStudentId(Long studentId);
}
