package com.utp.quesale.iam.interfaces.controllers;

import com.utp.quesale.iam.application.dtos.UniversityDto;
import com.utp.quesale.iam.application.handlers.commands.UniversityCommandHandler;
import com.utp.quesale.iam.application.handlers.queries.GetUniversityQueryHandler;
import com.utp.quesale.iam.application.handlers.queries.ListUniversitiesQueryHandler;
import com.utp.quesale.iam.application.queries.GetUniversityQuery;
import com.utp.quesale.iam.application.queries.ListUniversitiesQuery;
import com.utp.quesale.iam.interfaces.assemblers.UniversityResourceAssembler;
import com.utp.quesale.iam.interfaces.resources.CreateUniversityResource;
import com.utp.quesale.iam.interfaces.resources.UniversityResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

// El controller solo traduce HTTP: arma el command o la query, llama al handler y arma el resource
@RestController
@RequestMapping("/api/v1/universities")
@Tag(name = "Universities", description = "Universidades admitidas y su dominio de correo institucional")
public class UniversitiesController {
    private final UniversityCommandHandler commandHandler;
    private final GetUniversityQueryHandler getHandler;
    private final ListUniversitiesQueryHandler listHandler;

    public UniversitiesController(UniversityCommandHandler commandHandler,
                                  GetUniversityQueryHandler getHandler,
                                  ListUniversitiesQueryHandler listHandler) {
        this.commandHandler = commandHandler;
        this.getHandler = getHandler;
        this.listHandler = listHandler;
    }

    @PostMapping
    @Operation(summary = "Registrar una universidad")
    public ResponseEntity<UniversityResource> create(@Valid @RequestBody CreateUniversityResource resource) {
        UniversityDto dto = commandHandler.handle(UniversityResourceAssembler.toCommandFromResource(resource));
        return ResponseEntity.created(URI.create("/api/v1/universities/" + dto.id()))
                .body(UniversityResourceAssembler.toResourceFromDto(dto));
    }

    @GetMapping
    @Operation(summary = "Listar universidades")
    public List<UniversityResource> findAll() {
        return listHandler.handle(new ListUniversitiesQuery()).stream()
                .map(UniversityResourceAssembler::toResourceFromDto)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una universidad")
    public UniversityResource findById(@PathVariable Long id) {
        return UniversityResourceAssembler.toResourceFromDto(getHandler.handle(new GetUniversityQuery(id)));
    }
}
