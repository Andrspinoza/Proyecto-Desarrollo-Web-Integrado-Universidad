package com.utp.quesale.communities.interfaces.assemblers;

import com.utp.quesale.communities.application.commands.PublishPostCommand;
import com.utp.quesale.communities.application.dtos.CommunityPostDto;
import com.utp.quesale.communities.domain.entities.CommunityPost;
import com.utp.quesale.communities.interfaces.resources.CommunityPostResource;
import com.utp.quesale.communities.interfaces.resources.CreateCommunityPostResource;

public class CommunityPostResourceAssembler {

    public static PublishPostCommand toPublishCommand(Long communityId, Long authorId,
                                                      CreateCommunityPostResource resource) {
        return new PublishPostCommand(communityId, authorId, resource.content());
    }

    public static CommunityPostResource toResourceFromDto(CommunityPostDto dto) {
        return new CommunityPostResource(dto.id(), dto.communityId(), dto.authorId(), dto.content(), dto.createdAt());
    }

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
