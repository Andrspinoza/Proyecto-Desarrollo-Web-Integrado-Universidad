package com.utp.quesale.profiles.application.acl;

import com.utp.quesale.profiles.domain.aggregates.Student;
import com.utp.quesale.profiles.domain.repositories.StudentRepository;
import com.utp.quesale.profiles.interfaces.acl.ProfilesContextFacade;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProfilesContextFacadeImpl implements ProfilesContextFacade {
    private final StudentRepository students;

    public ProfilesContextFacadeImpl(StudentRepository students) {
        this.students = students;
    }

    @Override
    public boolean existsStudent(Long studentId) {
        return studentId != null && studentId > 0 && students.existsById(studentId);
    }

    @Override
    public Optional<Long> studentIdOfAccount(Long accountId) {
        return Optional.empty();
    }

    @Override
    public Optional<Long> universityOfStudent(Long studentId) {
        if (studentId == null || studentId <= 0) return Optional.empty();
        return students.findById(studentId).map(Student::getUniversityId);
    }

    @Override
    public Optional<String> firstNameOfStudent(Long studentId) {
        if (studentId == null || studentId <= 0) return Optional.empty();
        return students.findById(studentId).map(student -> student.getFullName().firstName());
    }
}
