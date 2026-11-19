package com.utp.quesale.iam.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.BusinessRuleViolationException;

public class ResendTooSoonException extends BusinessRuleViolationException {
    public ResendTooSoonException(long seconds) {
        super("Wait %d seconds before requesting another verification email".formatted(seconds));
    }
}
