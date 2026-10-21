package com.utp.quesale.communities.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.ForbiddenOperationException;

public class StudentProfileRequiredException extends ForbiddenOperationException {
    public StudentProfileRequiredException(Long studentId) {
        super("Student %s has no profile in any university".formatted(studentId));
    }
}
