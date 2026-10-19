package com.utp.quesale.profiles.domain.repositories;

import com.utp.quesale.profiles.domain.aggregates.Interest;

import java.util.List;
import java.util.Optional;

public interface InterestRepository {
    Interest save(Interest interest);

    Optional<Interest> findById(Long id);

    List<Interest> findAll();

    boolean existsById(Long id);

    boolean existsByName(String name);
}
