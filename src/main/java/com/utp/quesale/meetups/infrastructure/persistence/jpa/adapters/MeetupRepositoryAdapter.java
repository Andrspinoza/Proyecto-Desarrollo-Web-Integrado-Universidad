package com.utp.quesale.meetups.infrastructure.persistence.jpa.adapters;

import com.utp.quesale.iam.infrastructure.persistence.jpa.entities.UniversityJpaEntity;
import com.utp.quesale.meetups.domain.aggregates.Meetup;
import com.utp.quesale.meetups.domain.repositories.MeetupRepository;
import com.utp.quesale.meetups.infrastructure.persistence.jpa.entities.MeetupJpaEntity;
import com.utp.quesale.meetups.infrastructure.persistence.jpa.mappers.MeetupPersistenceMapper;
import com.utp.quesale.meetups.infrastructure.persistence.jpa.repositories.MeetupJpaRepository;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class MeetupRepositoryAdapter implements MeetupRepository {
    private final MeetupJpaRepository repository;
    private final MeetupPersistenceMapper mapper;
    private final EntityManager entityManager;

    public MeetupRepositoryAdapter(
            MeetupJpaRepository repository,
            MeetupPersistenceMapper mapper,
            EntityManager entityManager) {
        this.repository = repository;
        this.mapper = mapper;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public Meetup save(Meetup meetup) {
        MeetupJpaEntity entity = mapper.toEntity(
                meetup,
                entityManager.getReference(StudentJpaEntity.class, meetup.getOrganizerId()),
                entityManager.getReference(UniversityJpaEntity.class, meetup.getUniversityId()),
                studentId -> entityManager.getReference(StudentJpaEntity.class, studentId));
        MeetupJpaEntity savedEntity = repository.save(entity);
        if (meetup.getId() == null)
            meetup.assignId(savedEntity.getId());
        return meetup;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Meetup> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Optional<Meetup> findByIdForUpdate(Long id) {
        return repository.findByIdForUpdate(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Meetup> findAllByUniversityId(Long universityId) {
        return repository.findAllByUniversity_Id(universityId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Meetup> findAllByOrganizerIdOrParticipantId(Long studentId) {
        return repository.findDistinctByOrganizer_IdOrParticipations_Student_Id(studentId, studentId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}