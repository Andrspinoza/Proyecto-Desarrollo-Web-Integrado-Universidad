package com.utp.quesale.communities.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.BusinessRuleViolationException;

public class CommunityNameAlreadyExistsException extends BusinessRuleViolationException {
    public CommunityNameAlreadyExistsException(String name) {
        super("A community named %s already exists in this university".formatted(name));
    }
}
