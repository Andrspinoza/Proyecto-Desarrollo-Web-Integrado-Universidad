package com.utp.quesale.communities.infrastructure.persistence.jpa.repositories;

import com.utp.quesale.communities.infrastructure.persistence.jpa.entities.CommunityPostJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityPostJpaRepository extends JpaRepository<CommunityPostJpaEntity, Long> {
    List<CommunityPostJpaEntity> findAllByCommunity_IdOrderByCreatedAtDesc(Long communityId);
}
