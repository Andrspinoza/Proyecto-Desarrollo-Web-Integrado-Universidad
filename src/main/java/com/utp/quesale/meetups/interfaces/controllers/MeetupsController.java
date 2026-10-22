package com.utp.quesale.meetups.interfaces.controllers;

import com.utp.quesale.meetups.domain.valueobjects.MeetupStatus;
import com.utp.quesale.meetups.domain.valueobjects.MeetupType;
import com.utp.quesale.meetups.application.commands.AcceptInvitationCommand;
import com.utp.quesale.meetups.application.commands.CancelMeetupCommand;
import com.utp.quesale.meetups.application.commands.ConfirmParticipationCommand;
import com.utp.quesale.meetups.application.commands.CreateMeetupCommand;
import com.utp.quesale.meetups.application.commands.DeclineInvitationCommand;
import com.utp.quesale.meetups.application.commands.FinishMeetupCommand;
import com.utp.quesale.meetups.application.commands.InviteParticipantCommand;
import com.utp.quesale.meetups.application.commands.RequestParticipationCommand;
import com.utp.quesale.meetups.application.commands.WithdrawParticipationCommand;
import com.utp.quesale.meetups.application.handlers.commands.MeetupCommandHandler;
import com.utp.quesale.meetups.application.handlers.queries.GetMeetupQueryHandler;
import com.utp.quesale.meetups.application.handlers.queries.ListMeetupsQueryHandler;
import com.utp.quesale.meetups.application.handlers.queries.ListMyMeetupsQueryHandler;
import com.utp.quesale.meetups.application.handlers.queries.ListParticipationsQueryHandler;
import com.utp.quesale.meetups.application.queries.GetMeetupQuery;
import com.utp.quesale.meetups.application.queries.ListMeetupsQuery;
import com.utp.quesale.meetups.application.queries.ListMyMeetupsQuery;
import com.utp.quesale.meetups.application.queries.ListParticipationsQuery;
import com.utp.quesale.meetups.interfaces.assemblers.MeetupResourceAssembler;
import com.utp.quesale.meetups.interfaces.resources.CreateMeetupResource;
import com.utp.quesale.meetups.interfaces.resources.InviteParticipantResource;
import com.utp.quesale.meetups.interfaces.resources.MeetupResource;
import com.utp.quesale.meetups.interfaces.resources.ParticipationResource;
import com.utp.quesale.meetups.interfaces.resources.UpdateMeetupResource;
import com.utp.quesale.shared.interfaces.rest.RequestHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/meetups")
@Tag(name = "Meetups", description = "Meetups académicos y sociales para estudiantes universitarios")
public class MeetupsController {

    private final MeetupCommandHandler commandHandler;
    private final GetMeetupQueryHandler getMeetupHandler;
    private final ListMeetupsQueryHandler listMeetupsHandler;
    private final ListMyMeetupsQueryHandler listMyMeetupsHandler;
    private final ListParticipationsQueryHandler listParticipationsHandler;

    public MeetupsController(
            MeetupCommandHandler commandHandler,
            GetMeetupQueryHandler getMeetupHandler,
            ListMeetupsQueryHandler listMeetupsHandler,
            ListMyMeetupsQueryHandler listMyMeetupsHandler,
            ListParticipationsQueryHandler listParticipationsHandler) {
        this.commandHandler = commandHandler;
        this.getMeetupHandler = getMeetupHandler;
        this.listMeetupsHandler = listMeetupsHandler;
        this.listMyMeetupsHandler = listMyMeetupsHandler;
        this.listParticipationsHandler = listParticipationsHandler;
    }

