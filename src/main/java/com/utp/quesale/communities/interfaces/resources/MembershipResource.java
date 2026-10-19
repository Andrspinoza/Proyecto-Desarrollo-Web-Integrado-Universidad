package com.utp.quesale.communities.interfaces.resources;

import java.time.Instant;

public record MembershipResource(Long studentId, String role, Instant joinedAt) {
}
