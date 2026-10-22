package com.utp.quesale.messaging.interfaces;

import com.utp.quesale.messaging.application.commands.SendMessageCommand;
import com.utp.quesale.messaging.application.dtos.MessageDto;
import com.utp.quesale.messaging.application.handlers.commands.MessageCommandHandler;
import com.utp.quesale.messaging.application.handlers.queries.GetConversationQueryHandler;
import com.utp.quesale.messaging.application.queries.GetConversationQuery;
import com.utp.quesale.messaging.domain.exceptions.ConversationBlockedException;
import com.utp.quesale.messaging.domain.exceptions.StudentsNotConnectedException;
import com.utp.quesale.messaging.interfaces.controllers.MessagesController;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Prueba HTTP del controller: sin base de datos, con los handlers simulados
class MessagesControllerTest {

    private static final Instant NOW = Instant.parse("2026-10-22T10:00:00Z");
    private static final MessageDto HELLO = new MessageDto(7L, 1L, 2L, "Hola Marco", NOW);

    private final MessageCommandHandler commandHandler = mock(MessageCommandHandler.class);
    private final GetConversationQueryHandler conversationHandler = mock(GetConversationQueryHandler.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new MessagesController(commandHandler, conversationHandler))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("POST envía el mensaje con el estudiante del header y responde 201 con Location")
    void sendsMessage() throws Exception {
        when(commandHandler.handle(any(SendMessageCommand.class))).thenReturn(HELLO);

        mockMvc.perform(post("/api/v1/messages")
                        .header("X-Student-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipientId\":2,\"content\":\"Hola Marco\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/messages/conversations/2"))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.senderId").value(1))
                .andExpect(jsonPath("$.recipientId").value(2))
                .andExpect(jsonPath("$.content").value("Hola Marco"));

        verify(commandHandler).handle(new SendMessageCommand(1L, 2L, "Hola Marco"));
    }

    @Test
    @DisplayName("POST responde 400 si falta el destinatario o el contenido está vacío y no llama al handler")
    void rejectsInvalidBody() throws Exception {
        mockMvc.perform(post("/api/v1/messages")
                        .header("X-Student-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Hola\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/messages")
                        .header("X-Student-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipientId\":2,\"content\":\" \"}"))
                .andExpect(status().isBadRequest());

        verify(commandHandler, never()).handle(any(SendMessageCommand.class));
    }

    @Test
    @DisplayName("POST responde 400 si falta el header X-Student-Id")
    void requiresStudentHeader() throws Exception {
        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipientId\":2,\"content\":\"Hola\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST responde 409 si los estudiantes no están conectados")
    void conflictWhenNotConnected() throws Exception {
        when(commandHandler.handle(any(SendMessageCommand.class)))
                .thenThrow(new StudentsNotConnectedException(1L, 2L));

        mockMvc.perform(post("/api/v1/messages")
                        .header("X-Student-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipientId\":2,\"content\":\"Hola\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST responde 409 si hay un bloqueo entre los dos")
    void conflictWhenBlocked() throws Exception {
        when(commandHandler.handle(any(SendMessageCommand.class)))
                .thenThrow(new ConversationBlockedException(1L, 2L));

        mockMvc.perform(post("/api/v1/messages")
                        .header("X-Student-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipientId\":2,\"content\":\"Hola\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET conversación responde 200 con los mensajes entre el estudiante del header y el otro")
    void listsConversation() throws Exception {
        when(conversationHandler.handle(any(GetConversationQuery.class))).thenReturn(List.of(
                HELLO, new MessageDto(8L, 2L, 1L, "Hola Ana", NOW.plusSeconds(5))));

        mockMvc.perform(get("/api/v1/messages/conversations/2").header("X-Student-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].content").value("Hola Marco"))
                .andExpect(jsonPath("$[1].senderId").value(2));

        verify(conversationHandler).handle(new GetConversationQuery(1L, 2L));
    }

    @Test
    @DisplayName("GET conversación responde 200 con una lista vacía si aún no hay mensajes")
    void emptyConversation() throws Exception {
        when(conversationHandler.handle(any(GetConversationQuery.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/messages/conversations/3").header("X-Student-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET conversación responde 400 si falta el header X-Student-Id")
    void conversationRequiresStudentHeader() throws Exception {
        mockMvc.perform(get("/api/v1/messages/conversations/2"))
                .andExpect(status().isBadRequest());
    }
}
