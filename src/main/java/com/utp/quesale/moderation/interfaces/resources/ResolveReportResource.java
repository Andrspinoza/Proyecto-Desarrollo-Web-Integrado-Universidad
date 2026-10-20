package com.utp.quesale.moderation.interfaces.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResolveReportResource(
        @NotBlank @Size(max = 500) String resolution) {
}
