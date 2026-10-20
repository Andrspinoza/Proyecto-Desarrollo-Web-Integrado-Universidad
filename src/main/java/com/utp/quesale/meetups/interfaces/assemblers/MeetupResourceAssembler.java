package com.utp.quesale.meetups.interfaces.assemblers;

import com.utp.quesale.meetups.application.commands.CreateMeetupCommand;
import com.utp.quesale.meetups.application.commands.ModifyMeetupCommand;
import com.utp.quesale.meetups.application.dtos.MeetupDto;
import com.utp.quesale.meetups.application.dtos.ParticipationDto;
import com.utp.quesale.meetups.interfaces.resources.MeetupResource;
import com.utp.quesale.meetups.interfaces.resources.ParticipationResource;
import com.utp.quesale.meetups.interfaces.resources.PlaceResource;
import com.utp.quesale.meetups.interfaces.resources.CreateMeetupResource;
import com.utp.quesale.meetups.interfaces.resources.UpdateMeetupResource;

public final class MeetupResourceAssembler {

    private MeetupResourceAssembler() {
    }

    public static CreateMeetupCommand toCommandFromResource(
            CreateMeetupResource resource,
            Long organizerId) {
        return new CreateMeetupCommand(
                organizerId,
                resource.title(),
                resource.description(),
                resource.type(),
                resource.visibility(),
                resource.scheduledAt(),
                resource.place().name(),
                resource.place().address(),
                resource.place().district(),
                resource.capacity());
    }

    public static ModifyMeetupCommand toCommandFromResource(
            UpdateMeetupResource resource,
            Long meetupId,
            Long requesterId) {
        return new ModifyMeetupCommand(
                meetupId,
                requesterId,
                resource.title(),
                resource.description(),
                resource.type(),
                resource.visibility(),
                resource.scheduledAt(),
                resource.place().name(),
                resource.place().address(),
                resource.place().district(),
                resource.capacity());
    }

    public static MeetupResource toResourceFromDto(MeetupDto meetup) {
        return new MeetupResource(
                meetup.id(),
                meetup.organizerId(),
                meetup.universityId(),
                meetup.title(),
                meetup.description(),
                meetup.type(),
                meetup.visibility(),
                meetup.scheduledAt(),
                new PlaceResource(meetup.placeName(), meetup.placeAddress(), meetup.placeDistrict()),
                meetup.capacity(),
                meetup.status(),
                meetup.availableSpots(),
                meetup.participations().stream()
                        .map(MeetupResourceAssembler::toResourceFromDto)
                        .toList());
    }

    public static ParticipationResource toResourceFromDto(ParticipationDto participation) {
        return new ParticipationResource(
                participation.id(),
                participation.studentId(),
                participation.status(),
                participation.registeredAt());
    }
}
