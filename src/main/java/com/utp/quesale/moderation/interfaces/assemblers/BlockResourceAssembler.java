package com.utp.quesale.moderation.interfaces.assemblers;

import com.utp.quesale.moderation.domain.aggregates.Block;
import com.utp.quesale.moderation.interfaces.resources.BlockResource;
import com.utp.quesale.moderation.interfaces.resources.CreateBlockResource;

public final class BlockResourceAssembler {

    private BlockResourceAssembler() {
    }

    public static Block toDomainFromResource(Long blockerId, CreateBlockResource resource) {
        return new Block(blockerId, resource.blockedStudentId());
    }

    public static BlockResource toResourceFromEntity(Block block) {
        return new BlockResource(block.getId(), block.getBlockerId(),
                block.getBlockedId(), block.getCreatedAt());
    }
}
