package com.utp.quesale.meetups.domain.events;
import java.time.Instant;
public record ParticipationConfirmedEvent(Long meetupId, Long organizerId, Long studentId, Instant occurredAt) { }