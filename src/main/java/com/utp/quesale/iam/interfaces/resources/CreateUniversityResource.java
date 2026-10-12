package com.utp.quesale.iam.interfaces.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUniversityResource(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 80) String emailDomain
) {
}
