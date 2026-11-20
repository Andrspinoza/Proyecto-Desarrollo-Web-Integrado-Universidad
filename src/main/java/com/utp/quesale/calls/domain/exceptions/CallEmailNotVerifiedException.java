package com.utp.quesale.calls.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.ForbiddenOperationException;

public class CallEmailNotVerifiedException extends ForbiddenOperationException {
    public CallEmailNotVerifiedException(Long studentId) {
        super("Student %s must verify their email before calling".formatted(studentId));
    }
}
