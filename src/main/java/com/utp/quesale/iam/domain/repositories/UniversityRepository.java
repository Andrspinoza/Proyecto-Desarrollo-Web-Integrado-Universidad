package com.utp.quesale.iam.domain.repositories;

import com.utp.quesale.iam.domain.aggregates.University;

import java.util.List;
import java.util.Optional;

public interface UniversityRepository {
    University save(University university);

    University update(University university);

    Optional<University> findById(Long id);

    List<University> findAll();

    boolean existsById(Long id);

    boolean existsByEmailDomain(String emailDomain);
}
