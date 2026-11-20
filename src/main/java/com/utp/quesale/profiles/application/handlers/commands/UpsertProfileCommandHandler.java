package com.utp.quesale.profiles.application.handlers.commands;

import com.utp.quesale.profiles.application.assemblers.ProfilesDtoAssembler;
import com.utp.quesale.profiles.application.commands.UpsertProfileCommand;
import com.utp.quesale.profiles.application.dtos.StudentDto;
import com.utp.quesale.profiles.application.exceptions.StudentNotFoundException;
import com.utp.quesale.profiles.domain.aggregates.Student;
import com.utp.quesale.profiles.domain.repositories.StudentRepository;
import com.utp.quesale.profiles.domain.valueobjects.Career;
import com.utp.quesale.profiles.domain.valueobjects.StudyCycle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;

@Service
public class UpsertProfileCommandHandler {
    private static final ZoneId LIMA = ZoneId.of("America/Lima");

    private final StudentRepository repository;
    private final Clock clock;

    public UpsertProfileCommandHandler(StudentRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public StudentDto handle(UpsertProfileCommand command) {
        if (command.studentId() == null || command.studentId() <= 0)
            throw new IllegalArgumentException("Student id must be positive");
        Student student = repository.findById(command.studentId())
                .orElseThrow(() -> new StudentNotFoundException(command.studentId()));
        if (command.cycle() == null) throw new IllegalArgumentException("Study cycle is required");
        student.saveProfile(new Career(command.career()), new StudyCycle(command.cycle()),
                command.description(), command.visibility(), command.gender(), command.birthDate(),
                command.campus(), orEmpty(command.interestIds()), orEmpty(command.skillIds()),
                LocalDate.ofInstant(clock.instant(), LIMA));
        return ProfilesDtoAssembler.toOwnDto(repository.update(student));
    }

    private static Set<Long> orEmpty(Set<Long> values) {
        return values == null ? Set.of() : values;
    }
}
