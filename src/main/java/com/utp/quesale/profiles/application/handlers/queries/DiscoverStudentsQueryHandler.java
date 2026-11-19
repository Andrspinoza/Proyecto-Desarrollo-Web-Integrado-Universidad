package com.utp.quesale.profiles.application.handlers.queries;

import com.utp.quesale.iam.interfaces.acl.IamContextFacade;
import com.utp.quesale.moderation.interfaces.acl.ModerationContextFacade;
import com.utp.quesale.profiles.application.exceptions.StudentNotFoundException;
import com.utp.quesale.profiles.domain.repositories.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class DiscoverStudentsQueryHandler {
    private final StudentRepository students;
    private final ModerationContextFacade moderation;
    private final IamContextFacade iam;

    public record Result(Long id, String firstName, String lastName) {}

    public DiscoverStudentsQueryHandler(StudentRepository students, ModerationContextFacade moderation, IamContextFacade iam) {
        this.students = students;
        this.moderation = moderation;
        this.iam = iam;
    }

    public List<Result> handle(Long viewerId, String query) {
        if (viewerId == null || viewerId <= 0) throw new IllegalArgumentException("Student id must be positive");
        var viewer = students.findById(viewerId).orElseThrow(() -> new StudentNotFoundException(viewerId));
        String term = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
        if (term.length() > 100) throw new IllegalArgumentException("Search term is too long");
        return students.findAll().stream()
                .filter(student -> !student.getId().equals(viewerId))
                .filter(student -> student.getUniversityId().equals(viewer.getUniversityId()))
                .filter(student -> (student.getFullName().firstName() + " " + student.getFullName().lastName()).toLowerCase(Locale.ROOT).contains(term))
                .filter(student -> iam.isEmailVerified(student.getId()))
                .filter(student -> !moderation.isBlocked(viewerId, student.getId()))
                .limit(50)
                .map(student -> new Result(student.getId(), student.getFullName().firstName(), student.getFullName().lastName()))
                .toList();
    }
}
