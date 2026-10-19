package com.utp.quesale.profiles.domain.repositories;

import com.utp.quesale.profiles.domain.aggregates.Skill;

import java.util.List;
import java.util.Optional;

public interface SkillRepository {
    Skill save(Skill skill);

    Optional<Skill> findById(Long id);

    List<Skill> findAll();

    boolean existsById(Long id);

    boolean existsByName(String name);
}
