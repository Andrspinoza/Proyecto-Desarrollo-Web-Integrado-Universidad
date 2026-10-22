package com.utp.quesale.messaging.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.BusinessRuleViolationException;

public class StudentsNotConnectedException extends BusinessRuleViolationException {
    public StudentsNotConnectedException(Long studentId, Long otherStudentId) {
        super("Students %s and %s are not connected".formatted(studentId, otherStudentId));
    }
}
