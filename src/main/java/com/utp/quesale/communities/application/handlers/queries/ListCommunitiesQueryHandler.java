package com.utp.quesale.communities.application.handlers.queries;

import com.utp.quesale.communities.application.assemblers.CommunitiesDtoAssembler;
import com.utp.quesale.communities.application.dtos.CommunityDto;
import com.utp.quesale.communities.application.queries.ListCommunitiesQuery;
import com.utp.quesale.communities.domain.exceptions.StudentProfileRequiredException;
import com.utp.quesale.communities.domain.repositories.CommunityRepository;
import com.utp.quesale.profiles.interfaces.acl.ProfilesContextFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListCommunitiesQueryHandler {

    private final CommunityRepository communityRepository;
    private final ProfilesContextFacade profiles;

    public ListCommunitiesQueryHandler(CommunityRepository communityRepository, ProfilesContextFacade profiles) {
        this.communityRepository = communityRepository;
        this.profiles = profiles;
    }

    // Las comunidades de la universidad del estudiante
    public List<CommunityDto> handle(ListCommunitiesQuery query) {
        Long universityId = profiles.universityOfStudent(query.studentId())
                .orElseThrow(() -> new StudentProfileRequiredException(query.studentId()));
        return communityRepository.findAllByUniversityId(universityId).stream()
                .map(CommunitiesDtoAssembler::toDto)
                .toList();
    }
}
