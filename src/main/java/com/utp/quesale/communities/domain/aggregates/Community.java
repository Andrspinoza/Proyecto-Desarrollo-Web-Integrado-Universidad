package com.utp.quesale.communities.domain.aggregates;

import com.utp.quesale.communities.domain.entities.CommunityPost;
import com.utp.quesale.communities.domain.entities.Membership;
import com.utp.quesale.communities.domain.exceptions.AlreadyMemberException;
import com.utp.quesale.communities.domain.exceptions.LastAdminCannotLeaveException;
import com.utp.quesale.communities.domain.exceptions.NotCommunityAdminException;
import com.utp.quesale.communities.domain.exceptions.NotMemberException;
import com.utp.quesale.communities.domain.valueobjects.MemberRole;
import com.utp.quesale.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Community extends AbstractDomainAggregateRoot<Community> {

    private static final int NAME_MAX_LENGTH = 80;
    private static final int DESCRIPTION_MAX_LENGTH = 500;

    private Long id;
    private final Long universityId;
    private String name;
    private String description;
    private final Long creatorId;
    private final Instant createdAt;
    private final List<Membership> memberships;

    // Creación: el creador entra como ADMIN
    public Community(Long universityId, String name, String description, Long creatorId) {
        this(null, universityId, name, description, creatorId, Instant.now(),
                List.of(new Membership(creatorId, MemberRole.ADMIN)));
    }

    // Rehidratación: la usa solo el mapper; nunca registra eventos
    public Community(Long id, Long universityId, String name, String description, Long creatorId,
                     Instant createdAt, List<Membership> memberships) {
        if (universityId == null) throw new IllegalArgumentException("University id is required");
        if (creatorId == null) throw new IllegalArgumentException("Creator id is required");
        if (createdAt == null) throw new IllegalArgumentException("Created at is required");
        if (memberships == null) throw new IllegalArgumentException("Memberships are required");
        this.id = id;
        this.universityId = universityId;
        this.name = validName(name);
        this.description = validDescription(description);
        this.creatorId = creatorId;
        this.createdAt = createdAt;
        this.memberships = new ArrayList<>(memberships);
    }

    public void join(Long studentId) {
        if (isMember(studentId)) throw new AlreadyMemberException(studentId);
        memberships.add(new Membership(studentId, MemberRole.MEMBER));
    }

    public void leave(Long studentId) {
        Membership membership = findMembership(studentId)
                .orElseThrow(() -> new NotMemberException(studentId));
        // Una comunidad nunca se queda sin administrador
        if (membership.isAdmin() && adminCount() == 1) throw new LastAdminCannotLeaveException();
        memberships.remove(membership);
    }

    public void update(String name, String description, Long requesterId) {
        if (!isAdmin(requesterId)) throw new NotCommunityAdminException(requesterId);
        this.name = validName(name);
        this.description = validDescription(description);
    }

    // Solo los miembros publican; la comunidad ya debe estar guardada para tener id
    public CommunityPost publish(Long authorId, String content) {
        if (id == null) throw new IllegalStateException("Community must be saved before publishing");
        if (!isMember(authorId)) throw new NotMemberException(authorId);
        return new CommunityPost(id, authorId, content);
    }

    public boolean isMember(Long studentId) {
        return findMembership(studentId).isPresent();
    }

    public boolean isAdmin(Long studentId) {
        return findMembership(studentId).map(Membership::isAdmin).orElse(false);
    }

    private Optional<Membership> findMembership(Long studentId) {
        return memberships.stream().filter(m -> m.getStudentId().equals(studentId)).findFirst();
    }

    private long adminCount() {
        return memberships.stream().filter(Membership::isAdmin).count();
    }

    private static String validName(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Community name is required");
        String trimmed = value.trim();
        if (trimmed.length() > NAME_MAX_LENGTH)
            throw new IllegalArgumentException("Community name must be at most %d characters".formatted(NAME_MAX_LENGTH));
        return trimmed;
    }

    private static String validDescription(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.length() > DESCRIPTION_MAX_LENGTH)
            throw new IllegalArgumentException(
                    "Community description must be at most %d characters".formatted(DESCRIPTION_MAX_LENGTH));
        return trimmed;
    }

    public Long getId() {
        return id;
    }

    public Long getUniversityId() {
        return universityId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<Membership> getMemberships() {
        return Collections.unmodifiableList(memberships);
    }
}
