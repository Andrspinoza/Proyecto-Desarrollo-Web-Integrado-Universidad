package com.utp.quesale.iam.interfaces;

import com.utp.quesale.iam.domain.exceptions.EmailNotVerifiedException;
import com.utp.quesale.iam.interfaces.acl.IamContextFacade;
import com.utp.quesale.iam.interfaces.web.EmailVerifiedInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmailVerifiedInterceptorTest {

    private IamContextFacade iam;
    private EmailVerifiedInterceptor interceptor;
    private MockHttpServletRequest request;
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @BeforeEach
    void setUp() {
        iam = mock(IamContextFacade.class);
        interceptor = new EmailVerifiedInterceptor(iam);
        request = new MockHttpServletRequest();
    }

    @Test
    @DisplayName("Con la verificación desactivada todo pasa")
    void passesWhenNotRequired() {
        when(iam.isEmailVerificationRequired()).thenReturn(false);
        request.addHeader("X-Student-Id", "5");

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    @DisplayName("Un estudiante verificado pasa")
    void verifiedStudentPasses() {
        when(iam.isEmailVerificationRequired()).thenReturn(true);
        when(iam.isEmailVerified(5L)).thenReturn(true);
        request.addHeader("X-Student-Id", "5");

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    @DisplayName("Un estudiante sin verificar recibe una excepción que se traduce en 403")
    void unverifiedStudentIsForbidden() {
        when(iam.isEmailVerificationRequired()).thenReturn(true);
        when(iam.isEmailVerified(5L)).thenReturn(false);
        request.addHeader("X-Student-Id", "5");

        assertThrows(EmailNotVerifiedException.class, () -> interceptor.preHandle(request, response, new Object()));
    }

    @Test
    @DisplayName("Sin header o con header inválido deja que decida el controlador")
    void missingOrInvalidHeaderPasses() {
        when(iam.isEmailVerificationRequired()).thenReturn(true);

        assertTrue(interceptor.preHandle(request, response, new Object()));

        request.addHeader("X-Student-Id", "no-es-numero");
        assertTrue(interceptor.preHandle(request, response, new Object()));
    }
}
