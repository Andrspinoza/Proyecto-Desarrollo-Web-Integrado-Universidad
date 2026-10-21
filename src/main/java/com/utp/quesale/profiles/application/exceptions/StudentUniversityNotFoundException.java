package com.utp.quesale.profiles.application.exceptions;

import com.utp.quesale.shared.domain.exceptions.ResourceNotFoundException;

public class StudentUniversityNotFoundException extends ResourceNotFoundException {
    public StudentUniversityNotFoundException(Long universityId) {
        super("Active university not found: " + universityId);
    }
}
