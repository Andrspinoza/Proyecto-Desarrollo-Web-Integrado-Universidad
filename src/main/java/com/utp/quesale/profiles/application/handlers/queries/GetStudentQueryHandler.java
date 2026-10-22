package com.utp.quesale.profiles.application.handlers.queries;

import com.utp.quesale.connections.interfaces.acl.ConnectionsContextFacade;
import com.utp.quesale.profiles.application.assemblers.ProfilesDtoAssembler;
import com.utp.quesale.profiles.application.dtos.StudentDto;
import com.utp.quesale.profiles.application.exceptions.StudentNotFoundException;
import com.utp.quesale.profiles.application.queries.GetStudentQuery;
import com.utp.quesale.profiles.domain.aggregates.Student;
import com.utp.quesale.profiles.domain.repositories.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetStudentQueryHandler {
    private final StudentRepository repository;
    private final ConnectionsContextFacade connections;

    public GetStudentQueryHandler(StudentRepository repository, ConnectionsContextFacade connections) {
        this.repository = repository;
        this.connections = connections;
    }

    public StudentDto handle(GetStudentQuery query) {
        if (query.studentId() == null || query.studentId() <= 0
                || query.viewerStudentId() == null || query.viewerStudentId() <= 0)
            throw new IllegalArgumentException("Student and viewer ids must be positive");
        Student student = repository.findById(query.studentId())
                .orElseThrow(() -> new StudentNotFoundException(query.studentId()));
        if (student.getId().equals(query.viewerStudentId()) || student.isVisibleTo(query.viewerStudentId(), false))
            return ProfilesDtoAssembler.toDto(student);
        boolean connected = student.getProfile() != null
                && connections.areConnected(query.viewerStudentId(), student.getId());
        return student.isVisibleTo(query.viewerStudentId(), connected)
                ? ProfilesDtoAssembler.toDto(student) : ProfilesDtoAssembler.toReducedDto(student);
    }
}
