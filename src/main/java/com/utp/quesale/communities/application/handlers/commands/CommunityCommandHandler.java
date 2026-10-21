package com.utp.quesale.communities.application.handlers.commands;

import com.utp.quesale.communities.application.assemblers.CommunitiesDtoAssembler;
import com.utp.quesale.communities.application.commands.CreateCommunityCommand;
import com.utp.quesale.communities.application.commands.JoinCommunityCommand;
import com.utp.quesale.communities.application.commands.UpdateCommunityCommand;
import com.utp.quesale.communities.application.dtos.CommunityDto;
import com.utp.quesale.communities.domain.aggregates.Community;
import com.utp.quesale.communities.domain.exceptions.CommunityNameAlreadyExistsException;
import com.utp.quesale.communities.domain.exceptions.CommunityNotFoundException;
import com.utp.quesale.communities.domain.exceptions.DifferentUniversityException;
import com.utp.quesale.communities.domain.exceptions.StudentProfileRequiredException;
import com.utp.quesale.communities.domain.repositories.CommunityRepository;
import com.utp.quesale.profiles.interfaces.acl.ProfilesContextFacade;
import com.utp.quesale.shared.domain.events.DomainEventDispatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Solo coordina: las reglas de la comunidad viven en el agregado
@Service
public class CommunityCommandHandler {

    private final CommunityRepository communityRepository;
    private final ProfilesContextFacade profiles;
    private final DomainEventDispatcher events;

    public CommunityCommandHandler(CommunityRepository communityRepository, ProfilesContextFacade profiles,
                                   DomainEventDispatcher events) {
        this.communityRepository = communityRepository;
        this.profiles = profiles;
        this.events = events;
    }

    @Transactional
    public CommunityDto handle(CreateCommunityCommand command) {
        // La comunidad es de la universidad de su creador
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
        // El agregado valida primero que quien edita sea ADMIN
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
        // Solo se une quien estudia en la misma universidad de la comunidad
        if (!universityOf(command.studentId()).equals(community.getUniversityId()))
            throw new DifferentUniversityException(command.studentId(), community.getId());
        community.join(command.studentId());
        Community saved = communityRepository.update(community);
        events.dispatch(saved);
        return CommunitiesDtoAssembler.toDto(saved);
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
