package com.utp.quesale.communities.infrastructure.persistence.jpa.adapters;

import com.utp.quesale.communities.domain.aggregates.Community;
import com.utp.quesale.communities.domain.repositories.CommunityRepository;
import com.utp.quesale.communities.infrastructure.persistence.jpa.entities.CommunityJpaEntity;
import com.utp.quesale.communities.infrastructure.persistence.jpa.mappers.CommunityPersistenceMapper;
import com.utp.quesale.communities.infrastructure.persistence.jpa.repositories.CommunityJpaRepository;
import com.utp.quesale.iam.infrastructure.persistence.jpa.entities.UniversityJpaEntity;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class CommunityRepositoryAdapter implements CommunityRepository {
    private final CommunityJpaRepository repository;
    private final CommunityPersistenceMapper mapper;
    private final EntityManager entityManager;

    // Inyección de dependencias por constructor (indicación del profesor)
    public CommunityRepositoryAdapter(
            CommunityJpaRepository repository,
            CommunityPersistenceMapper mapper,
            EntityManager entityManager) {
        this.repository = repository;
        this.mapper = mapper;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public Community save(Community community) {
        CommunityJpaEntity entity = mapper.toEntity(
                community,
                entityManager.getReference(UniversityJpaEntity.class, community.getUniversityId()),
                entityManager.getReference(StudentJpaEntity.class, community.getCreatorId()),
                studentId -> entityManager.getReference(StudentJpaEntity.class, studentId));
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    @Transactional
    public Community update(Community community) {
        return save(community);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Community> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Community> findAllByUniversityId(Long universityId) {
        return repository.findAllByUniversity_Id(universityId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUniversityIdAndName(Long universityId, String name) {
        return repository.existsByUniversity_IdAndNameIgnoreCase(universityId, name);
    }
}
