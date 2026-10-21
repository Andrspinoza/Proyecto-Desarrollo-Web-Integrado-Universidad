package com.utp.quesale.communities.application.dtos;

import java.time.Instant;

public record CommunityDto(
        Long id,
        Long universityId,
        String name,
        String description,
        Long creatorId,
        int memberCount,
        Instant createdAt
) {
}
