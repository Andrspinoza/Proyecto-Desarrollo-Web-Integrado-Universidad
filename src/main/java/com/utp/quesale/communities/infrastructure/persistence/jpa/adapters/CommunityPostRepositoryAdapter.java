package com.utp.quesale.communities.infrastructure.persistence.jpa.adapters;

import com.utp.quesale.communities.domain.entities.CommunityPost;
import com.utp.quesale.communities.domain.repositories.CommunityPostRepository;
import com.utp.quesale.communities.infrastructure.persistence.jpa.entities.CommunityJpaEntity;
import com.utp.quesale.communities.infrastructure.persistence.jpa.entities.CommunityPostJpaEntity;
import com.utp.quesale.communities.infrastructure.persistence.jpa.mappers.CommunityPostPersistenceMapper;
import com.utp.quesale.communities.infrastructure.persistence.jpa.repositories.CommunityPostJpaRepository;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class CommunityPostRepositoryAdapter implements CommunityPostRepository {
    private final CommunityPostJpaRepository repository;
    private final CommunityPostPersistenceMapper mapper;
    private final EntityManager entityManager;

    public CommunityPostRepositoryAdapter(
            CommunityPostJpaRepository repository,
            CommunityPostPersistenceMapper mapper,
            EntityManager entityManager) {
        this.repository = repository;
        this.mapper = mapper;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public CommunityPost save(CommunityPost post) {
        CommunityPostJpaEntity entity = mapper.toEntity(
                post,
                entityManager.getReference(CommunityJpaEntity.class, post.getCommunityId()),
                entityManager.getReference(StudentJpaEntity.class, post.getAuthorId()));
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunityPost> findAllByCommunityId(Long communityId) {
        return repository.findAllByCommunity_IdOrderByCreatedAtDesc(communityId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
