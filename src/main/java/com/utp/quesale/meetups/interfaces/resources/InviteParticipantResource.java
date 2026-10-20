package com.utp.quesale.meetups.interfaces.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InviteParticipantResource(@NotNull @Positive Long studentId) {
}
