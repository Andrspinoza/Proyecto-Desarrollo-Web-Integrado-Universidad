package com.utp.quesale.profiles.application.handlers.commands;

import com.utp.quesale.profiles.application.assemblers.ProfilesDtoAssembler;
import com.utp.quesale.profiles.application.commands.CreateInterestCommand;
import com.utp.quesale.profiles.application.dtos.InterestDto;
import com.utp.quesale.profiles.domain.aggregates.Interest;
import com.utp.quesale.profiles.domain.repositories.InterestRepository;
import com.utp.quesale.shared.domain.events.DomainEventDispatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InterestCommandHandler {
    private final InterestRepository repository;
    private final DomainEventDispatcher events;

    public InterestCommandHandler(InterestRepository repository, DomainEventDispatcher events) {
        this.repository = repository;
        this.events = events;
    }

    @Transactional
    public InterestDto handle(CreateInterestCommand command) {
        Interest interest = new Interest(command.name());
        if (repository.existsByName(interest.getName()))
            throw new IllegalStateException("Interest name already exists: " + interest.getName());
        Interest saved = repository.save(interest);
        events.dispatch(saved);
        return ProfilesDtoAssembler.toDto(saved);
    }
}
