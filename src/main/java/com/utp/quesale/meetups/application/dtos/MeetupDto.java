package com.utp.quesale.meetups.application.dtos;

import com.utp.quesale.meetups.domain.valueobjects.MeetupStatus;
import com.utp.quesale.meetups.domain.valueobjects.MeetupType;
import com.utp.quesale.meetups.domain.valueobjects.MeetupVisibility;

import java.time.LocalDateTime;
import java.util.List;

public record MeetupDto(
        Long id,
        Long organizerId,
        Long universityId,
        String title,
        String description,
        MeetupType type,
        MeetupVisibility visibility,
        LocalDateTime scheduledAt,
        String placeName,
        String placeAddress,
        String placeDistrict,
        int capacity,
        MeetupStatus status,
        int availableSpots,
        List<ParticipationDto> participations) {

    public MeetupDto {
        participations = List.copyOf(participations);
    }
}
