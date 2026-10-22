package com.utp.quesale.communities.application.assemblers;

import com.utp.quesale.communities.application.dtos.CommunityDto;
import com.utp.quesale.communities.application.dtos.CommunityPostDto;
import com.utp.quesale.communities.application.dtos.MembershipDto;
import com.utp.quesale.communities.domain.aggregates.Community;
import com.utp.quesale.communities.domain.entities.CommunityPost;
import com.utp.quesale.communities.domain.entities.Membership;

public final class CommunitiesDtoAssembler {

    private CommunitiesDtoAssembler() {
    }

    public static CommunityDto toDto(Community community) {
        return new CommunityDto(
                community.getId(),
                community.getUniversityId(),
                community.getName(),
                community.getDescription(),
                community.getCreatorId(),
                community.getMemberships().size(),
                community.getCreatedAt()
        );
    }

    public static MembershipDto toDto(Membership membership) {
        return new MembershipDto(
                membership.getStudentId(),
                membership.getRole().name(),
                membership.getJoinedAt()
        );
    }

    public static CommunityPostDto toDto(CommunityPost post) {
        return new CommunityPostDto(
                post.getId(),
                post.getCommunityId(),
                post.getAuthorId(),
                post.getContent(),
                post.getCreatedAt()
        );
    }
}
