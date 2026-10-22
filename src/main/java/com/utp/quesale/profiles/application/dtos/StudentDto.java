package com.utp.quesale.profiles.application.dtos;

import java.time.Instant;

public record StudentDto(Long id, String recordId, Long universityId, String email,
                         String firstName, String lastName, Instant createdAt,
                         ProfileDto profile) {
}
