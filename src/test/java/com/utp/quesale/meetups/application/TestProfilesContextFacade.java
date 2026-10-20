package com.utp.quesale.meetups.application;

import com.utp.quesale.profiles.interfaces.acl.ProfilesContextFacade;

import java.util.Map;
import java.util.Optional;

public class TestProfilesContextFacade implements ProfilesContextFacade {

    private final Map<Long, Long> universitiesByStudent;

    public TestProfilesContextFacade(Map<Long, Long> universitiesByStudent) {
        this.universitiesByStudent = Map.copyOf(universitiesByStudent);
    }

    @Override
    public boolean existsStudent(Long studentId) {
        return universitiesByStudent.containsKey(studentId);
    }

    @Override
    public Optional<Long> studentIdOfAccount(Long accountId) {
        return Optional.empty();
    }

    @Override
    public Optional<Long> universityOfStudent(Long studentId) {
        return Optional.ofNullable(universitiesByStudent.get(studentId));
    }
}
