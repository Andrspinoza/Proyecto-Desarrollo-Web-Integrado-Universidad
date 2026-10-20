package com.utp.quesale.meetups.application.commands;

public record RequestParticipationCommand(Long meetupId, Long studentId) {
}
