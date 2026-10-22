package com.utp.quesale.meetups.interfaces.resources;

import com.utp.quesale.meetups.domain.valueobjects.MeetupType;
import com.utp.quesale.meetups.domain.valueobjects.MeetupVisibility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateMeetupResource(
        @NotBlank @Size(max = 100) String title,
        @Size(max = 500) String description,
        @NotNull MeetupType type,
        @NotNull MeetupVisibility visibility,
        @NotNull @Future LocalDateTime scheduledAt,
        @NotNull @Valid PlaceResource place,
        @Min(2) @Max(50) int capacity) {
}
