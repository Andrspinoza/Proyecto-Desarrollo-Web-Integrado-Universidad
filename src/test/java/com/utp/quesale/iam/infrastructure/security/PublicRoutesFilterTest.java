package com.utp.quesale.iam.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublicRoutesFilterTest {
    private final JwtHttpFilter filter = new JwtHttpFilter(new JwtService("a-secret-with-at-least-32-bytes-long"), true);

    private int status(String method, String path, AtomicBoolean passed) throws Exception {
        var response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest(method, path), response, (a, b) -> passed.set(true));
        return passed.get() ? 200 : response.getStatus();
    }

    @Test void photosByKeyAreReachableWithoutATokenSoImagesCanLoadInTheBrowser() throws Exception {
        var passed = new AtomicBoolean();
        assertEquals(200, status("GET", "/api/v1/photos/0b9d6f56-8c7e-4a76-9d51-3f1a2b6c7d8e", passed));
        assertTrue(passed.get());
    }

    @Test void uploadingOrDeletingAPhotoStillNeedsAToken() throws Exception {
        assertEquals(401, status("PUT", "/api/v1/students/me/photo", new AtomicBoolean()));
        assertEquals(401, status("DELETE", "/api/v1/students/me/photo", new AtomicBoolean()));
    }

    @Test void thePhotoRouteDoesNotOpenOtherPathsOrShortKeys() throws Exception {
        assertEquals(401, status("GET", "/api/v1/photos/corta", new AtomicBoolean()));
        assertEquals(401, status("GET", "/api/v1/photos/0b9d6f56-8c7e-4a76-9d51-3f1a2b6c7d8e/../students/1", new AtomicBoolean()));
        assertEquals(401, status("POST", "/api/v1/photos/0b9d6f56-8c7e-4a76-9d51-3f1a2b6c7d8e", new AtomicBoolean()));
    }

    @Test void forgotAndResetPasswordRoutesArePublic() throws Exception {
        var forgot = new AtomicBoolean();
        assertEquals(200, status("POST", "/api/v1/auth/forgot-password", forgot));
        var reset = new AtomicBoolean();
        assertEquals(200, status("POST", "/api/v1/auth/reset-password", reset));
    }
}
