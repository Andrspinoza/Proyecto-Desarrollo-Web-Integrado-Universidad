package com.utp.quesale.meetups.domain.events;
import java.time.Instant;
import java.util.List;
public record MeetupCancelledEvent(Long meetupId, Long organizerId, List<Long> participantIds, Instant occurredAt) {
    public MeetupCancelledEvent { participantIds = List.copyOf(participantIds); }
}