package com.utp.quesale.communities.application.handlers.commands;

import com.utp.quesale.communities.application.assemblers.CommunitiesDtoAssembler;
import com.utp.quesale.communities.application.commands.CreateCommunityCommand;
import com.utp.quesale.communities.application.commands.JoinCommunityCommand;
import com.utp.quesale.communities.application.commands.LeaveCommunityCommand;
import com.utp.quesale.communities.application.commands.PromoteMemberCommand;
import com.utp.quesale.communities.application.commands.PublishPostCommand;
import com.utp.quesale.communities.application.commands.UpdateCommunityCommand;
import com.utp.quesale.communities.application.dtos.CommunityDto;
import com.utp.quesale.communities.application.dtos.CommunityPostDto;
import com.utp.quesale.communities.domain.aggregates.Community;
import com.utp.quesale.communities.domain.entities.CommunityPost;
import com.utp.quesale.communities.domain.exceptions.CommunityNameAlreadyExistsException;
import com.utp.quesale.communities.domain.exceptions.CommunityNotFoundException;
import com.utp.quesale.communities.domain.exceptions.DifferentUniversityException;
import com.utp.quesale.communities.domain.exceptions.StudentProfileRequiredException;
import com.utp.quesale.communities.domain.repositories.CommunityPostRepository;
import com.utp.quesale.communities.domain.repositories.CommunityRepository;
import com.utp.quesale.profiles.interfaces.acl.ProfilesContextFacade;
import com.utp.quesale.shared.domain.events.DomainEventDispatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommunityCommandHandler {

    private final CommunityRepository communityRepository;
    private final CommunityPostRepository postRepository;
    private final ProfilesContextFacade profiles;
    private final DomainEventDispatcher events;

    public CommunityCommandHandler(CommunityRepository communityRepository, CommunityPostRepository postRepository,
                                   ProfilesContextFacade profiles, DomainEventDispatcher events) {
        this.communityRepository = communityRepository;
        this.postRepository = postRepository;
        this.profiles = profiles;
        this.events = events;
    }

    @Transactional
    public CommunityDto handle(CreateCommunityCommand command) {
        Long universityId = universityOf(command.studentId());
        Community community = new Community(universityId, command.name(), command.description(), command.studentId());
        if (communityRepository.existsByUniversityIdAndName(universityId, community.getName()))
            throw new CommunityNameAlreadyExistsException(community.getName());
        Community saved = communityRepository.save(community);
        events.dispatch(saved);
        return CommunitiesDtoAssembler.toDto(saved);
    }

    @Transactional
    public CommunityDto handle(UpdateCommunityCommand command) {
        Community community = findCommunity(command.communityId());
        String previousName = community.getName();
        community.update(command.name(), command.description(), command.requesterId());
        if (!community.getName().equalsIgnoreCase(previousName)
                && communityRepository.existsByUniversityIdAndName(community.getUniversityId(), community.getName()))
            throw new CommunityNameAlreadyExistsException(community.getName());
        Community saved = communityRepository.update(community);
        events.dispatch(saved);
        return CommunitiesDtoAssembler.toDto(saved);
    }

    @Transactional
    public CommunityDto handle(JoinCommunityCommand command) {
        Community community = findCommunity(command.communityId());
        if (!universityOf(command.studentId()).equals(community.getUniversityId()))
            throw new DifferentUniversityException(command.studentId(), community.getId());
        community.join(command.studentId());
        Community saved = communityRepository.update(community);
        events.dispatch(saved);
        return CommunitiesDtoAssembler.toDto(saved);
    }

    @Transactional
    public void handle(LeaveCommunityCommand command) {
        Community community = findCommunity(command.communityId());
        community.leave(command.studentId());
        events.dispatch(communityRepository.update(community));
    }

    @Transactional
    public CommunityDto handle(PromoteMemberCommand command) {
        Community community = findCommunity(command.communityId());
        community.promote(command.memberId(), command.requesterId());
        Community saved = communityRepository.update(community);
        events.dispatch(saved);
        return CommunitiesDtoAssembler.toDto(saved);
    }

    @Transactional
    public CommunityPostDto handle(PublishPostCommand command) {
        Community community = findCommunity(command.communityId());
        CommunityPost post = community.publish(command.authorId(), command.content());
        return CommunitiesDtoAssembler.toDto(postRepository.save(post));
    }

    private Community findCommunity(Long communityId) {
        return communityRepository.findById(communityId)
                .orElseThrow(() -> new CommunityNotFoundException(communityId));
    }

    private Long universityOf(Long studentId) {
        return profiles.universityOfStudent(studentId)
                .orElseThrow(() -> new StudentProfileRequiredException(studentId));
    }
}
