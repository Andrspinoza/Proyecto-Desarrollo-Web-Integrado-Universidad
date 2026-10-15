package com.utp.quesale.iam.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.BusinessRuleViolationException;

public class UniversityAlreadyExistsException extends BusinessRuleViolationException {
    public UniversityAlreadyExistsException(String emailDomain) {
        super("A university with email domain %s already exists".formatted(emailDomain));
    }
}
