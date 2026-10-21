package com.utp.quesale.communities.application.dtos;

import java.time.Instant;

public record CommunityPostDto(Long id, Long communityId, Long authorId, String content, Instant createdAt) {
}
