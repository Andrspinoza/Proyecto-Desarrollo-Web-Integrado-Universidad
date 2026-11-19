package com.utp.quesale.iam.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.ResourceNotFoundException;

public class InvalidVerificationTokenException extends ResourceNotFoundException {
    public InvalidVerificationTokenException() {
        super("Verification token is invalid");
    }
}
