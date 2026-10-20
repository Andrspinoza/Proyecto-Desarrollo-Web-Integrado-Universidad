package com.utp.quesale.meetups.application.commands;

public record CancelMeetupCommand(Long meetupId, Long requesterId) {
}
