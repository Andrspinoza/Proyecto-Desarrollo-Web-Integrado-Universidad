package com.utp.quesale.iam.interfaces.acl;

public interface IamContextFacade {

    boolean isActiveUniversity(Long universityId);

    boolean universityAcceptsEmail(Long universityId, String email);
}
