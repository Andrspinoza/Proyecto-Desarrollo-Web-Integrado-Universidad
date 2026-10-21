package com.utp.quesale.connections.interfaces.controllers;

import com.utp.quesale.connections.application.commands.AcceptConnectionCommand;
import com.utp.quesale.connections.application.commands.CancelConnectionCommand;
import com.utp.quesale.connections.application.commands.RejectConnectionCommand;
import com.utp.quesale.connections.application.dtos.ConnectionDto;
import com.utp.quesale.connections.application.handlers.commands.ConnectionCommandHandler;
import com.utp.quesale.connections.application.handlers.queries.ListConnectionsQueryHandler;
import com.utp.quesale.connections.application.queries.ListConnectionsQuery;
import com.utp.quesale.connections.domain.valueobjects.ConnectionStatus;
import com.utp.quesale.connections.interfaces.assemblers.ConnectionResourceAssembler;
import com.utp.quesale.connections.interfaces.resources.ConnectionResource;
import com.utp.quesale.connections.interfaces.resources.CreateConnectionResource;
import com.utp.quesale.shared.interfaces.rest.RequestHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/connections")
@Tag(name = "Connections", description = "Solicitudes de conexión entre estudiantes")
public class ConnectionsController {
    private final ConnectionCommandHandler commandHandler;
    private final ListConnectionsQueryHandler listHandler;

    public ConnectionsController(ConnectionCommandHandler commandHandler,
                                 ListConnectionsQueryHandler listHandler) {
        this.commandHandler = commandHandler;
        this.listHandler = listHandler;
    }

    @PostMapping
    @Operation(summary = "Enviar una solicitud de conexión")
    public ResponseEntity<ConnectionResource> create(
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId,
            @Valid @RequestBody CreateConnectionResource resource) {
        ConnectionDto dto = commandHandler.handle(
                ConnectionResourceAssembler.toCreateCommand(studentId, resource));
        return ResponseEntity.created(URI.create("/api/v1/connections/" + dto.id()))
                .body(ConnectionResourceAssembler.toResourceFromDto(dto));
    }

    @GetMapping
    @Operation(summary = "Listar mis conexiones")
    public List<ConnectionResource> findAll(
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId,
            @RequestParam(required = false) ConnectionStatus status) {
        return listHandler.handle(new ListConnectionsQuery(studentId, status)).stream()
                .map(ConnectionResourceAssembler::toResourceFromDto)
                .toList();
    }

    @GetMapping("/received")
    @Operation(summary = "Listar solicitudes pendientes recibidas")
    public List<ConnectionResource> findReceived(
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId) {
        return listHandler.handleReceived(studentId).stream()
                .map(ConnectionResourceAssembler::toResourceFromDto)
                .toList();
    }

    @GetMapping("/sent")
    @Operation(summary = "Listar solicitudes pendientes enviadas")
    public List<ConnectionResource> findSent(
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId) {
        return listHandler.handleSent(studentId).stream()
                .map(ConnectionResourceAssembler::toResourceFromDto)
                .toList();
    }

    @PatchMapping("/{connectionId}/accept")
    @Operation(summary = "Aceptar una solicitud de conexión")
    public ConnectionResource accept(
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId,
            @PathVariable Long connectionId) {
        return ConnectionResourceAssembler.toResourceFromDto(
                commandHandler.handle(new AcceptConnectionCommand(connectionId, studentId)));
    }

    @PatchMapping("/{connectionId}/reject")
    @Operation(summary = "Rechazar una solicitud de conexión")
    public ConnectionResource reject(
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId,
            @PathVariable Long connectionId) {
        return ConnectionResourceAssembler.toResourceFromDto(
                commandHandler.handle(new RejectConnectionCommand(connectionId, studentId)));
    }

    @PatchMapping("/{connectionId}/cancel")
    @Operation(summary = "Cancelar una solicitud de conexión enviada")
    public ConnectionResource cancel(
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId,
            @PathVariable Long connectionId) {
        return ConnectionResourceAssembler.toResourceFromDto(
                commandHandler.handle(new CancelConnectionCommand(connectionId, studentId)));
    }
}
