package com.utp.quesale.moderation.application;

import com.utp.quesale.moderation.domain.aggregates.Block;
import com.utp.quesale.moderation.domain.repositories.BlockRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class InMemoryBlockRepository implements BlockRepository {

    private final Map<Long, Block> blocks = new LinkedHashMap<>();
    private long nextId = 1;

    @Override
    public Block save(Block block) {
        Block saved = new Block(nextId++, block.getBlockerId(), block.getBlockedId(), block.getCreatedAt());
        blocks.put(saved.getId(), saved);
        return saved;
    }

    @Override
    public Optional<Block> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId) {
        return blocks.values().stream()
                .filter(block -> block.getBlockerId().equals(blockerId) && block.getBlockedId().equals(blockedId))
                .findFirst();
    }

    @Override
    public List<Block> findAllByBlockerId(Long blockerId) {
        return blocks.values().stream()
                .filter(block -> block.getBlockerId().equals(blockerId))
                .toList();
    }

    @Override
    public boolean existsBetween(Long studentId, Long otherStudentId) {
        return findByBlockerIdAndBlockedId(studentId, otherStudentId).isPresent()
                || findByBlockerIdAndBlockedId(otherStudentId, studentId).isPresent();
    }

    @Override
    public boolean deleteByBlockerIdAndBlockedId(Long blockerId, Long blockedId) {
        return findByBlockerIdAndBlockedId(blockerId, blockedId)
                .map(block -> blocks.remove(block.getId()) != null)
                .orElse(false);
    }
}
