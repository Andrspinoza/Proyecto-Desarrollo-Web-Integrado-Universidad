package com.utp.quesale.meetups.domain.repositories;

import com.utp.quesale.meetups.domain.aggregates.Meetup;

import java.util.List;
import java.util.Optional;

public interface MeetupRepository {
    Meetup save(Meetup meetup);

    Optional<Meetup> findById(Long id);

    Optional<Meetup> findByIdForUpdate(Long id);

    List<Meetup> findAllByUniversityId(Long universityId);

    List<Meetup> findAllByOrganizerIdOrParticipantId(Long studentId);
}