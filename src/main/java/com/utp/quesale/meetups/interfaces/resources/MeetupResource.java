package com.utp.quesale.meetups.interfaces.resources;

import com.utp.quesale.meetups.domain.valueobjects.MeetupStatus;
import com.utp.quesale.meetups.domain.valueobjects.MeetupType;
import com.utp.quesale.meetups.domain.valueobjects.MeetupVisibility;

import java.time.LocalDateTime;
import java.util.List;

public record MeetupResource(
        Long id,
        Long organizerId,
        Long universityId,
        String title,
        String description,
        MeetupType type,
        MeetupVisibility visibility,
        LocalDateTime scheduledAt,
        PlaceResource place,
        int capacity,
        MeetupStatus status,
        int availableSpots,
        List<ParticipationResource> participations) {

    public MeetupResource {
        participations = List.copyOf(participations);
    }
}
