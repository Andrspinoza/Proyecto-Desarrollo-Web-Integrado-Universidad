package com.utp.quesale.profiles.interfaces.controllers;

import com.utp.quesale.profiles.application.handlers.commands.SkillCommandHandler;
import com.utp.quesale.profiles.application.handlers.queries.ListSkillsQueryHandler;
import com.utp.quesale.profiles.application.queries.ListSkillsQuery;
import com.utp.quesale.profiles.interfaces.assemblers.SkillResourceAssembler;
import com.utp.quesale.profiles.interfaces.resources.CreateSkillResource;
import com.utp.quesale.profiles.interfaces.resources.SkillResource;
import com.utp.quesale.shared.interfaces.rest.RequestHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/skills")
@Tag(name = "Skills", description = "Catalogo de habilidades")
public class SkillsController {
    private final SkillCommandHandler commandHandler;
    private final ListSkillsQueryHandler listHandler;

    public SkillsController(SkillCommandHandler commandHandler, ListSkillsQueryHandler listHandler) {
        this.commandHandler = commandHandler;
        this.listHandler = listHandler;
    }

    @PostMapping
    @Operation(summary = "Registrar una habilidad (administrador provisional)",
            description = "X-Student-Id representa provisionalmente al administrador. No se valida el rol hasta disponer de Account y JWT.")
    public ResponseEntity<SkillResource> create(@RequestHeader(RequestHeaders.STUDENT_ID) Long adminAccountId,
                                                  @Valid @RequestBody CreateSkillResource resource) {
        var dto = commandHandler.handle(SkillResourceAssembler.toCommandFromResource(resource));
        return ResponseEntity.created(URI.create("/api/v1/skills/" + dto.id()))
                .body(SkillResourceAssembler.toResourceFromDto(dto));
    }

    @GetMapping
    @Operation(summary = "Listar habilidades")
    public List<SkillResource> findAll() {
        return listHandler.handle(new ListSkillsQuery()).stream()
                .map(SkillResourceAssembler::toResourceFromDto).toList();
    }
}
