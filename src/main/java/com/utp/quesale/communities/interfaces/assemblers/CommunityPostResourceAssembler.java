package com.utp.quesale.communities.interfaces.assemblers;

import com.utp.quesale.communities.domain.entities.CommunityPost;
import com.utp.quesale.communities.interfaces.resources.CommunityPostResource;

public class CommunityPostResourceAssembler {

    public static CommunityPostResource toResourceFromEntity(CommunityPost post) {
        return new CommunityPostResource(
                post.getId(),
                post.getCommunityId(),
                post.getAuthorId(),
                post.getContent(),
                post.getCreatedAt()
        );
    }
}
