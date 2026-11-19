package com.utp.quesale.profiles.application.handlers.commands;

import com.utp.quesale.iam.interfaces.acl.IamContextFacade;
import com.utp.quesale.profiles.application.assemblers.ProfilesDtoAssembler;
import com.utp.quesale.profiles.application.commands.CreateStudentCommand;
import com.utp.quesale.profiles.application.dtos.StudentDto;
import com.utp.quesale.profiles.application.exceptions.StudentUniversityNotFoundException;
import com.utp.quesale.profiles.domain.aggregates.Student;
import com.utp.quesale.profiles.domain.repositories.StudentRepository;
import com.utp.quesale.profiles.domain.valueobjects.FullName;
import com.utp.quesale.shared.domain.events.DomainEventDispatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateStudentCommandHandler {
    private final StudentRepository repository;
    private final IamContextFacade iam;
    private final DomainEventDispatcher events;

    public CreateStudentCommandHandler(StudentRepository repository, IamContextFacade iam,
                                       DomainEventDispatcher events) {
        this.repository = repository;
        this.iam = iam;
        this.events = events;
    }

    @Transactional
    public StudentDto handle(CreateStudentCommand command) {
        Student student = new Student(command.universityId(), command.email(),
                new FullName(command.firstName(), command.lastName()));
        if (!iam.isActiveUniversity(student.getUniversityId()))
            throw new StudentUniversityNotFoundException(student.getUniversityId());
        if (!iam.universityAcceptsEmail(student.getUniversityId(), student.getEmail()))
            throw new IllegalArgumentException("Email does not belong to the university institutional domain");
        if (repository.existsByEmail(student.getEmail()))
            throw new IllegalStateException("Student email already exists: " + student.getEmail());
        Student saved = repository.save(student);
        saved.markAsRegistered();
        events.dispatch(saved);
        return ProfilesDtoAssembler.toDto(saved);
    }
}
