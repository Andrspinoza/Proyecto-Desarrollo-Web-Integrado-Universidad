package com.utp.quesale.communities.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.ForbiddenOperationException;

public class DifferentUniversityException extends ForbiddenOperationException {
    public DifferentUniversityException(Long studentId, Long communityId) {
        super("Student %s does not belong to the university of community %s".formatted(studentId, communityId));
    }
}
