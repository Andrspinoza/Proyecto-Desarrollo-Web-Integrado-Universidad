package com.utp.quesale.profiles.domain.valueobjects;

import java.util.UUID;

public record StudentRecordId(String studentRecordId) {
    public StudentRecordId() {
        this(UUID.randomUUID().toString());
    }

    public StudentRecordId {
        if (studentRecordId == null || studentRecordId.isBlank()) {
            throw new IllegalArgumentException("Student record id cannot be null or empty");
        }
        UUID.fromString(studentRecordId);
    }
}
