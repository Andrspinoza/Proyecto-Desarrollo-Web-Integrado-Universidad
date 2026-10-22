package com.utp.quesale.meetups.application.handlers.commands;

import com.utp.quesale.meetups.application.assemblers.MeetupsDtoAssembler;
import com.utp.quesale.meetups.application.commands.AcceptInvitationCommand;
import com.utp.quesale.meetups.application.commands.CancelMeetupCommand;
import com.utp.quesale.meetups.application.commands.ConfirmParticipationCommand;
import com.utp.quesale.meetups.application.commands.CreateMeetupCommand;
import com.utp.quesale.meetups.application.commands.DeclineInvitationCommand;
import com.utp.quesale.meetups.application.commands.FinishMeetupCommand;
import com.utp.quesale.meetups.application.commands.InviteParticipantCommand;
import com.utp.quesale.meetups.application.commands.ModifyMeetupCommand;
import com.utp.quesale.meetups.application.commands.RequestParticipationCommand;
import com.utp.quesale.meetups.application.commands.WithdrawParticipationCommand;
import com.utp.quesale.meetups.application.dtos.MeetupDto;
import com.utp.quesale.meetups.domain.aggregates.Meetup;
import com.utp.quesale.meetups.domain.exceptions.MeetupAccessDeniedException;
import com.utp.quesale.meetups.domain.exceptions.MeetupNotFoundException;
import com.utp.quesale.meetups.domain.repositories.MeetupRepository;
import com.utp.quesale.meetups.domain.valueobjects.Capacity;
import com.utp.quesale.meetups.domain.valueobjects.Place;
import com.utp.quesale.profiles.interfaces.acl.ProfilesContextFacade;
import com.utp.quesale.shared.domain.events.DomainEventDispatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MeetupCommandHandler {

    private final MeetupRepository meetupRepository;
    private final DomainEventDispatcher events;
    private final ProfilesContextFacade profiles;

    public MeetupCommandHandler(
            MeetupRepository meetupRepository,
            DomainEventDispatcher events,
            ProfilesContextFacade profiles) {
        this.meetupRepository = meetupRepository;
        this.events = events;
        this.profiles = profiles;
    }

    @Transactional
    public MeetupDto handle(CreateMeetupCommand command) {
        if (command.organizerId() == null || command.organizerId() <= 0)
            throw new IllegalArgumentException("Organizer ID must be positive");
        Long universityId = profiles.universityOfStudent(command.organizerId())
                .orElseThrow(() -> new MeetupAccessDeniedException(
                        "Organizer does not have a university profile"));
        Meetup meetup = new Meetup(
                command.organizerId(),
                universityId,
                command.title(),
                command.description(),
                command.type(),
                command.visibility(),
                command.scheduledAt(),
                new Place(command.placeName(), command.placeAddress(), command.placeDistrict()),
                new Capacity(command.capacity()));
        Meetup saved = meetupRepository.save(meetup);
        events.dispatch(meetup);
        return MeetupsDtoAssembler.toDto(saved);
    }

    @Transactional
    public MeetupDto handle(ModifyMeetupCommand command) {
        Meetup meetup = requireForUpdate(command.meetupId());
        meetup.modify(
                command.title(),
                command.description(),
                command.type(),
                command.visibility(),
                command.scheduledAt(),
                new Place(command.placeName(), command.placeAddress(), command.placeDistrict()),
                new Capacity(command.capacity()),
                command.requesterId());
        return save(meetup, false);
    }

    @Transactional
    public MeetupDto handle(CancelMeetupCommand command) {
        Meetup meetup = requireForUpdate(command.meetupId());
        meetup.cancel(command.requesterId());
        return save(meetup, true);
    }

    @Transactional
    public MeetupDto handle(FinishMeetupCommand command) {
        Meetup meetup = requireForUpdate(command.meetupId());
        meetup.finish(command.requesterId(), LocalDateTime.now());
        return save(meetup, false);
    }

    @Transactional
    public MeetupDto handle(RequestParticipationCommand command) {
        Meetup meetup = requireForUpdate(command.meetupId());
        requireSameUniversity(meetup, command.studentId());
        meetup.requestParticipation(command.studentId());
        return save(meetup, true);
    }

    @Transactional
    public MeetupDto handle(ConfirmParticipationCommand command) {
        Meetup meetup = requireForUpdate(command.meetupId());
        meetup.confirmParticipation(command.studentId(), command.requesterId());
        return save(meetup, true);
    }

    @Transactional
    public MeetupDto handle(InviteParticipantCommand command) {
        Meetup meetup = requireForUpdate(command.meetupId());
        requireSameUniversity(meetup, command.studentId());
        meetup.invite(command.studentId(), command.requesterId());
        return save(meetup, true);
    }

    @Transactional
    public MeetupDto handle(AcceptInvitationCommand command) {
        Meetup meetup = requireForUpdate(command.meetupId());
        requireSameUniversity(meetup, command.studentId());
        meetup.acceptInvitation(command.studentId());
        return save(meetup, true);
    }

    @Transactional
    public MeetupDto handle(DeclineInvitationCommand command) {
        Meetup meetup = requireForUpdate(command.meetupId());
        meetup.declineInvitation(command.studentId());
        return save(meetup, true);
    }

    @Transactional
    public MeetupDto handle(WithdrawParticipationCommand command) {
        Meetup meetup = requireForUpdate(command.meetupId());
        meetup.withdraw(command.studentId());
        return save(meetup, false);
    }

    private Meetup requireForUpdate(Long meetupId) {
        return meetupRepository.findByIdForUpdate(meetupId)
                .orElseThrow(() -> new MeetupNotFoundException(meetupId));
    }

    private void requireSameUniversity(Meetup meetup, Long studentId) {
        Long studentUniversityId = profiles.universityOfStudent(studentId)
                .orElseThrow(() -> new MeetupAccessDeniedException(
                        "Student does not have a university profile"));
        if (!studentUniversityId.equals(meetup.getUniversityId()))
            throw new MeetupAccessDeniedException(
                    "Student must belong to the meetup's university");
    }

    private MeetupDto save(Meetup meetup, boolean dispatchEvents) {
        Meetup saved = meetupRepository.save(meetup);
        if (dispatchEvents)
            events.dispatch(meetup);
        return MeetupsDtoAssembler.toDto(saved);
    }
}
