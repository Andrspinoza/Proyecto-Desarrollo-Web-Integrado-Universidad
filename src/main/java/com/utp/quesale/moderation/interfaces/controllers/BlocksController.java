package com.utp.quesale.moderation.interfaces.controllers;

import com.utp.quesale.moderation.application.commands.DeleteBlockCommand;
import com.utp.quesale.moderation.application.handlers.commands.BlockCommandHandler;
import com.utp.quesale.moderation.application.handlers.queries.ListBlocksQueryHandler;
import com.utp.quesale.moderation.application.queries.ListBlocksQuery;
import com.utp.quesale.moderation.interfaces.assemblers.BlockResourceAssembler;
import com.utp.quesale.moderation.interfaces.resources.BlockResource;
import com.utp.quesale.moderation.interfaces.resources.CreateBlockResource;
import com.utp.quesale.shared.interfaces.rest.RequestHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/blocks")
@Tag(name = "Blocks", description = "Bloqueos entre estudiantes")
public class BlocksController {
    private final BlockCommandHandler commandHandler;
    private final ListBlocksQueryHandler listHandler;

    public BlocksController(BlockCommandHandler commandHandler, ListBlocksQueryHandler listHandler) {
        this.commandHandler = commandHandler;
        this.listHandler = listHandler;
    }

    @PostMapping
    @Operation(summary = "Bloquear a un estudiante")
    public ResponseEntity<BlockResource> create(
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId,
            @Valid @RequestBody CreateBlockResource resource) {
        var dto = commandHandler.handle(BlockResourceAssembler.toCreateCommand(studentId, resource));
        return ResponseEntity.created(URI.create("/api/v1/blocks/" + dto.blockedId()))
                .body(BlockResourceAssembler.toResourceFromDto(dto));
    }

    @GetMapping
    @Operation(summary = "Listar mis bloqueos")
    public List<BlockResource> findAll(@RequestHeader(RequestHeaders.STUDENT_ID) Long studentId) {
        return listHandler.handle(new ListBlocksQuery(studentId)).stream()
                .map(BlockResourceAssembler::toResourceFromDto)
                .toList();
    }

    @DeleteMapping("/{blockedStudentId}")
    @Operation(summary = "Desbloquear a un estudiante")
    public ResponseEntity<Void> delete(
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId,
            @PathVariable Long blockedStudentId) {
        commandHandler.handle(new DeleteBlockCommand(studentId, blockedStudentId));
        return ResponseEntity.noContent().build();
    }
}
