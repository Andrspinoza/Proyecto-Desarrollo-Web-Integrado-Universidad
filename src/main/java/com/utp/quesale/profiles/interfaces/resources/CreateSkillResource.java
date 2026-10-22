package com.utp.quesale.profiles.interfaces.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSkillResource(@NotBlank @Size(max = 40) String name) {
}
