package com.utp.quesale.moderation.interfaces.controllers;

import com.utp.quesale.moderation.application.handlers.commands.ReportCommandHandler;
import com.utp.quesale.moderation.application.handlers.queries.GetReportQueryHandler;
import com.utp.quesale.moderation.application.handlers.queries.ListReportsQueryHandler;
import com.utp.quesale.moderation.application.queries.GetReportQuery;
import com.utp.quesale.moderation.application.queries.ListReportsQuery;
import com.utp.quesale.moderation.domain.valueobjects.ReportStatus;
import com.utp.quesale.moderation.interfaces.assemblers.ReportResourceAssembler;
import com.utp.quesale.moderation.interfaces.resources.CreateReportResource;
import com.utp.quesale.moderation.interfaces.resources.ResolveReportResource;
import com.utp.quesale.moderation.interfaces.resources.ReportResource;
import com.utp.quesale.shared.interfaces.rest.RequestHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "Reportes de contenido, estudiantes y meetups")
public class ReportsController {
    private final ReportCommandHandler commandHandler;
    private final GetReportQueryHandler getHandler;
    private final ListReportsQueryHandler listHandler;

    public ReportsController(ReportCommandHandler commandHandler,
                             GetReportQueryHandler getHandler,
                             ListReportsQueryHandler listHandler) {
        this.commandHandler = commandHandler;
        this.getHandler = getHandler;
        this.listHandler = listHandler;
    }

    @PostMapping
    @Operation(summary = "Crear un reporte")
    public ResponseEntity<ReportResource> create(
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId,
            @Valid @RequestBody CreateReportResource resource) {
        var dto = commandHandler.handle(ReportResourceAssembler.toCreateCommand(studentId, resource));
        return ResponseEntity.created(URI.create("/api/v1/reports/" + dto.id()))
                .body(ReportResourceAssembler.toResourceFromDto(dto));
    }

    @GetMapping
    @Operation(summary = "Listar reportes, opcionalmente filtrados por estado")
    public List<ReportResource> findAll(@RequestParam(required = false) ReportStatus status) {
        return listHandler.handle(new ListReportsQuery(status)).stream()
                .map(ReportResourceAssembler::toResourceFromDto)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un reporte")
    public ReportResource findById(@PathVariable Long id) {
        return ReportResourceAssembler.toResourceFromDto(getHandler.handle(new GetReportQuery(id)));
    }

    @PatchMapping("/{id}/review")
    @Operation(summary = "Marcar un reporte como revisado",
            description = "El header X-Student-Id se usa provisionalmente como adminAccountId; no se valida el rol.")
    public ReportResource review(
            @PathVariable Long id,
            @RequestHeader(RequestHeaders.STUDENT_ID) Long adminAccountId,
            @Valid @RequestBody ResolveReportResource resource) {
        var dto = commandHandler.handle(
                ReportResourceAssembler.toReviewCommand(id, adminAccountId, resource));
        return ReportResourceAssembler.toResourceFromDto(dto);
    }

    @PatchMapping("/{id}/dismiss")
    @Operation(summary = "Descartar un reporte",
            description = "El header X-Student-Id se usa provisionalmente como adminAccountId; no se valida el rol.")
    public ReportResource dismiss(
            @PathVariable Long id,
            @RequestHeader(RequestHeaders.STUDENT_ID) Long adminAccountId,
            @Valid @RequestBody ResolveReportResource resource) {
        var dto = commandHandler.handle(
                ReportResourceAssembler.toDismissCommand(id, adminAccountId, resource));
        return ReportResourceAssembler.toResourceFromDto(dto);
    }
}
