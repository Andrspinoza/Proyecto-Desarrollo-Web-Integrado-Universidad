package com.utp.quesale.meetups.application.handlers.queries;

import com.utp.quesale.meetups.application.assemblers.MeetupsDtoAssembler;
import com.utp.quesale.meetups.application.dtos.MeetupDto;
import com.utp.quesale.meetups.application.queries.ListMyMeetupsQuery;
import com.utp.quesale.meetups.domain.aggregates.Meetup;
import com.utp.quesale.meetups.domain.entities.Participation;
import com.utp.quesale.meetups.domain.repositories.MeetupRepository;
import com.utp.quesale.meetups.domain.valueobjects.ParticipationStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListMyMeetupsQueryHandler {

    private final MeetupRepository meetupRepository;

    public ListMyMeetupsQueryHandler(MeetupRepository meetupRepository) {
        this.meetupRepository = meetupRepository;
    }

    public List<MeetupDto> handle(ListMyMeetupsQuery query) {
        if (query.studentId() == null || query.studentId() <= 0)
            throw new IllegalArgumentException("Student ID must be positive");

        return meetupRepository.findAllByOrganizerIdOrParticipantId(query.studentId()).stream()
                .filter(meetup -> isRelevantTo(meetup, query.studentId()))
                .map(MeetupsDtoAssembler::toDto)
                .toList();
    }

    private boolean isRelevantTo(Meetup meetup, Long studentId) {
        if (meetup.getOrganizerId().equals(studentId))
            return true;
        return meetup.getParticipations().stream()
                .filter(participation -> participation.getStudentId().equals(studentId))
                .map(Participation::getStatus)
                .anyMatch(status -> status == ParticipationStatus.REQUESTED
                        || status == ParticipationStatus.INVITED
                        || status == ParticipationStatus.CONFIRMED);
    }
}
