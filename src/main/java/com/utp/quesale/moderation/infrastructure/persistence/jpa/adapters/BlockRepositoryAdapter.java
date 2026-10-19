package com.utp.quesale.moderation.infrastructure.persistence.jpa.adapters;

import com.utp.quesale.moderation.domain.aggregates.Block;
import com.utp.quesale.moderation.domain.repositories.BlockRepository;
import com.utp.quesale.moderation.infrastructure.persistence.jpa.entities.BlockJpaEntity;
import com.utp.quesale.moderation.infrastructure.persistence.jpa.mappers.BlockPersistenceMapper;
import com.utp.quesale.moderation.infrastructure.persistence.jpa.repositories.BlockJpaRepository;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class BlockRepositoryAdapter implements BlockRepository {
    private final BlockJpaRepository repository;
    private final BlockPersistenceMapper mapper;
    private final EntityManager entityManager;

    public BlockRepositoryAdapter(BlockJpaRepository repository,
                                  BlockPersistenceMapper mapper,
                                  EntityManager entityManager) {
        this.repository = repository;
        this.mapper = mapper;
        this.entityManager = entityManager;
    }

    @Override
    public Block save(Block block) {
        StudentJpaEntity blocker = entityManager.getReference(StudentJpaEntity.class, block.getBlockerId());
        StudentJpaEntity blocked = entityManager.getReference(StudentJpaEntity.class, block.getBlockedId());
        BlockJpaEntity entity = repository.save(mapper.toEntity(block, blocker, blocked));
        return mapper.toDomain(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Block> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId) {
        return repository.findByBlocker_IdAndBlocked_Id(blockerId, blockedId).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Block> findAllByBlockerId(Long blockerId) {
        return repository.findAllByBlocker_Id(blockerId).stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBetween(Long studentId, Long otherStudentId) {
        return repository.existsBetween(studentId, otherStudentId);
    }

    @Override
    public boolean deleteByBlockerIdAndBlockedId(Long blockerId, Long blockedId) {
        return repository.deleteByBlocker_IdAndBlocked_Id(blockerId, blockedId) > 0;
    }
}
