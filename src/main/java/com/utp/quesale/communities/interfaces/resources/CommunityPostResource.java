package com.utp.quesale.communities.interfaces.resources;

import java.time.Instant;

public record CommunityPostResource(Long id, Long communityId, Long authorId, String content, Instant createdAt) {
}
