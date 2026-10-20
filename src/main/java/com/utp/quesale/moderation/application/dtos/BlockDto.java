package com.utp.quesale.moderation.application.dtos;

import java.time.Instant;

public record BlockDto(Long id, Long blockerId, Long blockedId, Instant createdAt) {
}
