package com.utp.quesale.iam.infrastructure.persistence.jpa.adapters;

import com.utp.quesale.iam.domain.aggregates.EmailVerification;
import com.utp.quesale.iam.domain.repositories.EmailVerificationRepository;
import com.utp.quesale.iam.infrastructure.persistence.jpa.mappers.EmailVerificationPersistenceMapper;
import com.utp.quesale.iam.infrastructure.persistence.jpa.repositories.EmailVerificationJpaRepository;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class EmailVerificationRepositoryAdapter implements EmailVerificationRepository {
    private final EmailVerificationJpaRepository repository;
    private final EmailVerificationPersistenceMapper mapper;
    private final EntityManager entityManager;

    public EmailVerificationRepositoryAdapter(EmailVerificationJpaRepository repository,
                                              EmailVerificationPersistenceMapper mapper,
                                              EntityManager entityManager) {
        this.repository = repository;
        this.mapper = mapper;
        this.entityManager = entityManager;
    }

    @Override
    public EmailVerification save(EmailVerification verification) {
        StudentJpaEntity student = entityManager.getReference(StudentJpaEntity.class, verification.getStudentId());
        return mapper.toDomain(repository.save(mapper.toEntity(verification, student)));
    }

    @Override
    public Optional<EmailVerification> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash).map(mapper::toDomain);
    }

    @Override
    public Optional<EmailVerification> findLatestByStudentId(Long studentId) {
        return repository.findFirstByStudent_IdOrderByCreatedAtDescIdDesc(studentId).map(mapper::toDomain);
    }

    @Override
    public Optional<EmailVerification> findVerifiedByStudentId(Long studentId) {
        return repository.findFirstByStudent_IdAndVerifiedAtIsNotNull(studentId).map(mapper::toDomain);
    }
}
