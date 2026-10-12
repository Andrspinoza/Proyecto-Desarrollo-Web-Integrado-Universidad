package com.utp.quesale.connections.domain.aggregates;

import com.utp.quesale.connections.domain.events.ConnectionAcceptedEvent;
import com.utp.quesale.connections.domain.events.ConnectionRequestedEvent;
import com.utp.quesale.connections.domain.exceptions.ConnectionOperationForbiddenException;
import com.utp.quesale.connections.domain.valueobjects.ConnectionStatus;
import com.utp.quesale.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Instant;

public class Connection extends AbstractDomainAggregateRoot<Connection> {

    private Long id;
    private final Long requesterId;
    private final Long recipientId;
    private ConnectionStatus status;
    private final Instant createdAt;
    private Instant respondedAt;
    private boolean creationEventRegistered;

    public Connection(Long requesterId, Long recipientId) {
        this(null, requesterId, recipientId, ConnectionStatus.PENDING, Instant.now(), null);
    }

    public Connection(Long id, Long requesterId, Long recipientId, ConnectionStatus status,
                      Instant createdAt, Instant respondedAt) {
        validateOptionalPositiveId(id, "Connection ID");
        validateRequiredPositiveId(requesterId, "Requester ID");
        validateRequiredPositiveId(recipientId, "Recipient ID");
        if (requesterId.equals(recipientId)) {
            throw new IllegalArgumentException("A student cannot connect with themselves");
        }
        if (status == null) throw new IllegalArgumentException("Connection status is required");
        if (createdAt == null) throw new IllegalArgumentException("Created at is required");
        if (status == ConnectionStatus.PENDING && respondedAt != null) {
            throw new IllegalArgumentException("Pending connections cannot have a response time");
        }
        if (status != ConnectionStatus.PENDING && respondedAt == null) {
            throw new IllegalArgumentException("Resolved connections require a response time");
        }
        if (respondedAt != null && respondedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("Response time cannot be before connection creation");
        }

        this.id = id;
        this.requesterId = requesterId;
        this.recipientId = recipientId;
        this.status = status;
        this.createdAt = createdAt;
        this.respondedAt = respondedAt;
    }

    public void markAsCreated() {
        if (id == null) throw new IllegalStateException("Connection must be saved before it is marked as created");
        if (status != ConnectionStatus.PENDING) {
            throw new IllegalStateException("Only pending connections can be marked as created");
        }
        if (creationEventRegistered) throw new IllegalStateException("Connection creation event is already registered");

        registerDomainEvent(new ConnectionRequestedEvent(id, requesterId, recipientId, Instant.now()));
        creationEventRegistered = true;
    }

    public void accept(Long byStudentId) {
        ensurePending();
        ensureRecipient(byStudentId);
        if (id == null) {
            throw new IllegalStateException("Connection must be saved before it can be accepted");
        }

        respondedAt = Instant.now();
        status = ConnectionStatus.ACCEPTED;
        registerDomainEvent(new ConnectionAcceptedEvent(id, requesterId, recipientId, respondedAt));
    }

    public void reject(Long byStudentId) {
        ensurePending();
        ensureRecipient(byStudentId);

        status = ConnectionStatus.REJECTED;
        respondedAt = Instant.now();
    }

    public void cancel(Long byStudentId) {
        ensurePending();
        validateRequiredPositiveId(byStudentId, "Student ID");
        if (!requesterId.equals(byStudentId)) {
            throw new ConnectionOperationForbiddenException("Only the requester can cancel the connection");
        }

        status = ConnectionStatus.CANCELLED;
        respondedAt = Instant.now();
    }

    private void ensurePending() {
        if (status != ConnectionStatus.PENDING) {
            throw new IllegalStateException("Only pending connections can be changed");
        }
    }

    private void ensureRecipient(Long byStudentId) {
        validateRequiredPositiveId(byStudentId, "Student ID");
        if (!recipientId.equals(byStudentId)) {
            throw new ConnectionOperationForbiddenException("Only the recipient can respond to the connection");
        }
    }

    private static void validateOptionalPositiveId(Long value, String fieldName) {
        if (value != null && value <= 0) throw new IllegalArgumentException(fieldName + " must be positive");
    }

    private static void validateRequiredPositiveId(Long value, String fieldName) {
        if (value == null) throw new IllegalArgumentException(fieldName + " is required");
        validateOptionalPositiveId(value, fieldName);
    }

    public Long getId() {
        return id;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public ConnectionStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getRespondedAt() {
        return respondedAt;
    }
}
