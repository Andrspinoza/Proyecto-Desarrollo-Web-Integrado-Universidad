package com.utp.quesale.meetups.domain.aggregates;

import com.utp.quesale.meetups.domain.entities.Participation;
import com.utp.quesale.meetups.domain.events.InvitationAcceptedEvent;
import com.utp.quesale.meetups.domain.events.InvitationDeclinedEvent;
import com.utp.quesale.meetups.domain.events.MeetupCancelledEvent;
import com.utp.quesale.meetups.domain.events.MeetupCreatedEvent;
import com.utp.quesale.meetups.domain.events.ParticipationConfirmedEvent;
import com.utp.quesale.meetups.domain.events.ParticipationInvitedEvent;
import com.utp.quesale.meetups.domain.events.ParticipationRequestedEvent;
import com.utp.quesale.meetups.domain.valueobjects.MeetupStatus;
import com.utp.quesale.meetups.domain.valueobjects.MeetupType;
import com.utp.quesale.meetups.domain.valueobjects.MeetupVisibility;
import com.utp.quesale.meetups.domain.valueobjects.ParticipationStatus;
import com.utp.quesale.meetups.domain.exceptions.MeetupAccessDeniedException;
import com.utp.quesale.meetups.domain.exceptions.ParticipationNotFoundException;
import com.utp.quesale.meetups.domain.valueobjects.Capacity;
import com.utp.quesale.meetups.domain.valueobjects.Place;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MeetupTest {

    private static final Long ORGANIZER_ID = 1L;
    private static final Long UNIVERSITY_ID = 7L;

    @Test
    @DisplayName("Al crearse, el organizador queda confirmado y ocupa un cupo")
    void confirmsOrganizerWhenCreated() {
        Meetup meetup = createMeetup(3);

        assertEquals(MeetupStatus.SCHEDULED, meetup.getStatus());
        assertEquals(MeetupVisibility.PUBLIC, meetup.getVisibility());
        assertEquals(1, meetup.getParticipations().size());
        assertEquals(ORGANIZER_ID, meetup.getParticipations().getFirst().getStudentId());
        assertEquals(ParticipationStatus.CONFIRMED, meetup.getParticipations().getFirst().getStatus());
        assertEquals(2, meetup.availableSpots());
    }

    @Test
    @DisplayName("Exige título y fecha futura al crear")
    void validatesTitleAndFutureDateWhenCreated() {
        assertThrows(IllegalArgumentException.class, () -> createMeetupAt(" ", futureDate(), 2));
        assertThrows(IllegalArgumentException.class,
                () -> createMeetupAt("Plan", LocalDateTime.now().minusMinutes(1), 2));
        assertThrows(IllegalArgumentException.class, () -> createMeetupAt("T".repeat(101), futureDate(), 2));
    }

    @Test
    @DisplayName("Una solicitud no ocupa cupo hasta que se confirma")
    void pendingParticipationDoesNotConsumeCapacity() {
        Meetup meetup = createMeetup(3);

        meetup.requestParticipation(2L);
        meetup.requestParticipation(3L);

        assertEquals(2, meetup.availableSpots());
        meetup.confirmParticipation(2L, ORGANIZER_ID);
        meetup.confirmParticipation(3L, ORGANIZER_ID);
        assertEquals(0, meetup.availableSpots());
    }

    @Test
    @DisplayName("No confirma más participantes que los cupos disponibles")
    void doesNotExceedCapacityWhenConfirming() {
        Meetup meetup = createMeetup(2);
        meetup.requestParticipation(2L);
        meetup.requestParticipation(3L);

        meetup.confirmParticipation(2L, ORGANIZER_ID);

        assertEquals(0, meetup.availableSpots());
        assertThrows(IllegalStateException.class,
                () -> meetup.confirmParticipation(3L, ORGANIZER_ID));
        assertEquals(2, confirmedParticipationCount(meetup));
        assertEquals(ParticipationStatus.REQUESTED, participationOf(meetup, 3L).getStatus());
    }

    @Test
    @DisplayName("No permite duplicar la participación de un estudiante")
    void rejectsDuplicateParticipation() {
        Meetup meetup = createMeetup(3);
        meetup.requestParticipation(2L);

        assertThrows(IllegalStateException.class, () -> meetup.requestParticipation(2L));
        assertThrows(IllegalStateException.class, () -> meetup.invite(2L, ORGANIZER_ID));
    }

    @Test
    @DisplayName("Un meetup privado solo admite participantes por invitación")
    void privateMeetupOnlyAdmitsInvitations() {
        Meetup meetup = createMeetup(3, MeetupVisibility.PRIVATE);

        assertThrows(IllegalStateException.class, () -> meetup.requestParticipation(2L));
        meetup.invite(2L, ORGANIZER_ID);

        assertEquals(ParticipationStatus.INVITED, participationOf(meetup, 2L).getStatus());
    }

    @Test
    @DisplayName("Solo el organizador puede invitar y las invitaciones no ocupan cupo")
    void onlyOrganizerCanInviteAndInvitationDoesNotConsumeCapacity() {
        Meetup meetup = createMeetup(2);

        assertThrows(MeetupAccessDeniedException.class, () -> meetup.invite(2L, 99L));
        meetup.invite(2L, ORGANIZER_ID);
        meetup.invite(3L, ORGANIZER_ID);

        assertEquals(1, meetup.availableSpots());
        assertEquals(ParticipationStatus.INVITED, participationOf(meetup, 2L).getStatus());
        assertEquals(ParticipationStatus.INVITED, participationOf(meetup, 3L).getStatus());
    }

    @Test
    @DisplayName("Aceptar una invitación ocupa cupo y respeta la capacidad")
    void acceptingInvitationConsumesAvailableCapacity() {
        Meetup meetup = createMeetup(2);
        meetup.invite(2L, ORGANIZER_ID);
        meetup.invite(3L, ORGANIZER_ID);

        meetup.acceptInvitation(2L);

        assertEquals(0, meetup.availableSpots());
        assertEquals(ParticipationStatus.CONFIRMED, participationOf(meetup, 2L).getStatus());
        assertThrows(IllegalStateException.class, () -> meetup.acceptInvitation(3L));
        assertEquals(ParticipationStatus.INVITED, participationOf(meetup, 3L).getStatus());
    }

    @Test
    @DisplayName("Solo el invitado puede aceptar o rechazar una invitación")
    void invitationActionsRequireInvitation() {
        Meetup meetup = createMeetup(3);
        meetup.requestParticipation(2L);

        assertThrows(IllegalStateException.class, () -> meetup.acceptInvitation(2L));
        assertThrows(IllegalStateException.class, () -> meetup.declineInvitation(2L));

        meetup.invite(3L, ORGANIZER_ID);
        meetup.declineInvitation(3L);
        assertEquals(ParticipationStatus.DECLINED, participationOf(meetup, 3L).getStatus());
        assertEquals(2, meetup.availableSpots());
    }

    @Test
    @DisplayName("Un meetup privado solo es visible para el organizador y sus invitados")
    void privateMeetupVisibilityIsRestrictedToOrganizerAndInvitees() {
        Meetup meetup = createMeetup(3, MeetupVisibility.PRIVATE);
        meetup.invite(2L, ORGANIZER_ID);

        assertTrue(meetup.isVisibleTo(ORGANIZER_ID));
        assertTrue(meetup.isVisibleTo(2L));
        assertFalse(meetup.isVisibleTo(3L));
        assertFalse(meetup.isVisibleTo(null));
    }

    @Test
    @DisplayName("Una solicitud pendiente no ve el meetup si pasa a privado")
    void pendingRequesterCannotSeeMeetupAfterItBecomesPrivate() {
        Meetup meetup = createMeetup(3);
        meetup.requestParticipation(2L);

        meetup.modify("Sesión privada", "Repaso para el curso", MeetupType.ACADEMIC,
                MeetupVisibility.PRIVATE, futureDate(), place(), new Capacity(3), ORGANIZER_ID);

        assertFalse(meetup.isVisibleTo(2L));
    }

    @Test
    @DisplayName("Un meetup público es visible para estudiantes con ID válido")
    void publicMeetupIsVisible() {
        assertTrue(createMeetup(3).isVisibleTo(99L));
    }

    @Test
    @DisplayName("No admite solicitudes si está cancelado, finalizado o ya pasó")
    void rejectsParticipationWhenNotJoinable() {
        Meetup cancelled = createMeetup(3);
        cancelled.cancel(ORGANIZER_ID);
        assertFalse(cancelled.isJoinable(LocalDateTime.now()));
        assertThrows(IllegalStateException.class, () -> cancelled.requestParticipation(2L));

        Meetup finished = createMeetup(3);
        finished.finish(ORGANIZER_ID, finished.getScheduledAt().plusMinutes(1));
        assertFalse(finished.isJoinable(LocalDateTime.now()));
        assertThrows(IllegalStateException.class, () -> finished.requestParticipation(2L));

        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        Meetup past = rehydrateMeetup(pastDate, MeetupStatus.SCHEDULED, List.of(organizerParticipation()));
        assertFalse(past.isJoinable(LocalDateTime.now()));
        assertThrows(IllegalStateException.class, () -> past.requestParticipation(2L));
    }

    @Test
    @DisplayName("Las invitaciones solo se envían para meetups programados y futuros")
    void cannotInviteToCancelledOrPastMeetups() {
        Meetup cancelled = createMeetup(3);
        cancelled.cancel(ORGANIZER_ID);
        assertThrows(IllegalStateException.class, () -> cancelled.invite(2L, ORGANIZER_ID));

        Meetup past = rehydrateMeetup(
                LocalDateTime.now().minusDays(1), MeetupStatus.SCHEDULED, List.of(organizerParticipation()));
        assertThrows(IllegalStateException.class, () -> past.invite(2L, ORGANIZER_ID));
    }

    @Test
    @DisplayName("Solo el organizador puede confirmar, modificar, cancelar y finalizar")
    void restrictsOrganizerOperations() {
        Meetup meetup = createMeetup(3);
        meetup.requestParticipation(2L);

        assertThrows(MeetupAccessDeniedException.class,
                () -> meetup.confirmParticipation(2L, 99L));
        assertThrows(MeetupAccessDeniedException.class,
                () -> meetup.modify("Nuevo plan", "Descripción", MeetupType.SOCIAL,
                        MeetupVisibility.PRIVATE, futureDate(), place(), new Capacity(3), 99L));
        assertThrows(MeetupAccessDeniedException.class, () -> meetup.cancel(99L));
        assertThrows(MeetupAccessDeniedException.class,
                () -> meetup.finish(99L, LocalDateTime.now().plusDays(10)));
    }

    @Test
    @DisplayName("El organizador puede modificar fecha, visibilidad y cupo válidos")
    void modifiesMeetupWithValidFutureDateAndCapacity() {
        Meetup meetup = createMeetup(3);
        meetup.requestParticipation(2L);
        meetup.confirmParticipation(2L, ORGANIZER_ID);

        meetup.modify("Nuevo plan", "Nueva descripción", MeetupType.ACADEMIC,
                MeetupVisibility.PRIVATE, futureDate(), place(), new Capacity(2), ORGANIZER_ID);

        assertEquals("Nuevo plan", meetup.getTitle());
        assertEquals(MeetupType.ACADEMIC, meetup.getType());
        assertEquals(MeetupVisibility.PRIVATE, meetup.getVisibility());
        assertEquals(2, meetup.getCapacity().value());
        assertEquals(0, meetup.availableSpots());
    }

    @Test
    @DisplayName("No permite reprogramar al pasado ni reducir el cupo bajo los confirmados")
    void rejectsInvalidMeetupModification() {
        Meetup meetup = createMeetup(3);
        meetup.requestParticipation(2L);
        meetup.requestParticipation(3L);
        meetup.confirmParticipation(2L, ORGANIZER_ID);
        meetup.confirmParticipation(3L, ORGANIZER_ID);

        assertThrows(IllegalArgumentException.class,
                () -> meetup.modify("Plan", "Descripción", MeetupType.SOCIAL,
                        MeetupVisibility.PUBLIC, LocalDateTime.now().minusDays(1), place(), new Capacity(3),
                        ORGANIZER_ID));
        assertThrows(IllegalStateException.class,
                () -> meetup.modify("Plan", "Descripción", MeetupType.SOCIAL,
                        MeetupVisibility.PUBLIC, futureDate(), place(), new Capacity(2), ORGANIZER_ID));
        assertEquals(3, meetup.getCapacity().value());
    }

    @Test
    @DisplayName("El organizador no puede retirarse y los demás sí")
    void preventsOrganizerWithdrawal() {
        Meetup meetup = createMeetup(3);
        meetup.requestParticipation(2L);

        assertThrows(IllegalStateException.class, () -> meetup.withdraw(ORGANIZER_ID));
        meetup.withdraw(2L);
        assertEquals(ParticipationStatus.WITHDRAWN, participationOf(meetup, 2L).getStatus());
    }

    @Test
    @DisplayName("Solo puede finalizarse después de la fecha programada")
    void finishesOnlyAfterScheduledDate() {
        Meetup meetup = createMeetup(2);

        assertThrows(IllegalStateException.class,
                () -> meetup.finish(ORGANIZER_ID, LocalDateTime.now()));
        meetup.finish(ORGANIZER_ID, meetup.getScheduledAt());

        assertEquals(MeetupStatus.FINISHED, meetup.getStatus());
    }

    @Test
    @DisplayName("La rehidratación permite conservar un meetup con fecha pasada")
    void rehydratesMeetupWithoutFutureDateValidation() {
        Meetup meetup = rehydrateMeetup(
                LocalDateTime.now().minusDays(1), MeetupStatus.SCHEDULED, List.of(organizerParticipation()));

        assertEquals(MeetupStatus.SCHEDULED, meetup.getStatus());
        assertFalse(meetup.isJoinable(LocalDateTime.now()));
    }

    @Test
    @DisplayName("Al crear se registra el evento MeetupCreatedEvent y luego se limpia")
    void registersAndPullsMeetupCreatedEvent() {
        Meetup meetup = createMeetup(3);

        List<Object> events = meetup.pullDomainEvents();

        assertEquals(1, events.size());
        MeetupCreatedEvent event = assertInstanceOf(MeetupCreatedEvent.class, events.getFirst());
        assertNull(event.meetupId());
        assertEquals(ORGANIZER_ID, event.organizerId());
        assertTrue(event.occurredAt() != null);
        assertTrue(meetup.pullDomainEvents().isEmpty());
    }

    @Test
    @DisplayName("La rehidratación no genera eventos de creación")
    void rehydrationDoesNotRegisterCreatedEvent() {
        Meetup meetup = rehydrateMeetup(futureDate(), MeetupStatus.SCHEDULED, List.of(organizerParticipation()));

        assertTrue(meetup.pullDomainEvents().isEmpty());
    }

    @Test
    @DisplayName("Las acciones registran sus eventos de dominio correspondientes")
    void registersParticipationAndInvitationEvents() {
        Meetup meetup = createMeetup(5);
        meetup.pullDomainEvents();

        meetup.requestParticipation(2L);
        ParticipationRequestedEvent requested = assertInstanceOf(
                ParticipationRequestedEvent.class, meetup.pullDomainEvents().getFirst());
        assertEventIdentity(requested.meetupId(), requested.organizerId(), requested.studentId(), 2L);

        meetup.confirmParticipation(2L, ORGANIZER_ID);
        ParticipationConfirmedEvent confirmed = assertInstanceOf(
                ParticipationConfirmedEvent.class, meetup.pullDomainEvents().getFirst());
        assertEventIdentity(confirmed.meetupId(), confirmed.organizerId(), confirmed.studentId(), 2L);

        meetup.invite(3L, ORGANIZER_ID);
        ParticipationInvitedEvent invited = assertInstanceOf(
                ParticipationInvitedEvent.class, meetup.pullDomainEvents().getFirst());
        assertEventIdentity(invited.meetupId(), invited.organizerId(), invited.studentId(), 3L);

        meetup.acceptInvitation(3L);
        InvitationAcceptedEvent accepted = assertInstanceOf(
                InvitationAcceptedEvent.class, meetup.pullDomainEvents().getFirst());
        assertEventIdentity(accepted.meetupId(), accepted.organizerId(), accepted.studentId(), 3L);

        meetup.invite(4L, ORGANIZER_ID);
        meetup.pullDomainEvents();
        meetup.declineInvitation(4L);
        InvitationDeclinedEvent declined = assertInstanceOf(
                InvitationDeclinedEvent.class, meetup.pullDomainEvents().getFirst());
        assertEventIdentity(declined.meetupId(), declined.organizerId(), declined.studentId(), 4L);
    }

    @Test
    @DisplayName("Al cancelar, el evento contiene solo participantes activos y su lista es inmutable")
    void cancellationEventContainsActiveParticipants() {
        Meetup meetup = createMeetup(5);
        meetup.pullDomainEvents();
        meetup.requestParticipation(2L);
        meetup.confirmParticipation(2L, ORGANIZER_ID);
        meetup.requestParticipation(3L);
        meetup.invite(4L, ORGANIZER_ID);
        meetup.declineInvitation(4L);
        meetup.requestParticipation(5L);
        meetup.withdraw(5L);
        meetup.pullDomainEvents();

        meetup.cancel(ORGANIZER_ID);

        MeetupCancelledEvent event = assertInstanceOf(MeetupCancelledEvent.class, meetup.pullDomainEvents().getFirst());
        assertEquals(ORGANIZER_ID, event.organizerId());
        assertEquals(List.of(2L, 3L), event.participantIds());
        assertThrows(UnsupportedOperationException.class, () -> event.participantIds().add(99L));
    }

    @Test
    @DisplayName("Las transiciones fallan con ParticipationNotFoundException si no existe participación")
    void missingParticipationUsesNotFoundException() {
        Meetup meetup = createMeetup(3);

        assertThrows(ParticipationNotFoundException.class, () -> meetup.confirmParticipation(2L, ORGANIZER_ID));
        assertThrows(ParticipationNotFoundException.class, () -> meetup.acceptInvitation(2L));
        assertThrows(ParticipationNotFoundException.class, () -> meetup.declineInvitation(2L));
        assertThrows(ParticipationNotFoundException.class, () -> meetup.withdraw(2L));
    }

    private static void assertEventIdentity(Long meetupId, Long organizerId, Long studentId, Long expectedStudentId) {
        assertNull(meetupId);
        assertEquals(ORGANIZER_ID, organizerId);
        assertEquals(expectedStudentId, studentId);
    }

    private Meetup createMeetup(int capacity) {
        return createMeetup(capacity, MeetupVisibility.PUBLIC);
    }

    private Meetup createMeetup(int capacity, MeetupVisibility visibility) {
        return new Meetup(ORGANIZER_ID, UNIVERSITY_ID, "Sesión de estudio", "Repaso para el curso",
                MeetupType.ACADEMIC, visibility, futureDate(), place(), new Capacity(capacity));
    }

    private Meetup createMeetupAt(String title, LocalDateTime scheduledAt, int capacity) {
        return new Meetup(ORGANIZER_ID, UNIVERSITY_ID, title, "Repaso para el curso",
                MeetupType.ACADEMIC, MeetupVisibility.PUBLIC, scheduledAt, place(), new Capacity(capacity));
    }

    private Meetup rehydrateMeetup(
            LocalDateTime scheduledAt,
            MeetupStatus status,
            List<Participation> participations) {
        return new Meetup(10L, ORGANIZER_ID, UNIVERSITY_ID, "Sesión de estudio", "Repaso",
                MeetupType.ACADEMIC, MeetupVisibility.PUBLIC, scheduledAt, place(), new Capacity(3),
                status, participations);
    }

    private static LocalDateTime futureDate() {
        return LocalDateTime.now().plusDays(10);
    }

    private static Place place() {
        return new Place("Campus UTP", "Av. Arequipa", "Lima");
    }

    private static Participation organizerParticipation() {
        return new Participation(ORGANIZER_ID, ParticipationStatus.CONFIRMED, Instant.now());
    }

    private static int confirmedParticipationCount(Meetup meetup) {
        return (int) meetup.getParticipations().stream()
                .filter(participation -> participation.getStatus() == ParticipationStatus.CONFIRMED)
                .count();
    }

    private static Participation participationOf(Meetup meetup, Long studentId) {
        return meetup.getParticipations().stream()
                .filter(participation -> participation.getStudentId().equals(studentId))
                .findFirst()
                .orElseThrow();
    }
}
