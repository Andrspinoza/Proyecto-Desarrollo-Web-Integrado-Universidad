package com.utp.quesale.meetups.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.ForbiddenOperationException;

public class MeetupAccessDeniedException extends ForbiddenOperationException {
    public MeetupAccessDeniedException(String message) {
        super(message);
    }
}
