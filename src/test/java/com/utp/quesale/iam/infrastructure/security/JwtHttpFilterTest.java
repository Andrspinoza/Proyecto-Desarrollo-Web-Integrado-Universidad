package com.utp.quesale.iam.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.concurrent.atomic.AtomicReference;
import jakarta.servlet.http.HttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

class JwtHttpFilterTest {
    private final JwtService jwt = new JwtService("a-secret-with-at-least-32-bytes-long");
    private final JwtHttpFilter filter = new JwtHttpFilter(jwt, true);
    @Test void missingTokenIsUnauthorized() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/v1/meetups");
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, (a, b) -> fail("must reject"));
        assertEquals(401, response.getStatus());
    }
    @Test void forgedHeaderIsReplacedBySignedIdentity() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/v1/meetups");
        request.addHeader("Authorization", "Bearer " + jwt.issue(42L));
        request.addHeader("X-Student-Id", "99");
        var id = new AtomicReference<String>();
        filter.doFilter(request, new MockHttpServletResponse(), (a, b) -> id.set(((HttpServletRequest)a).getHeader("X-Student-Id")));
        assertEquals("42", id.get());
    }
    @Test void sensitiveAdminEndpointIsForbidden() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/v1/reports");
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, (a, b) -> fail("must reject"));
        assertEquals(403, response.getStatus());
    }
}
