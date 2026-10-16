package com.utp.quesale.moderation.domain.repositories;

import com.utp.quesale.moderation.domain.aggregates.Block;

import java.util.List;
import java.util.Optional;

public interface BlockRepository {
    Block save(Block block);

    Optional<Block> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    List<Block> findAllByBlockerId(Long blockerId);

    boolean existsBetween(Long studentId, Long otherStudentId);

    boolean deleteByBlockerIdAndBlockedId(Long blockerId, Long blockedId);
}
