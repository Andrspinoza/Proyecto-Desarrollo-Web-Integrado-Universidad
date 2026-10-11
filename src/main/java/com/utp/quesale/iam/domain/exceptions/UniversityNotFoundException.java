package com.utp.quesale.iam.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.ResourceNotFoundException;

public class UniversityNotFoundException extends ResourceNotFoundException {
    public UniversityNotFoundException(Long universityId) {
        super(String.format("University with ID %s not found", universityId));
    }
}
