package com.utp.quesale.meetups.domain.aggregates;

import com.utp.quesale.meetups.domain.entities.Participation;
import com.utp.quesale.meetups.domain.events.InvitationAcceptedEvent;
import com.utp.quesale.meetups.domain.events.InvitationDeclinedEvent;
import com.utp.quesale.meetups.domain.events.MeetupCancelledEvent;
import com.utp.quesale.meetups.domain.events.MeetupCreatedEvent;
import com.utp.quesale.meetups.domain.events.ParticipationConfirmedEvent;
import com.utp.quesale.meetups.domain.events.ParticipationInvitedEvent;
import com.utp.quesale.meetups.domain.events.ParticipationRequestedEvent;
import com.utp.quesale.meetups.domain.enums.MeetupStatus;
import com.utp.quesale.meetups.domain.enums.MeetupType;
import com.utp.quesale.meetups.domain.enums.MeetupVisibility;
import com.utp.quesale.meetups.domain.enums.ParticipationStatus;
import com.utp.quesale.meetups.domain.exceptions.MeetupAccessDeniedException;
import com.utp.quesale.meetups.domain.exceptions.ParticipationNotFoundException;
import com.utp.quesale.meetups.domain.valueobjects.Capacity;
import com.utp.quesale.meetups.domain.valueobjects.Place;
import com.utp.quesale.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Meetup extends AbstractDomainAggregateRoot<Meetup> {

    private static final int TITLE_MAX_LENGTH = 100;
    private static final int DESCRIPTION_MAX_LENGTH = 500;

    private final Long id;
    private final Long organizerId;
    private final Long universityId;
    private String title;
    private String description;
    private MeetupType type;
    private MeetupVisibility visibility;
    private LocalDateTime scheduledAt;
    private Place place;
    private Capacity capacity;
    private MeetupStatus status;
    private final List<Participation> participations;

    public Meetup(
            Long organizerId,
            Long universityId,
            String title,
            String description,
            MeetupType type,
            MeetupVisibility visibility,
            LocalDateTime scheduledAt,
            Place place,
            Capacity capacity) {
        this(null, organizerId, universityId, title, description, type, visibility, scheduledAt,
                place, capacity, MeetupStatus.SCHEDULED,
                List.of(new Participation(organizerId, ParticipationStatus.CONFIRMED, Instant.now())));

        if (!scheduledAt.isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("Meetup date must be in the future");
        registerDomainEvent(new MeetupCreatedEvent(id, organizerId, Instant.now()));
    }

    // Rehidrata el agregado desde persistencia sin exigir que la fecha siga en el futuro.
    public Meetup(
            Long id,
            Long organizerId,
            Long universityId,
            String title,
            String description,
            MeetupType type,
            MeetupVisibility visibility,
            LocalDateTime scheduledAt,
            Place place,
            Capacity capacity,
            MeetupStatus status,
            List<Participation> participations) {
        if (id != null && id <= 0)
            throw new IllegalArgumentException("Meetup ID must be positive");
        if (organizerId == null || organizerId <= 0)
            throw new IllegalArgumentException("Organizer ID must be positive");
        if (universityId == null || universityId <= 0)
            throw new IllegalArgumentException("University ID must be positive");
        if (type == null)
            throw new IllegalArgumentException("Meetup type is required");
        if (visibility == null)
            throw new IllegalArgumentException("Meetup visibility is required");
        if (scheduledAt == null)
            throw new IllegalArgumentException("Meetup date is required");
        if (place == null)
            throw new IllegalArgumentException("Meetup place is required");
        if (capacity == null)
            throw new IllegalArgumentException("Meetup capacity is required");
        if (status == null)
            throw new IllegalArgumentException("Meetup status is required");

        this.id = id;
        this.organizerId = organizerId;
        this.universityId = universityId;
        this.title = validTitle(title);
        this.description = validDescription(description);
        this.type = type;
        this.visibility = visibility;
        this.scheduledAt = scheduledAt;
        this.place = place;
        this.capacity = capacity;
        this.status = status;
        this.participations = new ArrayList<>();

        if (participations == null)
            throw new IllegalArgumentException("Meetup participations are required");

        Set<Long> studentIds = new HashSet<>();
        for (Participation participation : participations) {
            if (participation == null)
                throw new IllegalArgumentException("Meetup participation cannot be null");
            if (!studentIds.add(participation.getStudentId()))
                throw new IllegalArgumentException("A student can participate only once");
            this.participations.add(participation);
        }

        boolean organizerConfirmed = this.participations.stream()
                .anyMatch(participation -> participation.getStudentId().equals(organizerId)
                        && participation.getStatus() == ParticipationStatus.CONFIRMED);
        if (!organizerConfirmed)
            throw new IllegalArgumentException("Organizer must have confirmed participation");

        if (confirmedParticipationCount() > capacity.value())
            throw new IllegalArgumentException("Confirmed participants cannot exceed capacity");
    }

    public void requestParticipation(Long studentId) {
        validateStudentId(studentId);
        if (visibility == MeetupVisibility.PRIVATE)
            throw new IllegalStateException("Participation requests are not allowed for private meetups");
        ensureJoinable(LocalDateTime.now());
        ensureNotParticipating(studentId);
        participations.add(new Participation(studentId));
        registerDomainEvent(new ParticipationRequestedEvent(id, organizerId, studentId, Instant.now()));
    }

    public void confirmParticipation(Long studentId, Long requesterId) {
        ensureOrganizer(requesterId);
        ensureJoinable(LocalDateTime.now());

        Participation participation = findParticipation(studentId);
        if (participation == null)
            throw new ParticipationNotFoundException(id, studentId);
        participation.confirm();
        registerDomainEvent(new ParticipationConfirmedEvent(id, organizerId, studentId, Instant.now()));
    }

    public void invite(Long studentId, Long requesterId) {
        ensureOrganizer(requesterId);
        validateStudentId(studentId);
        ensureUpcoming();
        ensureNotParticipating(studentId);
        participations.add(new Participation(studentId, ParticipationStatus.INVITED, Instant.now()));
        registerDomainEvent(new ParticipationInvitedEvent(id, organizerId, studentId, Instant.now()));
    }

    public void acceptInvitation(Long studentId) {
        validateStudentId(studentId);
        ensureJoinable(LocalDateTime.now());

        Participation participation = findParticipation(studentId);
        if (participation == null)
            throw new ParticipationNotFoundException(id, studentId);
        participation.acceptInvitation();
        registerDomainEvent(new InvitationAcceptedEvent(id, organizerId, studentId, Instant.now()));
    }

    public void declineInvitation(Long studentId) {
        validateStudentId(studentId);

        Participation participation = findParticipation(studentId);
        if (participation == null)
            throw new ParticipationNotFoundException(id, studentId);
        participation.declineInvitation();
        registerDomainEvent(new InvitationDeclinedEvent(id, organizerId, studentId, Instant.now()));
    }

    public void withdraw(Long studentId) {
        validateStudentId(studentId);
        if (organizerId.equals(studentId))
            throw new IllegalStateException("The organizer cannot withdraw from their own meetup");

        Participation participation = findParticipation(studentId);
        if (participation == null)
            throw new ParticipationNotFoundException(id, studentId);
        participation.withdraw();
    }

    public void modify(
            String title,
            String description,
            MeetupType type,
            MeetupVisibility visibility,
            LocalDateTime scheduledAt,
            Place place,
            Capacity capacity,
            Long requesterId) {
        ensureOrganizer(requesterId);
        ensureScheduled();
        if (scheduledAt == null || !scheduledAt.isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("Meetup date must be in the future");
        if (type == null)
            throw new IllegalArgumentException("Meetup type is required");
        if (visibility == null)
            throw new IllegalArgumentException("Meetup visibility is required");
        if (place == null)
            throw new IllegalArgumentException("Meetup place is required");
        if (capacity == null)
            throw new IllegalArgumentException("Meetup capacity is required");
        if (capacity.value() < confirmedParticipationCount())
            throw new IllegalStateException("Capacity cannot be lower than confirmed participants");

        String validTitle = validTitle(title);
        String validDescription = validDescription(description);
        this.title = validTitle;
        this.description = validDescription;
        this.type = type;
        this.visibility = visibility;
        this.scheduledAt = scheduledAt;
        this.place = place;
        this.capacity = capacity;
    }

    public void cancel(Long requesterId) {
        ensureOrganizer(requesterId);
        ensureScheduled();
        List<Long> participantIds = participations.stream()
                .filter(participation -> !participation.getStudentId().equals(organizerId))
                .filter(participation -> participation.getStatus() == ParticipationStatus.REQUESTED
                        || participation.getStatus() == ParticipationStatus.INVITED
                        || participation.getStatus() == ParticipationStatus.CONFIRMED)
                .map(Participation::getStudentId)
                .toList();
        status = MeetupStatus.CANCELLED;
        registerDomainEvent(new MeetupCancelledEvent(id, organizerId, List.copyOf(participantIds), Instant.now()));
    }

    public void finish(Long requesterId, LocalDateTime now) {
        ensureOrganizer(requesterId);
        ensureScheduled();
        if (now == null)
            throw new IllegalArgumentException("Current date is required");
        if (scheduledAt.isAfter(now))
            throw new IllegalStateException("A meetup cannot finish before its scheduled date");
        status = MeetupStatus.FINISHED;
    }

    public int availableSpots() {
        return capacity.value() - confirmedParticipationCount();
    }

    public boolean isJoinable(LocalDateTime now) {
        if (now == null)
            throw new IllegalArgumentException("Current date is required");
        return status == MeetupStatus.SCHEDULED
                && scheduledAt.isAfter(now)
                && availableSpots() > 0;
    }

    public boolean isVisibleTo(Long studentId) {
        if (studentId == null || studentId <= 0)
            return false;
        if (visibility == MeetupVisibility.PUBLIC || organizerId.equals(studentId))
            return true;
        Participation participation = findParticipation(studentId);
        return participation != null
                && participation.getStatus() != ParticipationStatus.REQUESTED
                && participation.getStatus() != ParticipationStatus.WITHDRAWN;
    }

    private void ensureJoinable(LocalDateTime now) {
        if (!isJoinable(now))
            throw new IllegalStateException("Meetup is not open for participation");
    }

    private void ensureUpcoming() {
        if (status != MeetupStatus.SCHEDULED || !scheduledAt.isAfter(LocalDateTime.now()))
            throw new IllegalStateException("Invitations can only be sent for upcoming meetups");
    }

    private void ensureOrganizer(Long requesterId) {
        if (requesterId == null || !organizerId.equals(requesterId))
            throw new MeetupAccessDeniedException("Only the meetup organizer can perform this operation");
    }

    private void ensureScheduled() {
        if (status != MeetupStatus.SCHEDULED)
            throw new IllegalStateException("Only scheduled meetups can be modified");
    }

    private void ensureNotParticipating(Long studentId) {
        if (findParticipation(studentId) != null)
            throw new IllegalStateException("Student already has a participation in this meetup");
    }

    private static void validateStudentId(Long studentId) {
        if (studentId == null || studentId <= 0)
            throw new IllegalArgumentException("Student ID must be positive");
    }

    private Participation findParticipation(Long studentId) {
        if (studentId == null)
            return null;
        return participations.stream()
                .filter(participation -> participation.getStudentId().equals(studentId))
                .findFirst()
                .orElse(null);
    }

    private int confirmedParticipationCount() {
        return (int) participations.stream()
                .filter(participation -> participation.getStatus() == ParticipationStatus.CONFIRMED)
                .count();
    }

    private static String validTitle(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Meetup title is required");
        String trimmed = value.trim();
        if (trimmed.length() > TITLE_MAX_LENGTH)
            throw new IllegalArgumentException(
                    "Meetup title must be at most %d characters".formatted(TITLE_MAX_LENGTH));
        return trimmed;
    }

    private static String validDescription(String value) {
        if (value == null)
            return null;
        String trimmed = value.trim();
        if (trimmed.length() > DESCRIPTION_MAX_LENGTH)
            throw new IllegalArgumentException(
                    "Meetup description must be at most %d characters".formatted(DESCRIPTION_MAX_LENGTH));
        return trimmed;
    }

    public Long getId() {
        return id;
    }

    public Long getOrganizerId() {
        return organizerId;
    }

    public Long getUniversityId() {
        return universityId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public MeetupType getType() {
        return type;
    }

    public MeetupVisibility getVisibility() {
        return visibility;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public Place getPlace() {
        return place;
    }

    public Capacity getCapacity() {
        return capacity;
    }

    public MeetupStatus getStatus() {
        return status;
    }

    public List<Participation> getParticipations() {
        return Collections.unmodifiableList(new ArrayList<>(participations));
    }
}
