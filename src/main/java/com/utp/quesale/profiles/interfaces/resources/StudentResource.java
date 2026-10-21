package com.utp.quesale.profiles.interfaces.resources;

import java.time.Instant;

public record StudentResource(Long id, String recordId, Long universityId, String email,
                              String firstName, String lastName, Instant createdAt,
                              ProfileResource profile) {
}
