package com.utp.quesale.communities.interfaces.controllers;

import com.utp.quesale.communities.application.commands.JoinCommunityCommand;
import com.utp.quesale.communities.application.commands.LeaveCommunityCommand;
import com.utp.quesale.communities.application.commands.PromoteMemberCommand;
import com.utp.quesale.communities.application.handlers.commands.CommunityCommandHandler;
import com.utp.quesale.communities.application.handlers.queries.GetCommunityQueryHandler;
import com.utp.quesale.communities.application.handlers.queries.ListCommunitiesQueryHandler;
import com.utp.quesale.communities.application.handlers.queries.ListMembersQueryHandler;
import com.utp.quesale.communities.application.handlers.queries.ListPostsQueryHandler;
import com.utp.quesale.communities.application.queries.GetCommunityQuery;
import com.utp.quesale.communities.application.queries.ListCommunitiesQuery;
import com.utp.quesale.communities.application.queries.ListMembersQuery;
import com.utp.quesale.communities.application.queries.ListPostsQuery;
import com.utp.quesale.communities.interfaces.assemblers.CommunityPostResourceAssembler;
import com.utp.quesale.communities.interfaces.assemblers.CommunityResourceAssembler;
import com.utp.quesale.communities.interfaces.assemblers.MembershipResourceAssembler;
import com.utp.quesale.communities.interfaces.resources.CommunityPostResource;
import com.utp.quesale.communities.interfaces.resources.CommunityResource;
import com.utp.quesale.communities.interfaces.resources.CreateCommunityPostResource;
import com.utp.quesale.communities.interfaces.resources.CreateCommunityResource;
import com.utp.quesale.communities.interfaces.resources.MembershipResource;
import com.utp.quesale.communities.interfaces.resources.UpdateCommunityResource;
import com.utp.quesale.shared.interfaces.rest.RequestHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/communities")
@Tag(name = "Communities", description = "Comunidades de estudiantes de una misma universidad")
public class CommunitiesController {
    private final CommunityCommandHandler commandHandler;
    private final GetCommunityQueryHandler getHandler;
    private final ListCommunitiesQueryHandler listHandler;
    private final ListMembersQueryHandler membersHandler;
    private final ListPostsQueryHandler postsHandler;

    public CommunitiesController(CommunityCommandHandler commandHandler, GetCommunityQueryHandler getHandler,
                                 ListCommunitiesQueryHandler listHandler, ListMembersQueryHandler membersHandler,
                                 ListPostsQueryHandler postsHandler) {
        this.commandHandler = commandHandler;
        this.getHandler = getHandler;
        this.listHandler = listHandler;
        this.membersHandler = membersHandler;
        this.postsHandler = postsHandler;
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

    @PostMapping("/{id}/members")
    @Operation(summary = "Unirme a una comunidad de mi universidad")
    public ResponseEntity<CommunityResource> join(
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId,
            @PathVariable Long id) {
        var dto = commandHandler.handle(new JoinCommunityCommand(id, studentId));
        return ResponseEntity.created(URI.create("/api/v1/communities/" + id + "/members"))
                .body(CommunityResourceAssembler.toResourceFromDto(dto));
    }

    @DeleteMapping("/{id}/members/me")
    @Operation(summary = "Salir de una comunidad (el único administrador no puede)")
    public ResponseEntity<Void> leave(
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId,
            @PathVariable Long id) {
        commandHandler.handle(new LeaveCommunityCommand(id, studentId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "Listar los miembros de una comunidad")
    public List<MembershipResource> findMembers(
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId,
            @PathVariable Long id) {
        return membersHandler.handle(new ListMembersQuery(id, studentId)).stream()
                .map(MembershipResourceAssembler::toResourceFromDto)
                .toList();
    }

    @PatchMapping("/{id}/members/{memberId}/promote")
    @Operation(summary = "Promover a un miembro a administrador (solo administradores)")
    public CommunityResource promote(
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId,
            @PathVariable Long id,
            @PathVariable Long memberId) {
        return CommunityResourceAssembler.toResourceFromDto(
                commandHandler.handle(new PromoteMemberCommand(id, memberId, studentId)));
    }

    @PostMapping("/{id}/posts")
    @Operation(summary = "Publicar en una comunidad (solo miembros)")
    public ResponseEntity<CommunityPostResource> publish(
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId,
            @PathVariable Long id,
            @Valid @RequestBody CreateCommunityPostResource resource) {
        var dto = commandHandler.handle(CommunityPostResourceAssembler.toPublishCommand(id, studentId, resource));
        return ResponseEntity.created(URI.create("/api/v1/communities/" + id + "/posts"))
                .body(CommunityPostResourceAssembler.toResourceFromDto(dto));
    }

    @GetMapping("/{id}/posts")
    @Operation(summary = "Ver las publicaciones de una comunidad, las más recientes primero (solo miembros)")
    public List<CommunityPostResource> findPosts(
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId,
            @PathVariable Long id) {
        return postsHandler.handle(new ListPostsQuery(id, studentId)).stream()
                .map(CommunityPostResourceAssembler::toResourceFromDto)
                .toList();
    }
}
