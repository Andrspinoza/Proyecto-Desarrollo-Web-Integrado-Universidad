package com.utp.quesale.communities.application.commands;

public record PublishPostCommand(Long communityId, Long authorId, String content) {
}
