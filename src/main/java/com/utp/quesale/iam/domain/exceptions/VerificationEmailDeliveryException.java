package com.utp.quesale.iam.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.BusinessRuleViolationException;

public class VerificationEmailDeliveryException extends BusinessRuleViolationException {
    public VerificationEmailDeliveryException() {
        super("The verification email could not be sent");
    }
}
