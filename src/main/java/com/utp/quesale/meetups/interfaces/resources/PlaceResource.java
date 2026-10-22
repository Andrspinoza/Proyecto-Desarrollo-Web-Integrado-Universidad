package com.utp.quesale.meetups.interfaces.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlaceResource(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 200) String address,
        @NotBlank @Size(max = 60) String district) {
}
