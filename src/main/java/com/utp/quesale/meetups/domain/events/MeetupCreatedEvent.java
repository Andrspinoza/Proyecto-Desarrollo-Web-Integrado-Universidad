package com.utp.quesale.meetups.domain.events;
import java.time.Instant;
public record MeetupCreatedEvent(Long meetupId, Long organizerId, Instant occurredAt) { }