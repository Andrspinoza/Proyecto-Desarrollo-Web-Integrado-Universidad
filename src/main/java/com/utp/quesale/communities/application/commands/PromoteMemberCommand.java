package com.utp.quesale.communities.application.commands;

public record PromoteMemberCommand(Long communityId, Long memberId, Long requesterId) {
}
