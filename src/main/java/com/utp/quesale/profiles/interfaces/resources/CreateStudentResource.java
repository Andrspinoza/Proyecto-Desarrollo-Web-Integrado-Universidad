package com.utp.quesale.profiles.interfaces.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateStudentResource(
        @NotNull @Positive Long universityId,
        @NotBlank @Email @Size(max = 120) String email,
        @NotBlank @Size(max = 60) String firstName,
        @NotBlank @Size(max = 60) String lastName
) {
}
