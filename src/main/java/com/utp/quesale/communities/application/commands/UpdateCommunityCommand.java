package com.utp.quesale.communities.application.commands;

public record UpdateCommunityCommand(Long communityId, Long requesterId, String name, String description) {
}
