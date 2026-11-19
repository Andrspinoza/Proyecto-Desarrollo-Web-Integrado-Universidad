package com.utp.quesale.iam.application;

import com.utp.quesale.iam.domain.aggregates.EmailVerification;
import com.utp.quesale.iam.domain.repositories.EmailVerificationRepository;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryEmailVerificationRepository implements EmailVerificationRepository {

    private final Map<Long, EmailVerification> store = new LinkedHashMap<>();
    private long nextId = 1;

    @Override
    public EmailVerification save(EmailVerification verification) {
        if (verification.getId() != null) {
            store.put(verification.getId(), verification);
            return verification;
        }
        EmailVerification saved = new EmailVerification(nextId++, verification.getStudentId(),
                verification.getEmail(), verification.getTokenHash(), verification.getExpiresAt(),
                verification.getVerifiedAt(), verification.getCreatedAt());
        store.put(saved.getId(), saved);
        return saved;
    }

    @Override
    public Optional<EmailVerification> findByTokenHash(String tokenHash) {
        return store.values().stream().filter(v -> v.getTokenHash().equals(tokenHash)).findFirst();
    }

    @Override
    public Optional<EmailVerification> findLatestByStudentId(Long studentId) {
        return store.values().stream()
                .filter(v -> v.getStudentId().equals(studentId))
                .max(Comparator.comparing(EmailVerification::getCreatedAt).thenComparing(EmailVerification::getId));
    }

    @Override
    public Optional<EmailVerification> findVerifiedByStudentId(Long studentId) {
        return store.values().stream()
                .filter(v -> v.getStudentId().equals(studentId) && v.isVerified())
                .findFirst();
    }

    public int size() {
        return store.size();
    }
}
