package com.utp.quesale.moderation.application;

import com.utp.quesale.moderation.application.acl.ModerationContextFacadeImpl;
import com.utp.quesale.moderation.domain.aggregates.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModerationContextFacadeImplTest {

    private final InMemoryBlockRepository blocks = new InMemoryBlockRepository();
    private final ModerationContextFacadeImpl facade = new ModerationContextFacadeImpl(blocks);

    @Test
    void isBlockedInTheSenseOfTheBlocker() {
        blocks.save(new Block(1L, 3L));

        assertTrue(facade.isBlocked(1L, 3L));
    }

    @Test
    void isBlockedInTheOppositeDirectionToo() {
        blocks.save(new Block(1L, 3L));

        assertTrue(facade.isBlocked(3L, 1L));
    }

    @Test
    void isNotBlockedWithoutABlock() {
        blocks.save(new Block(1L, 3L));

        assertFalse(facade.isBlocked(1L, 2L));
    }

    @Test
    void invalidIdsAreNeverBlocked() {
        assertFalse(facade.isBlocked(null, 2L));
        assertFalse(facade.isBlocked(1L, null));
        assertFalse(facade.isBlocked(0L, 2L));
        assertFalse(facade.isBlocked(1L, -5L));
    }
}
