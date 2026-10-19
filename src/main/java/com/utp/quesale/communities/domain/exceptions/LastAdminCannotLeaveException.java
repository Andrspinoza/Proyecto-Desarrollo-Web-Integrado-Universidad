package com.utp.quesale.communities.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.BusinessRuleViolationException;

public class LastAdminCannotLeaveException extends BusinessRuleViolationException {
    public LastAdminCannotLeaveException() {
        super("The last admin cannot leave the community");
    }
}
