package com.utp.quesale.connections.interfaces;

import com.utp.quesale.connections.application.commands.AcceptConnectionCommand;
import com.utp.quesale.connections.application.commands.CreateConnectionCommand;
import com.utp.quesale.connections.application.dtos.ConnectionDto;
import com.utp.quesale.connections.application.handlers.commands.ConnectionCommandHandler;
import com.utp.quesale.connections.application.handlers.queries.ListConnectionsQueryHandler;
import com.utp.quesale.connections.application.queries.ListConnectionsQuery;
import com.utp.quesale.connections.domain.exceptions.ConnectionNotFoundException;
import com.utp.quesale.connections.domain.valueobjects.ConnectionStatus;
import com.utp.quesale.connections.interfaces.controllers.ConnectionsController;
import com.utp.quesale.shared.interfaces.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConnectionsControllerTest {

    private final ConnectionCommandHandler commandHandler = mock(ConnectionCommandHandler.class);
    private final ListConnectionsQueryHandler listHandler = mock(ListConnectionsQueryHandler.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new ConnectionsController(commandHandler, listHandler))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void createsConnectionUsingStudentIdFromHeader() throws Exception {
        when(commandHandler.handle(any(CreateConnectionCommand.class))).thenReturn(connectionDto(4L,
                12L, 34L, ConnectionStatus.PENDING));

        mockMvc.perform(post("/api/v1/connections")
                        .header("X-Student-Id", 12)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipientId\":34}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/connections/4"))
                .andExpect(jsonPath("$.requesterId").value(12))
                .andExpect(jsonPath("$.recipientId").value(34))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(commandHandler).handle(new CreateConnectionCommand(12L, 34L));
    }

    @Test
    void validatesCreateRequestBody() throws Exception {
        mockMvc.perform(post("/api/v1/connections")
                        .header("X-Student-Id", 12)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipientId\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listsConnectionsAndPassesOptionalStatusFilter() throws Exception {
        when(listHandler.handle(new ListConnectionsQuery(12L, ConnectionStatus.PENDING)))
                .thenReturn(List.of(connectionDto(4L, 12L, 34L, ConnectionStatus.PENDING)));

        mockMvc.perform(get("/api/v1/connections?status=PENDING").header("X-Student-Id", 12))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void listsReceivedAndSentConnections() throws Exception {
        when(listHandler.handleReceived(12L))
                .thenReturn(List.of(connectionDto(4L, 34L, 12L, ConnectionStatus.PENDING)));
        when(listHandler.handleSent(12L))
                .thenReturn(List.of(connectionDto(5L, 12L, 56L, ConnectionStatus.PENDING)));

        mockMvc.perform(get("/api/v1/connections/received").header("X-Student-Id", 12))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].recipientId").value(12));
        mockMvc.perform(get("/api/v1/connections/sent").header("X-Student-Id", 12))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].requesterId").value(12));
    }

    @Test
    void acceptsConnectionAsStudentFromHeader() throws Exception {
        when(commandHandler.handle(new AcceptConnectionCommand(4L, 34L)))
                .thenReturn(connectionDto(4L, 12L, 34L, ConnectionStatus.ACCEPTED));

        mockMvc.perform(patch("/api/v1/connections/4/accept").header("X-Student-Id", 34))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void returnsNotFoundWhenConnectionDoesNotExist() throws Exception {
        doThrow(new ConnectionNotFoundException(404L))
                .when(commandHandler).handle(new AcceptConnectionCommand(404L, 34L));

        mockMvc.perform(patch("/api/v1/connections/404/accept").header("X-Student-Id", 34))
                .andExpect(status().isNotFound());
    }

    private ConnectionDto connectionDto(
            Long id, Long requesterId, Long recipientId, ConnectionStatus status) {
        return new ConnectionDto(id, requesterId, recipientId, status,
                Instant.parse("2026-10-21T10:00:00Z"),
                status == ConnectionStatus.PENDING ? null : Instant.parse("2026-10-21T10:05:00Z"));
    }
}
