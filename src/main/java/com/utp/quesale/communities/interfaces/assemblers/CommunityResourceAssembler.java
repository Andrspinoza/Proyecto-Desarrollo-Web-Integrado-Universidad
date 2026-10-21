package com.utp.quesale.communities.interfaces.assemblers;

import com.utp.quesale.communities.application.commands.CreateCommunityCommand;
import com.utp.quesale.communities.application.commands.UpdateCommunityCommand;
import com.utp.quesale.communities.application.dtos.CommunityDto;
import com.utp.quesale.communities.domain.aggregates.Community;
import com.utp.quesale.communities.interfaces.resources.CommunityResource;
import com.utp.quesale.communities.interfaces.resources.CreateCommunityResource;
import com.utp.quesale.communities.interfaces.resources.UpdateCommunityResource;

public class CommunityResourceAssembler {

    public static CreateCommunityCommand toCreateCommand(Long studentId, CreateCommunityResource resource) {
        return new CreateCommunityCommand(studentId, resource.name(), resource.description());
    }

    public static UpdateCommunityCommand toUpdateCommand(Long communityId, Long requesterId,
                                                         UpdateCommunityResource resource) {
        return new UpdateCommunityCommand(communityId, requesterId, resource.name(), resource.description());
    }

    public static CommunityResource toResourceFromDto(CommunityDto dto) {
        return new CommunityResource(
                dto.id(),
                dto.universityId(),
                dto.name(),
                dto.description(),
                dto.creatorId(),
                dto.memberCount(),
                dto.createdAt()
        );
    }

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
