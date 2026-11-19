package com.utp.quesale.iam.application.acl;

import com.utp.quesale.iam.domain.aggregates.University;
import com.utp.quesale.iam.domain.repositories.EmailVerificationRepository;
import com.utp.quesale.iam.domain.repositories.UniversityRepository;
import com.utp.quesale.iam.interfaces.acl.IamContextFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class IamContextFacadeImpl implements IamContextFacade {

    private final UniversityRepository universityRepository;
    private final EmailVerificationRepository verificationRepository;
    private final boolean verificationRequired;

    public IamContextFacadeImpl(UniversityRepository universityRepository,
                                EmailVerificationRepository verificationRepository,
                                @Value("${app.verification.required:false}") boolean verificationRequired) {
        this.universityRepository = universityRepository;
        this.verificationRepository = verificationRepository;
        this.verificationRequired = verificationRequired;
    }

    @Override
    public boolean isActiveUniversity(Long universityId) {
        if (universityId == null || universityId <= 0) {
            return false;
        }
        return universityRepository.findById(universityId).map(University::isActive).orElse(false);
    }

    @Override
    public boolean universityAcceptsEmail(Long universityId, String email) {
        if (universityId == null || universityId <= 0) {
            return false;
        }
        return universityRepository.findById(universityId)
                .map(university -> university.accepts(email))
                .orElse(false);
    }

    @Override
    public boolean isEmailVerified(Long studentId) {
        if (studentId == null || studentId <= 0) {
            return false;
        }
        return verificationRepository.findVerifiedByStudentId(studentId).isPresent();
    }

    @Override
    public boolean isEmailVerificationRequired() {
        return verificationRequired;
    }
}
