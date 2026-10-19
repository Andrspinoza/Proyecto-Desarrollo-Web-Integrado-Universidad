package com.utp.quesale.connections.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.ResourceNotFoundException;

public class ConnectionNotFoundException extends ResourceNotFoundException {
    public ConnectionNotFoundException(Long connectionId) {
        super(String.format("Connection with ID %s not found", connectionId));
    }
}
