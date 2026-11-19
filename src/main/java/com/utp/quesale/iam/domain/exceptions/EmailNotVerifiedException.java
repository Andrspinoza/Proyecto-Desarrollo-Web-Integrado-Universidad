package com.utp.quesale.iam.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.ForbiddenOperationException;

public class EmailNotVerifiedException extends ForbiddenOperationException {
    public EmailNotVerifiedException(Long studentId) {
        super("The email of student %s is not verified".formatted(studentId));
    }
}
