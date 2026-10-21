package com.utp.quesale.moderation.interfaces;

import com.utp.quesale.moderation.application.commands.CreateBlockCommand;
import com.utp.quesale.moderation.application.commands.DeleteBlockCommand;
import com.utp.quesale.moderation.application.dtos.BlockDto;
import com.utp.quesale.moderation.application.handlers.commands.BlockCommandHandler;
import com.utp.quesale.moderation.application.handlers.queries.ListBlocksQueryHandler;
import com.utp.quesale.moderation.application.queries.ListBlocksQuery;
import com.utp.quesale.moderation.domain.exceptions.BlockNotFoundException;
import com.utp.quesale.moderation.interfaces.controllers.BlocksController;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BlocksControllerTest {

    private final BlockCommandHandler commandHandler = mock(BlockCommandHandler.class);
    private final ListBlocksQueryHandler listHandler = mock(ListBlocksQueryHandler.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new BlocksController(commandHandler, listHandler))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("POST crea un bloqueo con el estudiante del header")
    void createsBlock() throws Exception {
        when(commandHandler.handle(any(CreateBlockCommand.class)))
                .thenReturn(new BlockDto(1L, 12L, 34L, Instant.parse("2026-10-19T10:00:00Z")));

        mockMvc.perform(post("/api/v1/blocks")
                        .header("X-Student-Id", 12)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"blockedStudentId\":34}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/blocks/34"))
                .andExpect(jsonPath("$.blockerId").value(12))
                .andExpect(jsonPath("$.blockedId").value(34));

        verify(commandHandler).handle(new CreateBlockCommand(12L, 34L));
    }

    @Test
    @DisplayName("GET lista los bloqueos del estudiante del header")
    void listsBlocks() throws Exception {
        when(listHandler.handle(new ListBlocksQuery(12L)))
                .thenReturn(List.of(new BlockDto(1L, 12L, 34L, Instant.parse("2026-10-19T10:00:00Z"))));

        mockMvc.perform(get("/api/v1/blocks").header("X-Student-Id", 12))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].blockedId").value(34));
    }

    @Test
    @DisplayName("DELETE elimina el bloqueo y responde sin contenido")
    void deletesBlock() throws Exception {
        mockMvc.perform(delete("/api/v1/blocks/34").header("X-Student-Id", 12))
                .andExpect(status().isNoContent());

        verify(commandHandler).handle(new DeleteBlockCommand(12L, 34L));
    }

    @Test
    @DisplayName("DELETE responde 404 si no existe el bloqueo")
    void returnsNotFoundForMissingBlock() throws Exception {
        doThrow(new BlockNotFoundException(12L, 34L))
                .when(commandHandler).handle(new DeleteBlockCommand(12L, 34L));

        mockMvc.perform(delete("/api/v1/blocks/34").header("X-Student-Id", 12))
                .andExpect(status().isNotFound());
    }
}
