package com.utp.quesale.profiles.application.handlers.commands;

import com.utp.quesale.profiles.application.assemblers.ProfilesDtoAssembler;
import com.utp.quesale.profiles.application.commands.CreateSkillCommand;
import com.utp.quesale.profiles.application.dtos.SkillDto;
import com.utp.quesale.profiles.domain.aggregates.Skill;
import com.utp.quesale.profiles.domain.repositories.SkillRepository;
import com.utp.quesale.shared.domain.events.DomainEventDispatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SkillCommandHandler {
    private final SkillRepository repository;
    private final DomainEventDispatcher events;

    public SkillCommandHandler(SkillRepository repository, DomainEventDispatcher events) {
        this.repository = repository;
        this.events = events;
    }

    @Transactional
    public SkillDto handle(CreateSkillCommand command) {
        Skill skill = new Skill(command.name());
        if (repository.existsByName(skill.getName()))
            throw new IllegalStateException("Skill name already exists: " + skill.getName());
        Skill saved = repository.save(skill);
        events.dispatch(saved);
        return ProfilesDtoAssembler.toDto(saved);
    }
}

