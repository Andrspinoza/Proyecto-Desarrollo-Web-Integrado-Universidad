package com.utp.quesale.connections.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.ForbiddenOperationException;

public class ConnectionOperationForbiddenException extends ForbiddenOperationException {
    public ConnectionOperationForbiddenException(String message) {
        super(message);
    }
}
