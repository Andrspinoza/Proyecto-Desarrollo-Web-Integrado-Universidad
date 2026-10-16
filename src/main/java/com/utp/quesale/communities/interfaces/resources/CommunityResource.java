package com.utp.quesale.communities.interfaces.resources;

import java.time.Instant;

public record CommunityResource(
        Long id,
        Long universityId,
        String name,
        String description,
        Long creatorId,
        int memberCount,
        Instant createdAt
) {
}
