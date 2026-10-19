package com.utp.quesale.communities.interfaces.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommunityPostResource(
        @NotBlank @Size(max = 1000) String content
) {
}
