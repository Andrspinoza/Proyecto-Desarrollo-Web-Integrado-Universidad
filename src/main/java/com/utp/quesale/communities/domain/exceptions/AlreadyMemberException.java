package com.utp.quesale.communities.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.BusinessRuleViolationException;

public class AlreadyMemberException extends BusinessRuleViolationException {
    public AlreadyMemberException(Long studentId) {
        super("Student %s is already a member of this community".formatted(studentId));
    }
}
