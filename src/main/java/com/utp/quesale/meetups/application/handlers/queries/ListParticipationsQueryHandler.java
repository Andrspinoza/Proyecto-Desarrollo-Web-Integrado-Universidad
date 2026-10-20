package com.utp.quesale.meetups.application.handlers.queries;

import com.utp.quesale.meetups.application.dtos.MeetupDto;
import com.utp.quesale.meetups.application.dtos.ParticipationDto;
import com.utp.quesale.meetups.application.queries.GetMeetupQuery;
import com.utp.quesale.meetups.application.queries.ListParticipationsQuery;
import com.utp.quesale.meetups.domain.valueobjects.ParticipationStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListParticipationsQueryHandler {

    private final GetMeetupQueryHandler getMeetupHandler;

    public ListParticipationsQueryHandler(GetMeetupQueryHandler getMeetupHandler) {
        this.getMeetupHandler = getMeetupHandler;
    }

    public List<ParticipationDto> handle(ListParticipationsQuery query) {
        MeetupDto meetup = getMeetupHandler.handle(
                new GetMeetupQuery(query.meetupId(), query.studentId()));
        return meetup.participations().stream()
                .filter(participation -> participation.status() != ParticipationStatus.DECLINED
                        && participation.status() != ParticipationStatus.WITHDRAWN)
                .toList();
    }
}
