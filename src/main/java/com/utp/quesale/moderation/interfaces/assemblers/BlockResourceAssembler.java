package com.utp.quesale.moderation.interfaces.assemblers;

import com.utp.quesale.moderation.application.commands.CreateBlockCommand;
import com.utp.quesale.moderation.application.dtos.BlockDto;
import com.utp.quesale.moderation.domain.aggregates.Block;
import com.utp.quesale.moderation.interfaces.resources.BlockResource;
import com.utp.quesale.moderation.interfaces.resources.CreateBlockResource;

public final class BlockResourceAssembler {

    private BlockResourceAssembler() {
    }

    public static Block toDomainFromResource(Long blockerId, CreateBlockResource resource) {
        return new Block(blockerId, resource.blockedStudentId());
    }

    public static CreateBlockCommand toCreateCommand(Long blockerId, CreateBlockResource resource) {
        return new CreateBlockCommand(blockerId, resource.blockedStudentId());
    }

    public static BlockResource toResourceFromDto(BlockDto dto) {
        return new BlockResource(dto.id(), dto.blockerId(), dto.blockedId(), dto.createdAt());
    }

    public static BlockResource toResourceFromEntity(Block block) {
        return new BlockResource(block.getId(), block.getBlockerId(),
                block.getBlockedId(), block.getCreatedAt());
    }
}
