package com.utp.quesale.profiles.application.exceptions;

import com.utp.quesale.shared.domain.exceptions.ResourceNotFoundException;

public class StudentNotFoundException extends ResourceNotFoundException {
    public StudentNotFoundException(Long studentId) {
        super("Student not found: " + studentId);
    }
}
