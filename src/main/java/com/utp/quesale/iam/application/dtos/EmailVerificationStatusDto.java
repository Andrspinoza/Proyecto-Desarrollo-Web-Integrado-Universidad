package com.utp.quesale.iam.application.dtos;

import java.time.Instant;

public record EmailVerificationStatusDto(Long studentId, boolean verified, Instant verifiedAt) {
}
