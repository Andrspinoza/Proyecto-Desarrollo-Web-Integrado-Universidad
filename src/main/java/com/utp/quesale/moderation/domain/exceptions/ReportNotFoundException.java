package com.utp.quesale.moderation.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.ResourceNotFoundException;

public class ReportNotFoundException extends ResourceNotFoundException {
    public ReportNotFoundException(Long reportId) {
        super(String.format("Report with ID %s not found", reportId));
    }
}
