package com.utp.quesale.communities.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.BusinessRuleViolationException;

public class NotMemberException extends BusinessRuleViolationException {
    public NotMemberException(Long studentId) {
        super("Student %s is not a member of this community".formatted(studentId));
    }
}
