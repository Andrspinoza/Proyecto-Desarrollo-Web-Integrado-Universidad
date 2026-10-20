package com.utp.quesale.profiles.interfaces.acl;

import java.util.Optional;

public interface ProfilesContextFacade {
    boolean existsStudent(Long studentId);

    Optional<Long> studentIdOfAccount(Long accountId);

    Optional<Long> universityOfStudent(Long studentId);
}
