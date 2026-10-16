package com.utp.quesale.communities.domain.repositories;

import com.utp.quesale.communities.domain.aggregates.Community;

import java.util.List;
import java.util.Optional;

public interface CommunityRepository {
    Community save(Community community);

    Community update(Community community);

    Optional<Community> findById(Long id);

    List<Community> findAllByUniversityId(Long universityId);

    boolean existsById(Long id);

    // El nombre es único por universidad (sin distinguir mayúsculas)
    boolean existsByUniversityIdAndName(Long universityId, String name);
}
