package com.utp.quesale.messaging.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.BusinessRuleViolationException;

public class ConversationBlockedException extends BusinessRuleViolationException {
    public ConversationBlockedException(Long studentId, Long otherStudentId) {
        super("There is a block between students %s and %s".formatted(studentId, otherStudentId));
    }
}
