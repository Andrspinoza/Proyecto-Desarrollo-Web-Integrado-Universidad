package com.utp.quesale.messaging.interfaces.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendMessageResource(
        @NotNull Long recipientId,
        @NotBlank @Size(max = 1000) String content) {
}
