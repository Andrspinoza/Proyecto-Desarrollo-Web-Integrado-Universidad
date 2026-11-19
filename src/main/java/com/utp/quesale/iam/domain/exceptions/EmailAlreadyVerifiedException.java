package com.utp.quesale.iam.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.BusinessRuleViolationException;

public class EmailAlreadyVerifiedException extends BusinessRuleViolationException {
    public EmailAlreadyVerifiedException(Long studentId) {
        super("The email of student %s is already verified".formatted(studentId));
    }
}
