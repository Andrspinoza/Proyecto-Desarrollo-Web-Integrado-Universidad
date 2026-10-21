package com.utp.quesale.moderation.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.ResourceNotFoundException;

public class BlockNotFoundException extends ResourceNotFoundException {
    public BlockNotFoundException(Long blockerId, Long blockedId) {
        super("Block from student %s to student %s not found".formatted(blockerId, blockedId));
    }
}
