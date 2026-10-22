package com.utp.quesale.communities.application.handlers.queries;

import com.utp.quesale.communities.application.assemblers.CommunitiesDtoAssembler;
import com.utp.quesale.communities.application.dtos.CommunityPostDto;
import com.utp.quesale.communities.application.queries.ListPostsQuery;
import com.utp.quesale.communities.domain.aggregates.Community;
import com.utp.quesale.communities.domain.exceptions.CommunityNotFoundException;
import com.utp.quesale.communities.domain.exceptions.NotMemberException;
import com.utp.quesale.communities.domain.repositories.CommunityPostRepository;
import com.utp.quesale.communities.domain.repositories.CommunityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListPostsQueryHandler {

    private final CommunityRepository communityRepository;
    private final CommunityPostRepository postRepository;

    public ListPostsQueryHandler(CommunityRepository communityRepository, CommunityPostRepository postRepository) {
        this.communityRepository = communityRepository;
        this.postRepository = postRepository;
    }

    public List<CommunityPostDto> handle(ListPostsQuery query) {
        Community community = communityRepository.findById(query.communityId())
                .orElseThrow(() -> new CommunityNotFoundException(query.communityId()));
        if (!community.isMember(query.requesterId())) throw new NotMemberException(query.requesterId());
        return postRepository.findAllByCommunityId(community.getId()).stream()
                .map(CommunitiesDtoAssembler::toDto)
                .toList();
    }
}
