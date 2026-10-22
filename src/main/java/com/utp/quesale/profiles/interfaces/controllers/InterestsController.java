package com.utp.quesale.profiles.interfaces.controllers;

import com.utp.quesale.profiles.application.handlers.commands.InterestCommandHandler;
import com.utp.quesale.profiles.application.handlers.queries.ListInterestsQueryHandler;
import com.utp.quesale.profiles.application.queries.ListInterestsQuery;
import com.utp.quesale.profiles.interfaces.assemblers.InterestResourceAssembler;
import com.utp.quesale.profiles.interfaces.resources.CreateInterestResource;
import com.utp.quesale.profiles.interfaces.resources.InterestResource;
import com.utp.quesale.shared.interfaces.rest.RequestHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/interests")
@Tag(name = "Interests", description = "Catalogo de intereses")
public class InterestsController {
    private final InterestCommandHandler commandHandler;
    private final ListInterestsQueryHandler listHandler;

    public InterestsController(InterestCommandHandler commandHandler, ListInterestsQueryHandler listHandler) {
        this.commandHandler = commandHandler;
        this.listHandler = listHandler;
    }

    @PostMapping
    @Operation(summary = "Registrar un interes (administrador provisional)",
            description = "X-Student-Id representa provisionalmente al administrador. No se valida el rol hasta disponer de Account y JWT.")
    public ResponseEntity<InterestResource> create(@RequestHeader(RequestHeaders.STUDENT_ID) Long adminAccountId,
                                                  @Valid @RequestBody CreateInterestResource resource) {
        var dto = commandHandler.handle(InterestResourceAssembler.toCommandFromResource(resource));
        return ResponseEntity.created(URI.create("/api/v1/interests/" + dto.id()))
                .body(InterestResourceAssembler.toResourceFromDto(dto));
    }

    @GetMapping
    @Operation(summary = "Listar intereses")
    public List<InterestResource> findAll() {
        return listHandler.handle(new ListInterestsQuery()).stream()
                .map(InterestResourceAssembler::toResourceFromDto).toList();
    }
}
