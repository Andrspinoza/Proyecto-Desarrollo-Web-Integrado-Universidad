package com.utp.quesale.communities.interfaces.controllers;

import com.utp.quesale.communities.application.handlers.commands.CommunityCommandHandler;
import com.utp.quesale.communities.application.handlers.queries.GetCommunityQueryHandler;
import com.utp.quesale.communities.application.handlers.queries.ListCommunitiesQueryHandler;
import com.utp.quesale.communities.application.queries.GetCommunityQuery;
import com.utp.quesale.communities.application.queries.ListCommunitiesQuery;
import com.utp.quesale.communities.interfaces.assemblers.CommunityResourceAssembler;
import com.utp.quesale.communities.interfaces.resources.CommunityResource;
import com.utp.quesale.communities.interfaces.resources.CreateCommunityResource;
import com.utp.quesale.communities.interfaces.resources.UpdateCommunityResource;
import com.utp.quesale.shared.interfaces.rest.RequestHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

// No decide permisos: pasa el studentId del header al handler y el agregado decide
@RestController
@RequestMapping("/api/v1/communities")
@Tag(name = "Communities", description = "Comunidades de estudiantes de una misma universidad")
public class CommunitiesController {
    private final CommunityCommandHandler commandHandler;
    private final GetCommunityQueryHandler getHandler;
    private final ListCommunitiesQueryHandler listHandler;

    public CommunitiesController(CommunityCommandHandler commandHandler, GetCommunityQueryHandler getHandler,
                                 ListCommunitiesQueryHandler listHandler) {
        this.commandHandler = commandHandler;
        this.getHandler = getHandler;
        this.listHandler = listHandler;
    }

    @PostMapping
    @Operation(summary = "Crear una comunidad; el creador entra como administrador")
    public ResponseEntity<CommunityResource> create(
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId,
            @Valid @RequestBody CreateCommunityResource resource) {
        var dto = commandHandler.handle(CommunityResourceAssembler.toCreateCommand(studentId, resource));
        return ResponseEntity.created(URI.create("/api/v1/communities/" + dto.id()))
                .body(CommunityResourceAssembler.toResourceFromDto(dto));
    }

    @GetMapping
    @Operation(summary = "Listar las comunidades de mi universidad")
    public List<CommunityResource> findAll(@RequestHeader(RequestHeaders.STUDENT_ID) Long studentId) {
        return listHandler.handle(new ListCommunitiesQuery(studentId)).stream()
                .map(CommunityResourceAssembler::toResourceFromDto)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una comunidad por su id")
    public CommunityResource findById(
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId,
            @PathVariable Long id) {
        return CommunityResourceAssembler.toResourceFromDto(getHandler.handle(new GetCommunityQuery(id, studentId)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar una comunidad (solo administradores)")
    public CommunityResource update(
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCommunityResource resource) {
        return CommunityResourceAssembler.toResourceFromDto(
                commandHandler.handle(CommunityResourceAssembler.toUpdateCommand(id, studentId, resource)));
    }
}
