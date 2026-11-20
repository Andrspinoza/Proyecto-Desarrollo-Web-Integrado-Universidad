package com.utp.quesale.iam.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.*;

class AuthRateLimitFilterTest {
    private final AuthRateLimitFilter filter =
            new AuthRateLimitFilter(new AuthRateLimiter(Clock.systemUTC(), 2, 300, 8, 900));

    private int call(String method, String path, String cloudflareAddress) throws Exception {
        var request = new MockHttpServletRequest(method, path);
        if (cloudflareAddress != null) request.addHeader("CF-Connecting-IP", cloudflareAddress);
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response.getStatus();
    }

    @Test void thirdLoginFromTheSameAddressIsRejectedWith429() throws Exception {
        assertEquals(200, call("POST", "/api/v1/auth/login", "9.9.9.9"));
        assertEquals(200, call("POST", "/api/v1/auth/register", "9.9.9.9"));
        assertEquals(429, call("POST", "/api/v1/auth/login", "9.9.9.9"));
        assertEquals(200, call("POST", "/api/v1/auth/login", "8.8.8.8"));
    }

    @Test void otherRoutesAndMethodsAreNeverLimited() throws Exception {
        for (int i = 0; i < 10; i++) {
            assertEquals(200, call("GET", "/api/v1/universities", "7.7.7.7"));
            assertEquals(200, call("GET", "/api/v1/auth/login", "7.7.7.7"));
        }
    }

    @Test void clientAddressPrefersCloudflareThenForwardedThenRemote() {
        var request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.setRemoteAddr("10.0.0.1");
        assertEquals("10.0.0.1", AuthRateLimitFilter.clientAddress(request));
        request.addHeader("X-Forwarded-For", "3.3.3.3, 4.4.4.4");
        assertEquals("3.3.3.3", AuthRateLimitFilter.clientAddress(request));
        request.addHeader("CF-Connecting-IP", "5.5.5.5");
        assertEquals("5.5.5.5", AuthRateLimitFilter.clientAddress(request));
    }
}
