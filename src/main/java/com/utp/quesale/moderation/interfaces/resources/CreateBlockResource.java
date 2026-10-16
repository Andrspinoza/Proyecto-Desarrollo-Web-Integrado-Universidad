package com.utp.quesale.moderation.interfaces.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateBlockResource(
        @NotNull @Positive Long blockedStudentId) {
}
