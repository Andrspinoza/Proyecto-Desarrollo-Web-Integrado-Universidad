package com.utp.quesale.iam.interfaces.resources;

import java.time.Instant;

public record UniversityResource(Long id, String name, String emailDomain, boolean active, Instant createdAt) {
}
