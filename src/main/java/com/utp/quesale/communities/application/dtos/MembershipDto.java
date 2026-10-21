package com.utp.quesale.communities.application.dtos;

import java.time.Instant;

public record MembershipDto(Long studentId, String role, Instant joinedAt) {
}
