package com.utp.quesale.meetups.infrastructure;

import com.utp.quesale.iam.infrastructure.persistence.jpa.entities.UniversityJpaEntity;
import com.utp.quesale.meetups.domain.aggregates.Meetup;
import com.utp.quesale.meetups.domain.valueobjects.MeetupType;
import com.utp.quesale.meetups.domain.valueobjects.MeetupVisibility;
import com.utp.quesale.meetups.domain.valueobjects.ParticipationStatus;
import com.utp.quesale.meetups.domain.valueobjects.Capacity;
import com.utp.quesale.meetups.domain.valueobjects.Place;
import com.utp.quesale.meetups.infrastructure.persistence.jpa.adapters.MeetupRepositoryAdapter;
import com.utp.quesale.meetups.infrastructure.persistence.jpa.mappers.MeetupPersistenceMapper;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

// Prueba el adapter sobre H2, incluyendo las FKs a estudiantes y universidades.
@DataJpaTest
@Import({MeetupRepositoryAdapter.class, MeetupPersistenceMapper.class})
class MeetupRepositoryAdapterTest {

    @Autowired
    private MeetupRepositoryAdapter meetupRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Guarda y recupera el meetup y sus participaciones")
    void savesAndReadsMeetupWithParticipations() {
        UniversityJpaEntity university = persistUniversity();
        StudentJpaEntity organizer = persistStudent(university, "jesus@utp.edu.pe", "20261001");
        StudentJpaEntity participant = persistStudent(university, "ana@utp.edu.pe", "20261002");

        Meetup meetup = new Meetup(
                organizer.getId(),
                university.getId(),
                "Repaso de algoritmos",
                "Preparación para el examen",
                MeetupType.ACADEMIC,
                MeetupVisibility.PUBLIC,
                LocalDateTime.now().plusDays(3),
                new Place("Biblioteca UTP", "Av. Petit Thouars 116", "Lima"),
                new Capacity(5));
        meetup.requestParticipation(participant.getId());

        Meetup saved = meetupRepository.save(meetup);
        entityManager.flush();
        entityManager.clear();

        Meetup found = meetupRepository.findById(saved.getId()).orElseThrow();
        assertEquals(saved.getId(), found.getId());
        assertEquals(organizer.getId(), found.getOrganizerId());
        assertEquals(university.getId(), found.getUniversityId());
        assertEquals("Repaso de algoritmos", found.getTitle());
        assertEquals("Preparación para el examen", found.getDescription());
        assertEquals(MeetupType.ACADEMIC, found.getType());
        assertEquals(MeetupVisibility.PUBLIC, found.getVisibility());
        assertEquals("Biblioteca UTP", found.getPlace().name());
        assertEquals("Av. Petit Thouars 116", found.getPlace().address());
        assertEquals("Lima", found.getPlace().district());
        assertEquals(5, found.getCapacity().value());
        assertEquals(2, found.getParticipations().size());
        assertTrue(meetupRepository.findByIdForUpdate(saved.getId()).isPresent());
    }

    @Test
    @DisplayName("Actualiza un meetup recargado sin recrear sus participaciones existentes")
    void savesUpdatedMeetupWithoutRecreatingExistingParticipations() {
        UniversityJpaEntity university = persistUniversity();
        StudentJpaEntity organizer = persistStudent(university, "jesus-update@utp.edu.pe", "20261005");
        StudentJpaEntity participant = persistStudent(university, "ana-update@utp.edu.pe", "20261006");
        Meetup meetup = new Meetup(
                organizer.getId(),
                university.getId(),
                "Repaso de estructuras",
                "Preparación para el examen",
                MeetupType.ACADEMIC,
                MeetupVisibility.PUBLIC,
                LocalDateTime.now().plusDays(3),
                new Place("Biblioteca UTP", "Av. Petit Thouars 116", "Lima"),
                new Capacity(5));

        Meetup saved = meetupRepository.save(meetup);
        entityManager.flush();
        entityManager.clear();

        Meetup reloaded = meetupRepository.findByIdForUpdate(saved.getId()).orElseThrow();
        Long organizerParticipationId = reloaded.getParticipations().stream()
                .filter(item -> item.getStudentId().equals(organizer.getId()))
                .findFirst().orElseThrow().getId();
        reloaded.requestParticipation(participant.getId());

        Meetup updated = meetupRepository.save(reloaded);
        entityManager.flush();
        entityManager.clear();

        Meetup found = meetupRepository.findById(saved.getId()).orElseThrow();
        assertEquals(2, found.getParticipations().size());
        assertEquals(organizerParticipationId, found.getParticipations().stream()
                .filter(item -> item.getStudentId().equals(organizer.getId()))
                .findFirst().orElseThrow().getId());
        assertEquals(ParticipationStatus.REQUESTED, found.getParticipations().stream()
                .filter(item -> item.getStudentId().equals(participant.getId()))
                .findFirst().orElseThrow().getStatus());
        assertEquals(organizerParticipationId, updated.getParticipations().stream()
                .filter(item -> item.getStudentId().equals(organizer.getId()))
                .findFirst().orElseThrow().getId());
        assertNotNull(updated.getParticipations().stream()
                .filter(item -> item.getStudentId().equals(participant.getId()))
                .findFirst().orElseThrow().getId());
    }

    @Test
    @DisplayName("Lista meetups por universidad y por organizador o participante")
    void findsMeetupsByUniversityAndStudent() {
        UniversityJpaEntity university = persistUniversity();
        StudentJpaEntity organizer = persistStudent(university, "jesus@utp.edu.pe", "20261003");
        StudentJpaEntity participant = persistStudent(university, "ana@utp.edu.pe", "20261004");

        Meetup meetup = new Meetup(
                organizer.getId(),
                university.getId(),
                "Grupo de estudio",
                null,
                MeetupType.ACADEMIC,
                MeetupVisibility.PUBLIC,
                LocalDateTime.now().plusDays(2),
                new Place("Campus", "Av. Principal 100", "Lima"),
                new Capacity(4));
        meetup.requestParticipation(participant.getId());
        Meetup saved = meetupRepository.save(meetup);
        entityManager.flush();
        entityManager.clear();

        assertEquals(1, meetupRepository.findAllByUniversityId(university.getId()).size());
        assertEquals(1, meetupRepository.findAllByOrganizerIdOrParticipantId(participant.getId()).size());
        assertEquals(1, meetupRepository.findAllByOrganizerIdOrParticipantId(organizer.getId()).size());
        assertTrue(meetupRepository.findById(saved.getId()).isPresent());
    }

    private UniversityJpaEntity persistUniversity() {
        UniversityJpaEntity university = new UniversityJpaEntity();
        university.setName("Universidad Tecnológica del Perú");
        university.setEmailDomain("utp.edu.pe");
        university.setActive(true);
        university.setCreatedAt(Instant.now());
        entityManager.persist(university);
        return university;
    }

    private StudentJpaEntity persistStudent(
            UniversityJpaEntity university,
            String email,
            String recordId) {
        StudentJpaEntity student = new StudentJpaEntity();
        student.setRecordId(recordId);
        student.setUniversity(university);
        student.setEmail(email);
        student.setFirstName("Estudiante");
        student.setLastName("Prueba");
        student.setCreatedAt(Instant.now());
        entityManager.persist(student);
        return student;
    }
}
