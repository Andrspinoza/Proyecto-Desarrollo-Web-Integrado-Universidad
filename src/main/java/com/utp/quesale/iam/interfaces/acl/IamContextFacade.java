package com.utp.quesale.iam.interfaces.acl;

public interface IamContextFacade {

    boolean isActiveUniversity(Long universityId);

    boolean universityAcceptsEmail(Long universityId, String email);

    default boolean isEmailVerified(Long studentId) {
        return false;
    }

    default boolean isEmailVerificationRequired() {
        return false;
    }
}
