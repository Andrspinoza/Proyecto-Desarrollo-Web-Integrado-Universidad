package com.utp.quesale.calls.domain.valueobjects;

import java.time.Instant;

public record CallSignal(Long fromStudentId, Long toStudentId, CallSignalType type, String payload, Instant sentAt) {

    public static final int PAYLOAD_MAX_LENGTH = 16384;

    public CallSignal {
        if (fromStudentId == null || fromStudentId <= 0)
            throw new IllegalArgumentException("Caller id must be positive");
        if (toStudentId == null || toStudentId <= 0)
            throw new IllegalArgumentException("Recipient id must be positive");
        if (fromStudentId.equals(toStudentId))
            throw new IllegalArgumentException("A student cannot call themselves");
        if (type == null) throw new IllegalArgumentException("Call signal type is required");
        if (sentAt == null) throw new IllegalArgumentException("Sent at is required");
        if (payload != null && payload.length() > PAYLOAD_MAX_LENGTH)
            throw new IllegalArgumentException("Call signal payload must be at most %d characters".formatted(PAYLOAD_MAX_LENGTH));
        if (payload != null && payload.isBlank()) payload = null;
    }
}
