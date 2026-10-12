package com.utp.quesale.moderation.domain.aggregates;

import com.utp.quesale.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Instant;

public class Block extends AbstractDomainAggregateRoot<Block> {

    private Long id;
    private final Long blockerId;
    private final Long blockedId;
    private final Instant createdAt;

    public Block(Long blockerId, Long blockedId) {
        this(null, blockerId, blockedId, Instant.now());
    }

    public Block(Long id, Long blockerId, Long blockedId, Instant createdAt) {
        validateOptionalPositiveId(id, "Block ID");
        validateRequiredPositiveId(blockerId, "Blocker ID");
        validateRequiredPositiveId(blockedId, "Blocked ID");
        if (blockerId.equals(blockedId)) throw new IllegalArgumentException("A student cannot block themselves");
        if (createdAt == null) throw new IllegalArgumentException("Created at is required");

        this.id = id;
        this.blockerId = blockerId;
        this.blockedId = blockedId;
        this.createdAt = createdAt;
    }

    private static void validateOptionalPositiveId(Long value, String fieldName) {
        if (value != null && value <= 0) throw new IllegalArgumentException(fieldName + " must be positive");
    }

    private static void validateRequiredPositiveId(Long value, String fieldName) {
        if (value == null) throw new IllegalArgumentException(fieldName + " is required");
        validateOptionalPositiveId(value, fieldName);
    }

    public Long getId() {
        return id;
    }

    public Long getBlockerId() {
        return blockerId;
    }

    public Long getBlockedId() {
        return blockedId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
