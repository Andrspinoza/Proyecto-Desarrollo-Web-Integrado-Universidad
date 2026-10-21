package com.utp.quesale.meetups.application.handlers.queries;

import com.utp.quesale.meetups.application.assemblers.MeetupsDtoAssembler;
import com.utp.quesale.meetups.application.dtos.MeetupDto;
import com.utp.quesale.meetups.application.queries.GetMeetupQuery;
import com.utp.quesale.meetups.domain.aggregates.Meetup;
import com.utp.quesale.meetups.domain.entities.Participation;
import com.utp.quesale.meetups.domain.exceptions.MeetupAccessDeniedException;
import com.utp.quesale.meetups.domain.exceptions.MeetupNotFoundException;
import com.utp.quesale.meetups.domain.repositories.MeetupRepository;
import com.utp.quesale.meetups.domain.valueobjects.MeetupVisibility;
import com.utp.quesale.meetups.domain.valueobjects.ParticipationStatus;
import com.utp.quesale.profiles.interfaces.acl.ProfilesContextFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetMeetupQueryHandler {

    private final MeetupRepository meetupRepository;
    private final ProfilesContextFacade profiles;

    public GetMeetupQueryHandler(MeetupRepository meetupRepository, ProfilesContextFacade profiles) {
        this.meetupRepository = meetupRepository;
        this.profiles = profiles;
    }

    public MeetupDto handle(GetMeetupQuery query) {
        if (query.meetupId() == null || query.meetupId() <= 0)
            throw new IllegalArgumentException("Meetup ID must be positive");
        validateStudentId(query.studentId());
        Meetup meetup = meetupRepository.findById(query.meetupId())
                .orElseThrow(() -> new MeetupNotFoundException(query.meetupId()));
        ensureVisibleTo(meetup, query.studentId());
        return MeetupsDtoAssembler.toDto(meetup);
    }

    void ensureVisibleTo(Meetup meetup, Long studentId) {
        if (meetup.getOrganizerId().equals(studentId))
            return;

        if (meetup.getVisibility() == MeetupVisibility.PUBLIC) {
            Long studentUniversityId = profiles.universityOfStudent(studentId)
                    .orElseThrow(() -> new MeetupAccessDeniedException(
                            "Student does not have a university profile"));
            if (meetup.getUniversityId().equals(studentUniversityId))
                return;
        } else {
            boolean invited = meetup.getParticipations().stream()
                    .filter(participation -> participation.getStudentId().equals(studentId))
                    .map(Participation::getStatus)
                    .anyMatch(status -> status == ParticipationStatus.INVITED
                            || status == ParticipationStatus.CONFIRMED);
            if (invited)
                return;
        }

        throw new MeetupNotFoundException(meetup.getId());
    }

    private void validateStudentId(Long studentId) {
        if (studentId == null || studentId <= 0)
            throw new IllegalArgumentException("Student ID must be positive");
    }
}
