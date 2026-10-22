package com.utp.quesale.meetups.application;

import com.utp.quesale.meetups.application.commands.AcceptInvitationCommand;
import com.utp.quesale.meetups.application.commands.CreateMeetupCommand;
import com.utp.quesale.meetups.application.commands.InviteParticipantCommand;
import com.utp.quesale.meetups.application.commands.RequestParticipationCommand;
import com.utp.quesale.meetups.application.dtos.MeetupDto;
import com.utp.quesale.meetups.application.dtos.ParticipationDto;
import com.utp.quesale.meetups.application.handlers.commands.MeetupCommandHandler;
import com.utp.quesale.meetups.application.handlers.queries.GetMeetupQueryHandler;
import com.utp.quesale.meetups.application.handlers.queries.ListMeetupsQueryHandler;
import com.utp.quesale.meetups.application.handlers.queries.ListParticipationsQueryHandler;
import com.utp.quesale.meetups.application.queries.GetMeetupQuery;
import com.utp.quesale.meetups.application.queries.ListMeetupsQuery;
import com.utp.quesale.meetups.application.queries.ListParticipationsQuery;
import com.utp.quesale.meetups.domain.aggregates.Meetup;
import com.utp.quesale.meetups.domain.entities.Participation;
import com.utp.quesale.meetups.domain.exceptions.MeetupAccessDeniedException;
import com.utp.quesale.meetups.domain.exceptions.MeetupNotFoundException;
import com.utp.quesale.meetups.domain.valueobjects.Capacity;
import com.utp.quesale.meetups.domain.valueobjects.MeetupStatus;
import com.utp.quesale.meetups.domain.valueobjects.MeetupType;
import com.utp.quesale.meetups.domain.valueobjects.MeetupVisibility;
import com.utp.quesale.meetups.domain.valueobjects.ParticipationStatus;
import com.utp.quesale.meetups.domain.valueobjects.Place;
import com.utp.quesale.shared.domain.events.DomainEventDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MeetupApplicationHandlersTest {

    private static final Long ORGANIZER_ID = 101L;
    private static final Long INVITED_ID = 102L;
    private static final Long SAME_UNIVERSITY_ID = 103L;
    private static final Long OTHER_UNIVERSITY_ID = 201L;
    private static final Long UNIVERSITY_ID = 7L;

    private InMemoryMeetupRepository repository;
    private TestProfilesContextFacade profiles;
    private AtomicInteger dispatchedAggregates;
    private MeetupCommandHandler commandHandler;
    private GetMeetupQueryHandler getMeetupHandler;
    private ListMeetupsQueryHandler listMeetupsHandler;
    private ListParticipationsQueryHandler listParticipationsHandler;

    @BeforeEach
    void setUp() {
        repository = new InMemoryMeetupRepository();
        profiles = new TestProfilesContextFacade(Map.of(
                ORGANIZER_ID, UNIVERSITY_ID,
                INVITED_ID, UNIVERSITY_ID,
                SAME_UNIVERSITY_ID, UNIVERSITY_ID,
                OTHER_UNIVERSITY_ID, 8L));
        dispatchedAggregates = new AtomicInteger();
        DomainEventDispatcher dispatcher = aggregate -> dispatchedAggregates.incrementAndGet();
        commandHandler = new MeetupCommandHandler(repository, dispatcher, profiles);
        getMeetupHandler = new GetMeetupQueryHandler(repository, profiles);
        listMeetupsHandler = new ListMeetupsQueryHandler(repository, profiles);
        listParticipationsHandler = new ListParticipationsQueryHandler(getMeetupHandler);
    }

    @Test
    @DisplayName("Crea meetup asignando la universidad desde la fachada de perfiles")
    void createsMeetupWithUniversityFromProfiles() {
        MeetupDto created = commandHandler.handle(new CreateMeetupCommand(
                ORGANIZER_ID,
                "Repaso de algoritmos",
                "Preparacion para el examen",
                MeetupType.ACADEMIC,
                MeetupVisibility.PUBLIC,
                LocalDateTime.now().plusDays(3),
                "Biblioteca UTP",
                "Av. Petit Thouars 116",
                "Lima",
                5));

        assertEquals(UNIVERSITY_ID, created.universityId());
        assertEquals(ORGANIZER_ID, created.organizerId());
        assertEquals(1, dispatchedAggregates.get());
    }

    @Test
    @DisplayName("No crea un meetup si el estudiante no tiene perfil universitario")
    void rejectsCreateWithoutUniversityProfile() {
        CreateMeetupCommand command = new CreateMeetupCommand(
                999L, "Repaso", null, MeetupType.ACADEMIC, MeetupVisibility.PUBLIC,
                LocalDateTime.now().plusDays(2), "Biblioteca", "Av. Lima 10", "Lima", 4);

        assertThrows(MeetupAccessDeniedException.class, () -> commandHandler.handle(command));
        assertEquals(0, dispatchedAggregates.get());
    }

    @Test
    @DisplayName("Rechaza solicitar participacion si el estudiante es de otra universidad")
    void rejectsParticipationRequestFromOtherUniversity() {
        repository.add(meetup(6L, UNIVERSITY_ID, MeetupVisibility.PUBLIC,
                MeetupType.SOCIAL, MeetupStatus.SCHEDULED, LocalDateTime.now().plusDays(2)));

        assertThrows(MeetupAccessDeniedException.class,
                () -> commandHandler.handle(new RequestParticipationCommand(6L, OTHER_UNIVERSITY_ID)));
    }

    @Test
    @DisplayName("Rechaza invitar a un estudiante de otra universidad")
    void rejectsInvitationToStudentFromOtherUniversity() {
        repository.add(meetup(7L, UNIVERSITY_ID, MeetupVisibility.PUBLIC,
                MeetupType.SOCIAL, MeetupStatus.SCHEDULED, LocalDateTime.now().plusDays(2)));

        assertThrows(MeetupAccessDeniedException.class,
                () -> commandHandler.handle(new InviteParticipantCommand(
                        7L, OTHER_UNIVERSITY_ID, ORGANIZER_ID)));
    }

    @Test
    @DisplayName("Rechaza aceptar invitacion si el estudiante es de otra universidad")
    void rejectsInvitationAcceptanceFromOtherUniversity() {
        Meetup meetup = privateMeetup();
        meetup.invite(OTHER_UNIVERSITY_ID, ORGANIZER_ID);
        repository.add(meetup);

        assertThrows(MeetupAccessDeniedException.class,
                () -> commandHandler.handle(new AcceptInvitationCommand(5L, OTHER_UNIVERSITY_ID)));
    }

    @Test
    @DisplayName("Lista solo meetups publicos de la universidad y aplica filtros")
    void listsPublicMeetupsForStudentUniversityWithFilters() {
        LocalDateTime scheduledAt = LocalDateTime.now().plusDays(4);
        repository.add(meetup(1L, UNIVERSITY_ID, MeetupVisibility.PUBLIC, MeetupType.ACADEMIC,
                MeetupStatus.SCHEDULED, scheduledAt));
        repository.add(meetup(2L, UNIVERSITY_ID, MeetupVisibility.PRIVATE, MeetupType.ACADEMIC,
                MeetupStatus.SCHEDULED, scheduledAt));
        repository.add(meetup(3L, 8L, MeetupVisibility.PUBLIC, MeetupType.ACADEMIC,
                MeetupStatus.SCHEDULED, scheduledAt));
        repository.add(meetup(4L, UNIVERSITY_ID, MeetupVisibility.PUBLIC, MeetupType.SOCIAL,
                MeetupStatus.SCHEDULED, scheduledAt));

        List<MeetupDto> result = listMeetupsHandler.handle(new ListMeetupsQuery(
                SAME_UNIVERSITY_ID,
                MeetupType.ACADEMIC,
                MeetupStatus.SCHEDULED,
                scheduledAt.minusHours(1),
                scheduledAt.plusHours(1)));

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
    }

    @Test
    @DisplayName("Un meetup publico solo se obtiene desde su universidad")
    void hidesPublicMeetupFromOtherUniversity() {
        repository.add(meetup(1L, UNIVERSITY_ID, MeetupVisibility.PUBLIC,
                MeetupType.SOCIAL, MeetupStatus.SCHEDULED, LocalDateTime.now().plusDays(2)));

        assertThrows(MeetupNotFoundException.class,
                () -> getMeetupHandler.handle(new GetMeetupQuery(1L, OTHER_UNIVERSITY_ID)));
    }

    @Test
    @DisplayName("Un meetup privado es visible para su invitado, pero no para otros estudiantes")
    void restrictsPrivateMeetupVisibility() {
        repository.add(privateMeetup());

        assertEquals(5L, getMeetupHandler.handle(new GetMeetupQuery(5L, INVITED_ID)).id());
        assertThrows(MeetupNotFoundException.class,
                () -> getMeetupHandler.handle(new GetMeetupQuery(5L, SAME_UNIVERSITY_ID)));
    }

    @Test
    @DisplayName("Lista las participaciones activas de un meetup visible")
    void listsActiveParticipations() {
        repository.add(privateMeetup());

        List<ParticipationDto> result = listParticipationsHandler.handle(
                new ListParticipationsQuery(5L, INVITED_ID));

        assertEquals(2, result.size());
        assertEquals(ParticipationStatus.CONFIRMED, result.get(0).status());
        assertEquals(ParticipationStatus.INVITED, result.get(1).status());
    }

    private Meetup privateMeetup() {
        Instant registeredAt = Instant.now();
        return new Meetup(
                5L,
                ORGANIZER_ID,
                UNIVERSITY_ID,
                "Cita universitaria",
                null,
                MeetupType.SOCIAL,
                MeetupVisibility.PRIVATE,
                LocalDateTime.now().plusDays(2),
                new Place("Parque", "Av. Lima 100", "Lima"),
                new Capacity(2),
                MeetupStatus.SCHEDULED,
                List.of(
                        new Participation(100L, ORGANIZER_ID, ParticipationStatus.CONFIRMED, registeredAt),
                        new Participation(101L, INVITED_ID, ParticipationStatus.INVITED, registeredAt)));
    }

    private Meetup meetup(
            Long id,
            Long universityId,
            MeetupVisibility visibility,
            MeetupType type,
            MeetupStatus status,
            LocalDateTime scheduledAt) {
        return new Meetup(
                id,
                ORGANIZER_ID,
                universityId,
                "Meetup " + id,
                null,
                type,
                visibility,
                scheduledAt,
                new Place("Biblioteca", "Av. Lima 100", "Lima"),
                new Capacity(5),
                status,
                List.of(new Participation(1000L + id, ORGANIZER_ID,
                        ParticipationStatus.CONFIRMED, Instant.now())));
    }
}
