package com.utp.quesale.communities.application;

import com.utp.quesale.profiles.interfaces.acl.ProfilesContextFacade;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FakeProfilesContextFacade implements ProfilesContextFacade {

    private final Map<Long, Long> universityByStudent = new HashMap<>();

    public void register(Long studentId, Long universityId) {
        universityByStudent.put(studentId, universityId);
    }

    @Override
    public boolean existsStudent(Long studentId) {
        return universityByStudent.containsKey(studentId);
    }

    @Override
    public Optional<Long> studentIdOfAccount(Long accountId) {
        return Optional.empty();
    }

    @Override
    public Optional<Long> universityOfStudent(Long studentId) {
        return Optional.ofNullable(universityByStudent.get(studentId));
    }
}
