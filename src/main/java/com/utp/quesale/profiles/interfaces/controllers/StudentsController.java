package com.utp.quesale.profiles.interfaces.controllers;

import com.utp.quesale.profiles.application.dtos.StudentDto;
import com.utp.quesale.profiles.application.commands.UpsertProfileCommand;
import com.utp.quesale.profiles.application.handlers.commands.CreateStudentCommandHandler;
import com.utp.quesale.profiles.application.handlers.commands.UpsertProfileCommandHandler;
import com.utp.quesale.profiles.application.handlers.queries.GetStudentPhotoQueryHandler;
import com.utp.quesale.profiles.application.handlers.queries.GetStudentQueryHandler;
import com.utp.quesale.profiles.application.queries.GetStudentQuery;
import com.utp.quesale.profiles.interfaces.assemblers.StudentResourceAssembler;
import com.utp.quesale.profiles.interfaces.resources.CreateStudentResource;
import com.utp.quesale.profiles.interfaces.resources.StudentResource;
import com.utp.quesale.profiles.interfaces.resources.UpsertProfileResource;
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
    private final UpsertProfileCommandHandler profileHandler;
    private final GetStudentPhotoQueryHandler photoHandler;

    public StudentsController(CreateStudentCommandHandler createHandler, GetStudentQueryHandler getHandler,
                              UpsertProfileCommandHandler profileHandler, GetStudentPhotoQueryHandler photoHandler) {
        this.createHandler = createHandler;
        this.getHandler = getHandler;
        this.profileHandler = profileHandler;
        this.photoHandler = photoHandler;
    }

    private StudentResource withPhoto(StudentResource resource, Long viewerStudentId) {
        boolean owner = resource.id() != null && resource.id().equals(viewerStudentId);
        boolean visible = resource.profile() != null && resource.profile().visibility() != null;
        if (!owner && !visible) return resource;
        return photoHandler.keyOf(resource.id())
                .map(key -> resource.withPhotoUrl(StudentPhotosController.PHOTO_PATH + key))
                .orElse(resource);
    }

    @PostMapping
    @Operation(summary = "Registrar estudiante con correo institucional",
            description = "Registro provisional con universidad y correo, sin cuenta ni JWT. No requiere X-Student-Id.")
    public ResponseEntity<StudentResource> create(@Valid @RequestBody CreateStudentResource resource) {
        StudentDto dto = createHandler.handle(StudentResourceAssembler.toCommandFromResource(resource));
        return ResponseEntity.created(URI.create("/api/v1/students/" + dto.id()))
                .body(StudentResourceAssembler.toResourceFromDto(dto));
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener mi propio estudiante y perfil, con mi fecha de nacimiento")
    public StudentResource me(@RequestHeader(RequestHeaders.STUDENT_ID) Long studentId) {
        return withPhoto(StudentResourceAssembler.toResourceFromDto(
                getHandler.handle(new GetStudentQuery(studentId, studentId))), studentId);
    }

    @PutMapping("/me/profile")
    @Operation(summary = "Crear o actualizar mi perfil",
            description = "Carrera, ciclo, sede, género, fecha de nacimiento (se publica solo la edad), descripción, visibilidad, intereses y habilidades. Reemplaza los intereses y habilidades anteriores.")
    public StudentResource saveProfile(@RequestHeader(RequestHeaders.STUDENT_ID) Long studentId,
                                       @Valid @RequestBody UpsertProfileResource resource) {
        return withPhoto(StudentResourceAssembler.toResourceFromDto(profileHandler.handle(new UpsertProfileCommand(
                studentId, resource.career(), resource.cycle(), resource.description(), resource.visibility(),
                resource.gender(), resource.birthDate(), resource.campus(),
                resource.interestIds(), resource.skillIds()))), studentId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener estudiante respetando la visibilidad del perfil",
            description = "X-Student-Id identifica provisionalmente al visitante. Un perfil privado se reduce para terceros sin conexion.")
    public StudentResource findById(@PathVariable Long id,
                                  @RequestHeader(RequestHeaders.STUDENT_ID) Long viewerStudentId) {
        return withPhoto(StudentResourceAssembler.toResourceFromDto(
                getHandler.handle(new GetStudentQuery(id, viewerStudentId))), viewerStudentId);
    }
}
