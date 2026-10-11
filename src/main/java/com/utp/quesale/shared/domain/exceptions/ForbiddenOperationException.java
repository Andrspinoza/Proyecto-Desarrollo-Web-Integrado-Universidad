package com.utp.quesale.shared.domain.exceptions;

public abstract class ForbiddenOperationException extends RuntimeException {
    protected ForbiddenOperationException(String message) {
        super(message);
    }
}
