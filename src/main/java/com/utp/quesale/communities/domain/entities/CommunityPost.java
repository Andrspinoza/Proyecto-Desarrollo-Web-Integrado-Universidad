package com.utp.quesale.communities.domain.entities;

import java.time.Instant;

public class CommunityPost {

    private static final int CONTENT_MAX_LENGTH = 1000;

    private Long id;
    private final Long communityId;
    private final Long authorId;
    private final String content;
    private final Instant createdAt;

    public CommunityPost(Long communityId, Long authorId, String content) {
        this(null, communityId, authorId, content, Instant.now());
    }

    public CommunityPost(Long id, Long communityId, Long authorId, String content, Instant createdAt) {
        if (communityId == null) throw new IllegalArgumentException("Community id is required");
        if (authorId == null) throw new IllegalArgumentException("Author id is required");
        if (createdAt == null) throw new IllegalArgumentException("Created at is required");
        this.id = id;
        this.communityId = communityId;
        this.authorId = authorId;
        this.content = validContent(content);
        this.createdAt = createdAt;
    }

    private static String validContent(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Post content is required");
        String trimmed = value.trim();
        if (trimmed.length() > CONTENT_MAX_LENGTH)
            throw new IllegalArgumentException("Post content must be at most %d characters".formatted(CONTENT_MAX_LENGTH));
        return trimmed;
    }

    public Long getId() {
        return id;
    }

    public Long getCommunityId() {
        return communityId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
