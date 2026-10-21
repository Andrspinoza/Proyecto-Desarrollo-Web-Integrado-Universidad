package com.utp.quesale.moderation.infrastructure;

import com.utp.quesale.moderation.domain.aggregates.Block;
import com.utp.quesale.moderation.infrastructure.persistence.jpa.adapters.BlockRepositoryAdapter;
import com.utp.quesale.moderation.infrastructure.persistence.jpa.mappers.BlockPersistenceMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import({BlockRepositoryAdapter.class, BlockPersistenceMapper.class})
class BlockRepositoryAdapterTest {

    @Autowired
    private BlockRepositoryAdapter blockRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesReadsAndListsBlocksByBlocker() {
        ModerationPersistenceTestData.StudentIds students =
                ModerationPersistenceTestData.createStudents(entityManager);
        Block saved = blockRepository.save(new Block(students.first(), students.second()));

        Block found = blockRepository.findByBlockerIdAndBlockedId(students.first(), students.second()).orElseThrow();

        assertEquals(saved.getId(), found.getId());
        assertEquals(students.first(), found.getBlockerId());
        assertEquals(students.second(), found.getBlockedId());
        assertEquals(1, blockRepository.findAllByBlockerId(students.first()).size());
    }

    @Test
    void detectsBlocksInEitherDirectionAndDeletesTheRequestedDirection() {
        ModerationPersistenceTestData.StudentIds students =
                ModerationPersistenceTestData.createStudents(entityManager);
        blockRepository.save(new Block(students.first(), students.second()));

        assertTrue(blockRepository.existsBetween(students.first(), students.second()));
        assertTrue(blockRepository.existsBetween(students.second(), students.first()));
        assertFalse(blockRepository.existsBetween(students.first(), 999L));
        assertTrue(blockRepository.deleteByBlockerIdAndBlockedId(students.first(), students.second()));
        assertFalse(blockRepository.existsBetween(students.first(), students.second()));
        assertFalse(blockRepository.deleteByBlockerIdAndBlockedId(students.first(), students.second()));
    }
}
