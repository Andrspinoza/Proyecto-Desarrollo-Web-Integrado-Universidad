package com.utp.quesale.iam.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.ResourceNotFoundException;

public class EmailVerificationNotFoundException extends ResourceNotFoundException {
    public EmailVerificationNotFoundException(Long studentId) {
        super("Student %s has no pending email verification".formatted(studentId));
    }
}
