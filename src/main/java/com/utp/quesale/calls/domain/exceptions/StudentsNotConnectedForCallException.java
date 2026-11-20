package com.utp.quesale.calls.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.BusinessRuleViolationException;

public class StudentsNotConnectedForCallException extends BusinessRuleViolationException {
    public StudentsNotConnectedForCallException(Long studentId, Long otherStudentId) {
        super("Students %s and %s need an accepted connection to call each other".formatted(studentId, otherStudentId));
    }
}
