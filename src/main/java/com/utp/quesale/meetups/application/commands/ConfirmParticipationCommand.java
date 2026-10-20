package com.utp.quesale.meetups.application.commands;

public record ConfirmParticipationCommand(Long meetupId, Long studentId, Long requesterId) {
}
