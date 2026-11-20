package com.utp.quesale.calls.interfaces;

import com.utp.quesale.calls.application.commands.RelayCallSignalCommand;
import com.utp.quesale.calls.application.handlers.commands.CallSignalCommandHandler;
import com.utp.quesale.calls.application.handlers.queries.GetIceServersQueryHandler;
import com.utp.quesale.calls.interfaces.controllers.CallsController;
import com.utp.quesale.calls.interfaces.resources.CallErrorResource;
import com.utp.quesale.calls.interfaces.resources.CallSignalResource;
import com.utp.quesale.calls.interfaces.websocket.CallSignalingController;
import com.utp.quesale.shared.interfaces.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CallSignalingControllerTest {

    @Test
    @DisplayName("El emisor sale de la identidad del WebSocket, no del contenido del mensaje")
    void senderComesFromThePrincipal() {
        CallSignalCommandHandler handler = mock(CallSignalCommandHandler.class);
        CallSignalingController controller = new CallSignalingController(handler);

        controller.signal(new CallSignalResource(2L, "offer", "{\"sdp\":\"x\"}"), () -> "1");

        ArgumentCaptor<RelayCallSignalCommand> captor = ArgumentCaptor.forClass(RelayCallSignalCommand.class);
        verify(handler).handle(captor.capture());
        assertEquals(1L, captor.getValue().fromStudentId());
        assertEquals(2L, captor.getValue().toStudentId());
        assertEquals("offer", captor.getValue().type());
        assertEquals("{\"sdp\":\"x\"}", captor.getValue().payload());
    }

    @Test
    @DisplayName("Sin identidad en el WebSocket no se envía nada")
    void withoutPrincipalIsRejected() {
        CallSignalCommandHandler handler = mock(CallSignalCommandHandler.class);
        CallSignalingController controller = new CallSignalingController(handler);

        assertThrows(IllegalArgumentException.class,
                () -> controller.signal(new CallSignalResource(2L, "offer", null), null));
        verifyNoInteractions(handler);
    }

    @Test
    @DisplayName("Los errores vuelven al emisor con su tipo y mensaje")
    void errorsAreReturnedToTheSender() {
        CallSignalingController controller = new CallSignalingController(mock(CallSignalCommandHandler.class));

        CallErrorResource error = controller.handleError(new IllegalArgumentException("Unknown call signal type: x"));

        assertEquals("IllegalArgumentException", error.error());
        assertEquals("Unknown call signal type: x", error.message());
    }

    @Test
    @DisplayName("GET /ice-servers entrega STUN y credenciales TURN para el estudiante")
    void iceServersEndpoint() throws Exception {
        Instant now = Instant.parse("2026-11-18T12:00:00Z");
        GetIceServersQueryHandler handler = new GetIceServersQueryHandler(
                Clock.fixed(now, ZoneOffset.UTC),
                new String[]{"stun:stun.l.google.com:19302"},
                new String[]{"turn:turn.quesale.example:3478"}, "secreto-turn", 3600);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CallsController(handler))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/api/v1/calls/ice-servers").header("X-Student-Id", 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iceServers.length()").value(2))
                .andExpect(jsonPath("$.iceServers[0].urls[0]").value("stun:stun.l.google.com:19302"))
                .andExpect(jsonPath("$.iceServers[1].username").value((now.getEpochSecond() + 3600) + ":7"));

        mockMvc.perform(get("/api/v1/calls/ice-servers")).andExpect(status().isBadRequest());
    }
}
