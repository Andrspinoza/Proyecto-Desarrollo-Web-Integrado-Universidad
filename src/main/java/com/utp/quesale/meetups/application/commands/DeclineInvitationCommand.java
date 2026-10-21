package com.utp.quesale.meetups.application.commands;

public record DeclineInvitationCommand(Long meetupId, Long studentId) {
}
