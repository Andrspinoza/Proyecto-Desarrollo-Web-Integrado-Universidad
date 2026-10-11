package com.utp.quesale.moderation.domain.aggregates;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BlockTest {

    @Test
    void createsABlockBetweenDifferentStudents() {
        Block block = new Block(1L, 2L);

        assertEquals(1L, block.getBlockerId());
        assertEquals(2L, block.getBlockedId());
        assertNotNull(block.getCreatedAt());
    }

    @Test
    void rejectsBlockingYourself() {
        assertThrows(IllegalArgumentException.class, () -> new Block(1L, 1L));
    }

    @Test
    void requiresBothStudentIds() {
        assertThrows(IllegalArgumentException.class, () -> new Block(null, 2L));
        assertThrows(IllegalArgumentException.class, () -> new Block(1L, null));
    }
}
