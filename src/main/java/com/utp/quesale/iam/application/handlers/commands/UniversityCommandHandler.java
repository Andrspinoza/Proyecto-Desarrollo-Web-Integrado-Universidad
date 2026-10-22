package com.utp.quesale.iam.application.handlers.commands;

import com.utp.quesale.iam.application.assemblers.IamDtoAssembler;
import com.utp.quesale.iam.application.commands.CreateUniversityCommand;
import com.utp.quesale.iam.application.dtos.UniversityDto;
import com.utp.quesale.iam.domain.aggregates.University;
import com.utp.quesale.iam.domain.exceptions.UniversityAlreadyExistsException;
import com.utp.quesale.iam.domain.repositories.UniversityRepository;
import com.utp.quesale.iam.domain.valueobjects.EmailDomain;
import com.utp.quesale.shared.domain.events.DomainEventDispatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UniversityCommandHandler {

    private final UniversityRepository universityRepository;
    private final DomainEventDispatcher events;

    public UniversityCommandHandler(UniversityRepository universityRepository, DomainEventDispatcher events) {
        this.universityRepository = universityRepository;
        this.events = events;
    }

    @Transactional
    public UniversityDto handle(CreateUniversityCommand command) {
        EmailDomain emailDomain = new EmailDomain(command.emailDomain());
        if (universityRepository.existsByEmailDomain(emailDomain.value()))
            throw new UniversityAlreadyExistsException(emailDomain.value());
        University saved = universityRepository.save(new University(command.name(), emailDomain));
        events.dispatch(saved);
        return IamDtoAssembler.toDto(saved);
    }
}
