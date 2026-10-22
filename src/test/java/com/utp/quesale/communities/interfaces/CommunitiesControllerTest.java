package com.utp.quesale.communities.interfaces;

import com.utp.quesale.communities.application.commands.CreateCommunityCommand;
import com.utp.quesale.communities.application.commands.JoinCommunityCommand;
import com.utp.quesale.communities.application.commands.LeaveCommunityCommand;
import com.utp.quesale.communities.application.commands.PromoteMemberCommand;
import com.utp.quesale.communities.application.commands.PublishPostCommand;
import com.utp.quesale.communities.application.commands.UpdateCommunityCommand;
import com.utp.quesale.communities.application.dtos.CommunityDto;
import com.utp.quesale.communities.application.dtos.CommunityPostDto;
import com.utp.quesale.communities.application.dtos.MembershipDto;
import com.utp.quesale.communities.application.handlers.commands.CommunityCommandHandler;
import com.utp.quesale.communities.application.handlers.queries.GetCommunityQueryHandler;
import com.utp.quesale.communities.application.handlers.queries.ListCommunitiesQueryHandler;
import com.utp.quesale.communities.application.handlers.queries.ListMembersQueryHandler;
import com.utp.quesale.communities.application.handlers.queries.ListPostsQueryHandler;
import com.utp.quesale.communities.application.queries.GetCommunityQuery;
import com.utp.quesale.communities.application.queries.ListCommunitiesQuery;
import com.utp.quesale.communities.application.queries.ListMembersQuery;
import com.utp.quesale.communities.application.queries.ListPostsQuery;
import com.utp.quesale.communities.domain.exceptions.CommunityNotFoundException;
import com.utp.quesale.communities.domain.exceptions.LastAdminCannotLeaveException;
import com.utp.quesale.communities.domain.exceptions.NotCommunityAdminException;
import com.utp.quesale.communities.domain.exceptions.NotMemberException;
import com.utp.quesale.communities.interfaces.controllers.CommunitiesController;
import com.utp.quesale.shared.interfaces.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommunitiesControllerTest {

    private static final Instant NOW = Instant.parse("2026-10-21T10:00:00Z");
    private static final CommunityDto CHESS = new CommunityDto(5L, 1L, "Ajedrez", "Torneos", 10L, 1, NOW);

    private final CommunityCommandHandler commandHandler = mock(CommunityCommandHandler.class);
    private final GetCommunityQueryHandler getHandler = mock(GetCommunityQueryHandler.class);
    private final ListCommunitiesQueryHandler listHandler = mock(ListCommunitiesQueryHandler.class);
    private final ListMembersQueryHandler membersHandler = mock(ListMembersQueryHandler.class);
    private final ListPostsQueryHandler postsHandler = mock(ListPostsQueryHandler.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new CommunitiesController(
                        commandHandler, getHandler, listHandler, membersHandler, postsHandler))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("POST crea la comunidad con el estudiante del header y responde 201 con Location")
    void createsCommunity() throws Exception {
        when(commandHandler.handle(any(CreateCommunityCommand.class))).thenReturn(CHESS);

        mockMvc.perform(post("/api/v1/communities")
                        .header("X-Student-Id", 10)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ajedrez\",\"description\":\"Torneos\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/communities/5"))
                .andExpect(jsonPath("$.name").value("Ajedrez"))
                .andExpect(jsonPath("$.creatorId").value(10))
                .andExpect(jsonPath("$.memberCount").value(1));

        verify(commandHandler).handle(new CreateCommunityCommand(10L, "Ajedrez", "Torneos"));
    }

    @Test
    @DisplayName("POST responde 400 si falta el nombre y no llama al handler")
    void rejectsBlankName() throws Exception {
        mockMvc.perform(post("/api/v1/communities")
                        .header("X-Student-Id", 10)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\" \"}"))
                .andExpect(status().isBadRequest());

        verify(commandHandler, never()).handle(any(CreateCommunityCommand.class));
    }

    @Test
    @DisplayName("POST responde 400 si falta el header X-Student-Id")
    void requiresStudentHeader() throws Exception {
        mockMvc.perform(post("/api/v1/communities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ajedrez\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET lista las comunidades de la universidad del estudiante")
    void listsCommunities() throws Exception {
        when(listHandler.handle(new ListCommunitiesQuery(10L))).thenReturn(List.of(CHESS));

        mockMvc.perform(get("/api/v1/communities").header("X-Student-Id", 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Ajedrez"));
    }

    @Test
    @DisplayName("GET por id devuelve la comunidad")
    void getsCommunity() throws Exception {
        when(getHandler.handle(new GetCommunityQuery(5L, 10L))).thenReturn(CHESS);

        mockMvc.perform(get("/api/v1/communities/5").header("X-Student-Id", 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    @DisplayName("GET por id responde 404 si la comunidad no existe")
    void returnsNotFoundForMissingCommunity() throws Exception {
        when(getHandler.handle(new GetCommunityQuery(99L, 10L))).thenThrow(new CommunityNotFoundException(99L));

        mockMvc.perform(get("/api/v1/communities/99").header("X-Student-Id", 10))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT modifica la comunidad con el id de la ruta")
    void updatesCommunity() throws Exception {
        when(commandHandler.handle(any(UpdateCommunityCommand.class))).thenReturn(CHESS);

        mockMvc.perform(put("/api/v1/communities/5")
                        .header("X-Student-Id", 10)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ajedrez\",\"description\":\"Torneos\"}"))
                .andExpect(status().isOk());

        verify(commandHandler).handle(new UpdateCommunityCommand(5L, 10L, "Ajedrez", "Torneos"));
    }

    @Test
    @DisplayName("PUT responde 403 si quien edita no es administrador")
    void updateForbiddenForNonAdmin() throws Exception {
        when(commandHandler.handle(any(UpdateCommunityCommand.class))).thenThrow(new NotCommunityAdminException(11L));

        mockMvc.perform(put("/api/v1/communities/5")
                        .header("X-Student-Id", 11)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Otro\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST members une al estudiante y responde 201")
    void joinsCommunity() throws Exception {
        when(commandHandler.handle(any(JoinCommunityCommand.class)))
                .thenReturn(new CommunityDto(5L, 1L, "Ajedrez", "Torneos", 10L, 2, NOW));

        mockMvc.perform(post("/api/v1/communities/5/members").header("X-Student-Id", 11))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/communities/5/members"))
                .andExpect(jsonPath("$.memberCount").value(2));

        verify(commandHandler).handle(new JoinCommunityCommand(5L, 11L));
    }

    @Test
    @DisplayName("DELETE members/me saca al estudiante y responde 204")
    void leavesCommunity() throws Exception {
        mockMvc.perform(delete("/api/v1/communities/5/members/me").header("X-Student-Id", 11))
                .andExpect(status().isNoContent());

        verify(commandHandler).handle(new LeaveCommunityCommand(5L, 11L));
    }

    @Test
    @DisplayName("DELETE members/me responde 409 si es el único administrador")
    void lastAdminCannotLeave() throws Exception {
        doThrow(new LastAdminCannotLeaveException())
                .when(commandHandler).handle(new LeaveCommunityCommand(5L, 10L));

        mockMvc.perform(delete("/api/v1/communities/5/members/me").header("X-Student-Id", 10))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET members lista los miembros con su rol")
    void listsMembers() throws Exception {
        when(membersHandler.handle(new ListMembersQuery(5L, 10L)))
                .thenReturn(List.of(new MembershipDto(10L, "ADMIN", NOW), new MembershipDto(11L, "MEMBER", NOW)));

        mockMvc.perform(get("/api/v1/communities/5/members").header("X-Student-Id", 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].role").value("ADMIN"));
    }

    @Test
    @DisplayName("PATCH promote nombra administrador al miembro de la ruta")
    void promotesMember() throws Exception {
        when(commandHandler.handle(any(PromoteMemberCommand.class))).thenReturn(CHESS);

        mockMvc.perform(patch("/api/v1/communities/5/members/11/promote").header("X-Student-Id", 10))
                .andExpect(status().isOk());

        verify(commandHandler).handle(new PromoteMemberCommand(5L, 11L, 10L));
    }

    @Test
    @DisplayName("POST posts publica y responde 201 con Location")
    void publishesPost() throws Exception {
        when(commandHandler.handle(any(PublishPostCommand.class)))
                .thenReturn(new CommunityPostDto(7L, 5L, 10L, "Hola a todos", NOW));

        mockMvc.perform(post("/api/v1/communities/5/posts")
                        .header("X-Student-Id", 10)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Hola a todos\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/communities/5/posts"))
                .andExpect(jsonPath("$.content").value("Hola a todos"))
                .andExpect(jsonPath("$.authorId").value(10));

        verify(commandHandler).handle(new PublishPostCommand(5L, 10L, "Hola a todos"));
    }

    @Test
    @DisplayName("POST posts responde 409 si quien publica no es miembro")
    void nonMemberCannotPublish() throws Exception {
        when(commandHandler.handle(any(PublishPostCommand.class))).thenThrow(new NotMemberException(12L));

        mockMvc.perform(post("/api/v1/communities/5/posts")
                        .header("X-Student-Id", 12)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Hola\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST posts responde 400 si el contenido está vacío")
    void rejectsBlankPost() throws Exception {
        mockMvc.perform(post("/api/v1/communities/5/posts")
                        .header("X-Student-Id", 10)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"\"}"))
                .andExpect(status().isBadRequest());

        verify(commandHandler, never()).handle(any(PublishPostCommand.class));
    }

    @Test
    @DisplayName("GET posts lista las publicaciones")
    void listsPosts() throws Exception {
        when(postsHandler.handle(new ListPostsQuery(5L, 10L)))
                .thenReturn(List.of(new CommunityPostDto(7L, 5L, 10L, "Hola a todos", NOW)));

        mockMvc.perform(get("/api/v1/communities/5/posts").header("X-Student-Id", 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Hola a todos"));
    }

    @Test
    @DisplayName("GET posts responde 409 si quien consulta no es miembro")
    void nonMemberCannotListPosts() throws Exception {
        when(postsHandler.handle(new ListPostsQuery(5L, 12L))).thenThrow(new NotMemberException(12L));

        mockMvc.perform(get("/api/v1/communities/5/posts").header("X-Student-Id", 12))
                .andExpect(status().isConflict());
    }
}
