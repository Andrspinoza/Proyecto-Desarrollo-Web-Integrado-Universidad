package com.utp.quesale.profiles.domain.repositories;

import com.utp.quesale.profiles.domain.aggregates.Student;

import java.util.List;
import java.util.Optional;

public interface StudentRepository {
    Student save(Student student);

    Student update(Student student);

    Optional<Student> findById(Long id);

    List<Student> findAll();

    boolean existsById(Long id);

    boolean existsByEmail(String email);
}
