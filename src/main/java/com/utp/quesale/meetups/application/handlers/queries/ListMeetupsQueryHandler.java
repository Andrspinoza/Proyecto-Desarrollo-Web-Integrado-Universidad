package com.utp.quesale.meetups.application.handlers.queries;

import com.utp.quesale.meetups.application.assemblers.MeetupsDtoAssembler;
import com.utp.quesale.meetups.application.dtos.MeetupDto;
import com.utp.quesale.meetups.application.queries.ListMeetupsQuery;
import com.utp.quesale.meetups.domain.aggregates.Meetup;
import com.utp.quesale.meetups.domain.exceptions.MeetupAccessDeniedException;
import com.utp.quesale.meetups.domain.repositories.MeetupRepository;
import com.utp.quesale.meetups.domain.valueobjects.MeetupVisibility;
import com.utp.quesale.profiles.interfaces.acl.ProfilesContextFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListMeetupsQueryHandler {

    private final MeetupRepository meetupRepository;
    private final ProfilesContextFacade profiles;

    public ListMeetupsQueryHandler(MeetupRepository meetupRepository, ProfilesContextFacade profiles) {
        this.meetupRepository = meetupRepository;
        this.profiles = profiles;
    }

    public List<MeetupDto> handle(ListMeetupsQuery query) {
        if (query.studentId() == null || query.studentId() <= 0)
            throw new IllegalArgumentException("Student ID must be positive");
        if (query.from() != null && query.to() != null && query.from().isAfter(query.to()))
            throw new IllegalArgumentException("From date must not be after to date");

        Long universityId = profiles.universityOfStudent(query.studentId())
                .orElseThrow(() -> new MeetupAccessDeniedException(
                        "Student does not have a university profile"));

        return meetupRepository.findAllByUniversityId(universityId).stream()
                .filter(meetup -> meetup.getVisibility() == MeetupVisibility.PUBLIC)
                .filter(meetup -> query.type() == null || meetup.getType() == query.type())
                .filter(meetup -> query.status() == null || meetup.getStatus() == query.status())
                .filter(meetup -> query.from() == null || !meetup.getScheduledAt().isBefore(query.from()))
                .filter(meetup -> query.to() == null || !meetup.getScheduledAt().isAfter(query.to()))
                .map(MeetupsDtoAssembler::toDto)
                .toList();
    }
}
