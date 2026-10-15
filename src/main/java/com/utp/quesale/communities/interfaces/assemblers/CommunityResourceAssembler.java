package com.utp.quesale.communities.interfaces.assemblers;

import com.utp.quesale.communities.domain.aggregates.Community;
import com.utp.quesale.communities.interfaces.resources.CommunityResource;

public class CommunityResourceAssembler {

    public static CommunityResource toResourceFromEntity(Community community) {
        return new CommunityResource(
                community.getId(),
                community.getUniversityId(),
                community.getName(),
                community.getDescription(),
                community.getCreatorId(),
                community.getMemberships().size(),
                community.getCreatedAt()
        );
    }
}
