package com.utp.quesale.communities.application.handlers.queries;

import com.utp.quesale.communities.application.assemblers.CommunitiesDtoAssembler;
import com.utp.quesale.communities.application.dtos.MembershipDto;
import com.utp.quesale.communities.application.queries.ListMembersQuery;
import com.utp.quesale.communities.domain.aggregates.Community;
import com.utp.quesale.communities.domain.exceptions.CommunityNotFoundException;
import com.utp.quesale.communities.domain.exceptions.DifferentUniversityException;
import com.utp.quesale.communities.domain.exceptions.StudentProfileRequiredException;
import com.utp.quesale.communities.domain.repositories.CommunityRepository;
import com.utp.quesale.profiles.interfaces.acl.ProfilesContextFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListMembersQueryHandler {

    private final CommunityRepository communityRepository;
    private final ProfilesContextFacade profiles;

    public ListMembersQueryHandler(CommunityRepository communityRepository, ProfilesContextFacade profiles) {
        this.communityRepository = communityRepository;
        this.profiles = profiles;
    }

    public List<MembershipDto> handle(ListMembersQuery query) {
        Community community = communityRepository.findById(query.communityId())
                .orElseThrow(() -> new CommunityNotFoundException(query.communityId()));
        Long universityId = profiles.universityOfStudent(query.requesterId())
                .orElseThrow(() -> new StudentProfileRequiredException(query.requesterId()));
        if (!universityId.equals(community.getUniversityId()))
            throw new DifferentUniversityException(query.requesterId(), community.getId());
        return community.getMemberships().stream().map(CommunitiesDtoAssembler::toDto).toList();
    }
}
