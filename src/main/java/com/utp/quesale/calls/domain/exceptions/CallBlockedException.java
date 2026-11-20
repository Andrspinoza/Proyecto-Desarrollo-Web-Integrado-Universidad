package com.utp.quesale.calls.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.BusinessRuleViolationException;

public class CallBlockedException extends BusinessRuleViolationException {
    public CallBlockedException(Long studentId, Long otherStudentId) {
        super("A block exists between students %s and %s".formatted(studentId, otherStudentId));
    }
}
