package com.utp.quesale.iam.interfaces.web;

import com.utp.quesale.iam.domain.exceptions.EmailNotVerifiedException;
import com.utp.quesale.iam.interfaces.acl.IamContextFacade;
import com.utp.quesale.shared.interfaces.rest.RequestHeaders;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class EmailVerifiedInterceptor implements HandlerInterceptor {

    private final IamContextFacade iam;

    public EmailVerifiedInterceptor(IamContextFacade iam) {
        this.iam = iam;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!iam.isEmailVerificationRequired()) {
            return true;
        }
        Long studentId = studentIdOf(request);
        if (studentId == null) {
            return true;
        }
        if (!iam.isEmailVerified(studentId)) {
            throw new EmailNotVerifiedException(studentId);
        }
        return true;
    }

    private static Long studentIdOf(HttpServletRequest request) {
        String header = request.getHeader(RequestHeaders.STUDENT_ID);
        if (header == null || header.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(header.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
