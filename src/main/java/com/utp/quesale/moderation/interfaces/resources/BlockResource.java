package com.utp.quesale.moderation.interfaces.resources;

import java.time.Instant;

public record BlockResource(
        Long id,
        Long blockerId,
        Long blockedId,
        Instant createdAt) {
}
