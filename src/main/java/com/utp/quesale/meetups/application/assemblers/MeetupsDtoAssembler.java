package com.utp.quesale.meetups.application.assemblers;

import com.utp.quesale.meetups.application.dtos.MeetupDto;
import com.utp.quesale.meetups.application.dtos.ParticipationDto;
import com.utp.quesale.meetups.domain.aggregates.Meetup;
import com.utp.quesale.meetups.domain.entities.Participation;

public final class MeetupsDtoAssembler {

    private MeetupsDtoAssembler() {
    }

    public static MeetupDto toDto(Meetup meetup) {
        return new MeetupDto(
                meetup.getId(),
                meetup.getOrganizerId(),
                meetup.getUniversityId(),
                meetup.getTitle(),
                meetup.getDescription(),
                meetup.getType(),
                meetup.getVisibility(),
                meetup.getScheduledAt(),
                meetup.getPlace().name(),
                meetup.getPlace().address(),
                meetup.getPlace().district(),
                meetup.getCapacity().value(),
                meetup.getStatus(),
                meetup.availableSpots(),
                meetup.getParticipations().stream()
                        .map(MeetupsDtoAssembler::toDto)
                        .toList());
    }

    public static ParticipationDto toDto(Participation participation) {
        return new ParticipationDto(
                participation.getId(),
                participation.getStudentId(),
                participation.getStatus(),
                participation.getRegisteredAt());
    }
}
