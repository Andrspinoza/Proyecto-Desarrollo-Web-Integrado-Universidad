package com.utp.quesale.moderation.infrastructure.persistence.jpa.mappers;

import com.utp.quesale.moderation.domain.aggregates.Block;
import com.utp.quesale.moderation.infrastructure.persistence.jpa.entities.BlockJpaEntity;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class BlockPersistenceMapper {

    public Block toDomain(BlockJpaEntity entity) {
        return new Block(
                entity.getId(),
                entity.getBlocker().getId(),
                entity.getBlocked().getId(),
                entity.getCreatedAt()
        );
    }

    public BlockJpaEntity toEntity(Block block, StudentJpaEntity blocker, StudentJpaEntity blocked) {
        BlockJpaEntity entity = new BlockJpaEntity();
        entity.setId(block.getId());
        entity.setBlocker(blocker);
        entity.setBlocked(blocked);
        entity.setCreatedAt(block.getCreatedAt());
        return entity;
    }
}
