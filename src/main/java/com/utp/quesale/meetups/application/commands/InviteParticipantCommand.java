package com.utp.quesale.meetups.application.commands;

public record InviteParticipantCommand(Long meetupId, Long studentId, Long requesterId) {
}
