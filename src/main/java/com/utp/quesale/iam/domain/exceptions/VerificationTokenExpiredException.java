package com.utp.quesale.iam.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.BusinessRuleViolationException;

public class VerificationTokenExpiredException extends BusinessRuleViolationException {
    public VerificationTokenExpiredException() {
        super("Verification token has expired; request a new one");
    }
}
