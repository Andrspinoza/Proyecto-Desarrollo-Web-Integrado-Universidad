package com.utp.quesale.communities.infrastructure.persistence.jpa.repositories;

import com.utp.quesale.communities.infrastructure.persistence.jpa.entities.CommunityJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityJpaRepository extends JpaRepository<CommunityJpaEntity, Long> {
    List<CommunityJpaEntity> findAllByUniversity_Id(Long universityId);

    boolean existsByUniversity_IdAndNameIgnoreCase(Long universityId, String name);
}
