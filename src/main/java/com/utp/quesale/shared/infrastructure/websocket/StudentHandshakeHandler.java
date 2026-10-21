package com.utp.quesale.shared.infrastructure.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;
import java.util.Map;

// Identidad provisional del socket (D-16): el id del estudiante llega como parámetro `studentId` de la
// conexión, igual que el header X-Student-Id de REST. Con JWT saldrá del token y esta clase desaparece.
public class StudentHandshakeHandler extends DefaultHandshakeHandler {

    static final String STUDENT_ID_PARAM = "studentId";

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        String value = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams()
                .getFirst(STUDENT_ID_PARAM);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            String studentId = String.valueOf(Long.parseLong(value.trim()));
            return () -> studentId;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
