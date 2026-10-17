package com.utp.quesale.communities.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.ForbiddenOperationException;

public class NotCommunityAdminException extends ForbiddenOperationException {
    public NotCommunityAdminException(Long studentId) {
        super("Student %s is not an admin of this community".formatted(studentId));
    }
}
