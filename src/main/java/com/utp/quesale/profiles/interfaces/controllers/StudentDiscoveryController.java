package com.utp.quesale.profiles.interfaces.controllers;

import com.utp.quesale.profiles.application.handlers.queries.DiscoverStudentsQueryHandler;
import com.utp.quesale.shared.interfaces.rest.RequestHeaders;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1/students/discover")
public class StudentDiscoveryController {
    private final DiscoverStudentsQueryHandler handler;
    public record StudentSummary(Long id, String firstName, String lastName) {}

    public StudentDiscoveryController(DiscoverStudentsQueryHandler handler) { this.handler = handler; }

    @GetMapping
    @Operation(summary = "Buscar estudiantes verificados de mi universidad, sin estudiantes bloqueados")
    public List<StudentSummary> discover(@RequestHeader(RequestHeaders.STUDENT_ID) Long viewerId,
                                         @RequestParam(defaultValue = "") String q) {
        return handler.handle(viewerId, q).stream()
                .map(student -> new StudentSummary(student.id(), student.firstName(), student.lastName())).toList();
    }
}