    @PostMapping
    @Operation(summary = "Crear un meetup")
    public ResponseEntity<MeetupResource> create(
            @RequestHeader(RequestHeaders.STUDENT_ID) Long organizerId,
            @Valid @RequestBody CreateMeetupResource resource) {
        CreateMeetupCommand command = MeetupResourceAssembler.toCommandFromResource(resource, organizerId);
        MeetupResource response = MeetupResourceAssembler.toResourceFromDto(commandHandler.handle(command));
        return ResponseEntity.created(URI.create("/api/v1/meetups/" + response.id())).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar meetups públicos de mi universidad")
    public List<MeetupResource> findPublic(
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId,
            @RequestParam(required = false) MeetupType type,
            @RequestParam(required = false) MeetupStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return listMeetupsHandler.handle(new ListMeetupsQuery(studentId, type, status, from, to)).stream()
                .map(MeetupResourceAssembler::toResourceFromDto)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un meetup visible")
    public MeetupResource findById(
            @PathVariable Long id,
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId) {
        return MeetupResourceAssembler.toResourceFromDto(
                getMeetupHandler.handle(new GetMeetupQuery(id, studentId)));
    }

    @GetMapping("/{id}/participations")
    @Operation(summary = "Listar participantes de un meetup")
    public List<ParticipationResource> findParticipations(
            @PathVariable Long id,
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId) {
        return listParticipationsHandler.handle(new ListParticipationsQuery(id, studentId)).stream()
                .map(MeetupResourceAssembler::toResourceFromDto)
                .toList();
    }

    @GetMapping("/mine")
    @Operation(summary = "Listar mis meetups")
    public List<MeetupResource> findMine(
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId) {
        return listMyMeetupsHandler.handle(new ListMyMeetupsQuery(studentId)).stream()
                .map(MeetupResourceAssembler::toResourceFromDto)
                .toList();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar un meetup propio")
    public MeetupResource modify(
            @PathVariable Long id,
            @RequestHeader(RequestHeaders.STUDENT_ID) Long requesterId,
            @Valid @RequestBody UpdateMeetupResource resource) {
        var command = MeetupResourceAssembler.toCommandFromResource(resource, id, requesterId);
        return MeetupResourceAssembler.toResourceFromDto(commandHandler.handle(command));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancelar un meetup propio")
    public MeetupResource cancel(
            @PathVariable Long id,
            @RequestHeader(RequestHeaders.STUDENT_ID) Long requesterId) {
        return MeetupResourceAssembler.toResourceFromDto(
                commandHandler.handle(new CancelMeetupCommand(id, requesterId)));
    }

    @PatchMapping("/{id}/finish")
    @Operation(summary = "Finalizar un meetup propio")
    public MeetupResource finish(
            @PathVariable Long id,
            @RequestHeader(RequestHeaders.STUDENT_ID) Long requesterId) {
        return MeetupResourceAssembler.toResourceFromDto(
                commandHandler.handle(new FinishMeetupCommand(id, requesterId)));
    }

    @PostMapping("/{id}/participations")
    @Operation(summary = "Solicitar participar en un meetup")
    public MeetupResource requestParticipation(
            @PathVariable Long id,
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId) {
        return MeetupResourceAssembler.toResourceFromDto(
                commandHandler.handle(new RequestParticipationCommand(id, studentId)));
    }

    @PatchMapping("/{id}/participations/{studentId}/confirm")
    @Operation(summary = "Confirmar la participación de un estudiante")
    public MeetupResource confirmParticipation(
            @PathVariable Long id,
            @PathVariable Long studentId,
            @RequestHeader(RequestHeaders.STUDENT_ID) Long requesterId) {
        return MeetupResourceAssembler.toResourceFromDto(
                commandHandler.handle(new ConfirmParticipationCommand(id, studentId, requesterId)));
    }

    @PostMapping("/{id}/invitations")
    @Operation(summary = "Invitar a un estudiante a un meetup")
    public MeetupResource invite(
            @PathVariable Long id,
            @RequestHeader(RequestHeaders.STUDENT_ID) Long requesterId,
            @Valid @RequestBody InviteParticipantResource resource) {
        return MeetupResourceAssembler.toResourceFromDto(
                commandHandler.handle(new InviteParticipantCommand(id, resource.studentId(), requesterId)));
    }

    @PatchMapping("/{id}/invitations/me/accept")
    @Operation(summary = "Aceptar mi invitación a un meetup")
    public MeetupResource acceptInvitation(
            @PathVariable Long id,
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId) {
        return MeetupResourceAssembler.toResourceFromDto(
                commandHandler.handle(new AcceptInvitationCommand(id, studentId)));
    }

    @PatchMapping("/{id}/invitations/me/decline")
    @Operation(summary = "Rechazar mi invitación a un meetup")
    public MeetupResource declineInvitation(
            @PathVariable Long id,
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId) {
        return MeetupResourceAssembler.toResourceFromDto(
                commandHandler.handle(new DeclineInvitationCommand(id, studentId)));
    }

    @PatchMapping("/{id}/participations/me/withdraw")
    @Operation(summary = "Retirarme de un meetup")
    public MeetupResource withdraw(
            @PathVariable Long id,
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId) {
        return MeetupResourceAssembler.toResourceFromDto(
                commandHandler.handle(new WithdrawParticipationCommand(id, studentId)));
    }
}
