package com.utp.quesale.notifications.domain.valueobjects;

import java.time.Instant;

public record MatchNotification(Long connectionId, Long matchedStudentId, String matchedFirstName, Instant matchedAt) {

    public MatchNotification {
        if (connectionId == null || connectionId <= 0)
            throw new IllegalArgumentException("Connection id must be positive");
        if (matchedStudentId == null || matchedStudentId <= 0)
            throw new IllegalArgumentException("Matched student id must be positive");
        if (matchedAt == null) throw new IllegalArgumentException("Matched at is required");
        matchedFirstName = matchedFirstName == null ? "" : matchedFirstName.trim();
    }
}
