package com.utp.quesale.iam.infrastructure.persistence.jpa.mappers;

import com.utp.quesale.iam.domain.aggregates.EmailVerification;
import com.utp.quesale.iam.infrastructure.persistence.jpa.entities.EmailVerificationJpaEntity;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class EmailVerificationPersistenceMapper {

    public EmailVerification toDomain(EmailVerificationJpaEntity entity) {
        return new EmailVerification(
                entity.getId(),
                entity.getStudent().getId(),
                entity.getEmail(),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.getVerifiedAt(),
                entity.getCreatedAt()
        );
    }

    public EmailVerificationJpaEntity toEntity(EmailVerification verification, StudentJpaEntity student) {
        EmailVerificationJpaEntity entity = new EmailVerificationJpaEntity();
        entity.setId(verification.getId());
        entity.setStudent(student);
        entity.setEmail(verification.getEmail());
        entity.setTokenHash(verification.getTokenHash());
        entity.setExpiresAt(verification.getExpiresAt());
        entity.setVerifiedAt(verification.getVerifiedAt());
        entity.setCreatedAt(verification.getCreatedAt());
        return entity;
    }
}
