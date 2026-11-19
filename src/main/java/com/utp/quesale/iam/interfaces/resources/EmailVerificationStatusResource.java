package com.utp.quesale.iam.interfaces.resources;

import java.time.Instant;

public record EmailVerificationStatusResource(Long studentId, boolean verified, Instant verifiedAt) {
}
