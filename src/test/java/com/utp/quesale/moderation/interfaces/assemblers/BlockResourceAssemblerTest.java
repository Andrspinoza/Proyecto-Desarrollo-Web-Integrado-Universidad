package com.utp.quesale.moderation.interfaces.assemblers;

import com.utp.quesale.moderation.domain.aggregates.Block;
import com.utp.quesale.moderation.interfaces.resources.BlockResource;
import com.utp.quesale.moderation.interfaces.resources.CreateBlockResource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BlockResourceAssemblerTest {

    @Test
    void createsDomainBlockFromResourceAndBlockerId() {
        Block block = BlockResourceAssembler.toDomainFromResource(3L, new CreateBlockResource(7L));

        assertEquals(3L, block.getBlockerId());
        assertEquals(7L, block.getBlockedId());
    }

    @Test
    void createsResourceFromDomainBlock() {
        Block block = new Block(5L, 3L, 7L, java.time.Instant.parse("2026-10-11T12:00:00Z"));

        BlockResource resource = BlockResourceAssembler.toResourceFromEntity(block);

        assertEquals(block.getId(), resource.id());
        assertEquals(block.getBlockerId(), resource.blockerId());
        assertEquals(block.getBlockedId(), resource.blockedId());
        assertEquals(block.getCreatedAt(), resource.createdAt());
    }
}
