package com.utp.quesale.meetups.application;

import com.utp.quesale.meetups.domain.aggregates.Meetup;
import com.utp.quesale.meetups.domain.entities.Participation;
import com.utp.quesale.meetups.domain.repositories.MeetupRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryMeetupRepository implements MeetupRepository {

    private final Map<Long, Meetup> meetups = new ConcurrentHashMap<>();
    private final AtomicLong meetupIds = new AtomicLong(100);
    private final AtomicLong participationIds = new AtomicLong(1000);

    public void add(Meetup meetup) {
        meetups.put(meetup.getId(), meetup);
    }

    @Override
    public Meetup save(Meetup meetup) {
        Long id = meetup.getId() == null ? meetupIds.incrementAndGet() : meetup.getId();
        List<Participation> participations = new ArrayList<>();
        for (Participation participation : meetup.getParticipations()) {
            long participationId = participation.getId() == null
                    ? participationIds.incrementAndGet()
                    : participation.getId();
            participations.add(new Participation(
                    participationId,
                    participation.getStudentId(),
                    participation.getStatus(),
                    participation.getRegisteredAt()));
        }
        Meetup saved = new Meetup(
                id,
                meetup.getOrganizerId(),
                meetup.getUniversityId(),
                meetup.getTitle(),
                meetup.getDescription(),
                meetup.getType(),
                meetup.getVisibility(),
                meetup.getScheduledAt(),
                meetup.getPlace(),
                meetup.getCapacity(),
                meetup.getStatus(),
                participations);
        meetups.put(id, saved);
        return saved;
    }

    @Override
    public Optional<Meetup> findById(Long id) {
        return Optional.ofNullable(meetups.get(id));
    }

    @Override
    public Optional<Meetup> findByIdForUpdate(Long id) {
        return findById(id);
    }

    @Override
    public List<Meetup> findAllByUniversityId(Long universityId) {
        return meetups.values().stream()
                .filter(meetup -> meetup.getUniversityId().equals(universityId))
                .toList();
    }

    @Override
    public List<Meetup> findAllByOrganizerIdOrParticipantId(Long studentId) {
        return meetups.values().stream()
                .filter(meetup -> meetup.getOrganizerId().equals(studentId)
                        || meetup.getParticipations().stream()
                                .anyMatch(participation -> participation.getStudentId().equals(studentId)))
                .toList();
    }
}
