package com.utp.quesale.meetups.infrastructure.persistence.jpa.repositories;

import com.utp.quesale.meetups.infrastructure.persistence.jpa.entities.MeetupJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MeetupJpaRepository extends JpaRepository<MeetupJpaEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from MeetupJpaEntity m where m.id = :id")
    Optional<MeetupJpaEntity> findByIdForUpdate(@Param("id") Long id);

    List<MeetupJpaEntity> findAllByUniversity_Id(Long universityId);

    List<MeetupJpaEntity> findDistinctByOrganizer_IdOrParticipations_Student_Id(Long organizerId, Long studentId);
}