package com.utp.quesale.connections.interfaces.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateConnectionResource(
        @NotNull @Positive Long recipientId) {
}
