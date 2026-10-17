package com.utp.quesale.meetups.infrastructure.persistence.jpa.mappers;

import com.utp.quesale.iam.infrastructure.persistence.jpa.entities.UniversityJpaEntity;
import com.utp.quesale.meetups.domain.aggregates.Meetup;
import com.utp.quesale.meetups.domain.entities.Participation;
import com.utp.quesale.meetups.domain.valueobjects.Capacity;
import com.utp.quesale.meetups.domain.valueobjects.Place;
import com.utp.quesale.meetups.infrastructure.persistence.jpa.entities.MeetupJpaEntity;
import com.utp.quesale.meetups.infrastructure.persistence.jpa.entities.ParticipationJpaEntity;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class MeetupPersistenceMapper {

    public Meetup toDomain(MeetupJpaEntity entity) {
        List<Participation> participations = entity.getParticipations().stream()
                .map(participation -> new Participation(
                        participation.getStudent().getId(),
                        participation.getStatus(),
                        participation.getRegisteredAt()))
                .toList();

        return new Meetup(
                entity.getId(),
                entity.getOrganizer().getId(),
                entity.getUniversity().getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getType(),
                entity.getVisibility(),
                entity.getScheduledAt(),
                new Place(entity.getPlaceName(), entity.getPlaceAddress(), entity.getPlaceDistrict()),
                new Capacity(entity.getCapacity()),
                entity.getStatus(),
                participations);
    }

    public MeetupJpaEntity toEntity(
            Meetup meetup,
            StudentJpaEntity organizer,
            UniversityJpaEntity university,
            Function<Long, StudentJpaEntity> studentReference) {
        MeetupJpaEntity entity = new MeetupJpaEntity();
        entity.setId(meetup.getId());
        entity.setOrganizer(organizer);
        entity.setUniversity(university);
        entity.setTitle(meetup.getTitle());
        entity.setDescription(meetup.getDescription());
        entity.setType(meetup.getType());
        entity.setVisibility(meetup.getVisibility());
        entity.setScheduledAt(meetup.getScheduledAt());
        entity.setPlaceName(meetup.getPlace().name());
        entity.setPlaceAddress(meetup.getPlace().address());
        entity.setPlaceDistrict(meetup.getPlace().district());
        entity.setCapacity(meetup.getCapacity().value());
        entity.setStatus(meetup.getStatus());

        List<ParticipationJpaEntity> participations = meetup.getParticipations().stream()
                .map(participation -> toParticipationEntity(participation, studentReference))
                .toList();
        entity.replaceParticipations(participations);
        return entity;
    }

    private ParticipationJpaEntity toParticipationEntity(
            Participation participation,
            Function<Long, StudentJpaEntity> studentReference) {
        ParticipationJpaEntity entity = new ParticipationJpaEntity();
        entity.setStudent(studentReference.apply(participation.getStudentId()));
        entity.setStatus(participation.getStatus());
        entity.setRegisteredAt(participation.getRegisteredAt());
        return entity;
    }
}