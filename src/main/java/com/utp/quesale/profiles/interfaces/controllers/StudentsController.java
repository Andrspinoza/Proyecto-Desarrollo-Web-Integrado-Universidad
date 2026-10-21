package com.utp.quesale.profiles.interfaces.controllers;

import com.utp.quesale.profiles.application.dtos.StudentDto;
import com.utp.quesale.profiles.application.handlers.commands.CreateStudentCommandHandler;
import com.utp.quesale.profiles.application.handlers.queries.GetStudentQueryHandler;
import com.utp.quesale.profiles.application.queries.GetStudentQuery;
import com.utp.quesale.profiles.interfaces.assemblers.StudentResourceAssembler;
import com.utp.quesale.profiles.interfaces.resources.CreateStudentResource;
import com.utp.quesale.profiles.interfaces.resources.StudentResource;
import com.utp.quesale.shared.interfaces.rest.RequestHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/students")
@Tag(name = "Students", description = "Estudiantes universitarios; identidad provisional por X-Student-Id")
public class StudentsController {
    private final CreateStudentCommandHandler createHandler;
    private final GetStudentQueryHandler getHandler;

    public StudentsController(CreateStudentCommandHandler createHandler, GetStudentQueryHandler getHandler) {
        this.createHandler = createHandler;
        this.getHandler = getHandler;
    }

    @PostMapping
    @Operation(summary = "Registrar estudiante con correo institucional",
            description = "Registro provisional con universidad y correo, sin cuenta ni JWT. No requiere X-Student-Id.")
    public ResponseEntity<StudentResource> create(@Valid @RequestBody CreateStudentResource resource) {
        StudentDto dto = createHandler.handle(StudentResourceAssembler.toCommandFromResource(resource));
        return ResponseEntity.created(URI.create("/api/v1/students/" + dto.id()))
                .body(StudentResourceAssembler.toResourceFromDto(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener estudiante respetando la visibilidad del perfil",
            description = "X-Student-Id identifica provisionalmente al visitante. Un perfil privado se reduce para terceros sin conexion.")
    public StudentResource findById(@PathVariable Long id,
                                  @RequestHeader(RequestHeaders.STUDENT_ID) Long viewerStudentId) {
        return StudentResourceAssembler.toResourceFromDto(
                getHandler.handle(new GetStudentQuery(id, viewerStudentId)));
    }
}
