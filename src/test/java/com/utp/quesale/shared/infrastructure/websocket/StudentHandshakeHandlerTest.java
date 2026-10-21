package com.utp.quesale.shared.infrastructure.websocket;

import org.junit.jupiter.api.Test;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;

import java.security.Principal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class StudentHandshakeHandlerTest {

    private final StudentHandshakeHandler handler = new StudentHandshakeHandler();

    private Principal userFor(String query) {
        MockHttpServletRequest servlet = new MockHttpServletRequest("GET", "/ws");
        servlet.setQueryString(query);
        servlet.setScheme("http");
        servlet.setServerName("localhost");
        servlet.setServerPort(8080);
        return handler.determineUser(new ServletServerHttpRequest(servlet), null, Map.of());
    }

    @Test
    void usesStudentIdParameterAsPrincipalName() {
        Principal principal = userFor("studentId=2");

        assertNotNull(principal);
        assertEquals("2", principal.getName());
    }

    @Test
    void connectionWithoutStudentIdHasNoUser() {
        assertNull(userFor(null));
        assertNull(userFor("other=1"));
    }

    @Test
    void rejectsBlankOrNonNumericStudentId() {
        assertNull(userFor("studentId="));
        assertNull(userFor("studentId=abc"));
    }
}
