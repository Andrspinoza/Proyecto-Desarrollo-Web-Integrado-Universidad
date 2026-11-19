package com.utp.quesale.profiles.domain.events;

import java.time.Instant;

public record StudentRegisteredEvent(Long studentId, String email, String firstName, Instant occurredAt) {
}
