package com.utp.quesale.communities.interfaces.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommunityResource(
        @NotBlank @Size(max = 80) String name,
        @Size(max = 500) String description
) {
}
