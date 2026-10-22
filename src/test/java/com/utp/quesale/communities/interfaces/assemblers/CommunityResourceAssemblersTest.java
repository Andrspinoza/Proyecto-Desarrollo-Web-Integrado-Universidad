package com.utp.quesale.communities.interfaces.assemblers;

import com.utp.quesale.communities.application.commands.CreateCommunityCommand;
import com.utp.quesale.communities.application.commands.PublishPostCommand;
import com.utp.quesale.communities.application.commands.UpdateCommunityCommand;
import com.utp.quesale.communities.application.dtos.CommunityDto;
import com.utp.quesale.communities.application.dtos.CommunityPostDto;
import com.utp.quesale.communities.application.dtos.MembershipDto;
import com.utp.quesale.communities.interfaces.resources.CommunityPostResource;
import com.utp.quesale.communities.interfaces.resources.CommunityResource;
import com.utp.quesale.communities.interfaces.resources.CreateCommunityPostResource;
import com.utp.quesale.communities.interfaces.resources.CreateCommunityResource;
import com.utp.quesale.communities.interfaces.resources.MembershipResource;
import com.utp.quesale.communities.interfaces.resources.UpdateCommunityResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Pruebas puras: sin Spring ni base de datos
class CommunityResourceAssemblersTest {

    private static final Instant NOW = Instant.parse("2026-10-01T12:00:00Z");

    @Test
    @DisplayName("Arma el command de crear con el estudiante del header")
    void buildsCreateCommand() {
        CreateCommunityCommand command = CommunityResourceAssembler.toCreateCommand(
                10L, new CreateCommunityResource("Ajedrez", "Torneos"));

        assertEquals(new CreateCommunityCommand(10L, "Ajedrez", "Torneos"), command);
    }

    @Test
    @DisplayName("Arma el command de editar con el id de la ruta y el solicitante")
    void buildsUpdateCommand() {
        UpdateCommunityCommand command = CommunityResourceAssembler.toUpdateCommand(
                5L, 10L, new UpdateCommunityResource("Nuevo", null));

        assertEquals(new UpdateCommunityCommand(5L, 10L, "Nuevo", null), command);
    }

    @Test
    @DisplayName("Arma el command de publicar con la comunidad de la ruta y el autor")
    void buildsPublishCommand() {
        PublishPostCommand command = CommunityPostResourceAssembler.toPublishCommand(
                5L, 10L, new CreateCommunityPostResource("Hola"));

        assertEquals(new PublishPostCommand(5L, 10L, "Hola"), command);
    }

    @Test
    @DisplayName("Convierte los DTOs en resources sin perder datos")
    void convertsDtosToResources() {
        CommunityResource community = CommunityResourceAssembler.toResourceFromDto(
                new CommunityDto(5L, 1L, "Ajedrez", "Torneos", 10L, 3, NOW));
        MembershipResource membership = MembershipResourceAssembler.toResourceFromDto(
                new MembershipDto(10L, "ADMIN", NOW));
        CommunityPostResource post = CommunityPostResourceAssembler.toResourceFromDto(
                new CommunityPostDto(7L, 5L, 10L, "Hola", NOW));

        assertEquals(new CommunityResource(5L, 1L, "Ajedrez", "Torneos", 10L, 3, NOW), community);
        assertEquals(new MembershipResource(10L, "ADMIN", NOW), membership);
        assertEquals(new CommunityPostResource(7L, 5L, 10L, "Hola", NOW), post);
    }
}
