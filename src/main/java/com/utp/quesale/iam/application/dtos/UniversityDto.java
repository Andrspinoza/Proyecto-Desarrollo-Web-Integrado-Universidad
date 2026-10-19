package com.utp.quesale.iam.application.dtos;

import java.time.Instant;

public record UniversityDto(Long id, String name, String emailDomain, boolean active, Instant createdAt) {
}
