package com.utp.quesale.messaging.domain.aggregates;

import com.utp.quesale.messaging.domain.events.MessageSentEvent;
import com.utp.quesale.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Instant;

public class Message extends AbstractDomainAggregateRoot<Message> {

    private static final int CONTENT_MAX_LENGTH = 1000;

    private Long id;
    private final Long senderId;
    private final Long recipientId;
    private final String content;
    private final Instant sentAt;

    public Message(Long senderId, Long recipientId, String content) {
        this(null, senderId, recipientId, content, Instant.now());
    }

    public Message(Long id, Long senderId, Long recipientId, String content, Instant sentAt) {
        if (senderId == null) throw new IllegalArgumentException("Sender id is required");
        if (recipientId == null) throw new IllegalArgumentException("Recipient id is required");
        if (senderId.equals(recipientId)) throw new IllegalArgumentException("Cannot send a message to yourself");
        if (sentAt == null) throw new IllegalArgumentException("Sent at is required");
        this.id = id;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.content = validContent(content);
        this.sentAt = sentAt;
    }

    public void markAsSent() {
        if (id == null) throw new IllegalStateException("Message must be saved before marking it as sent");
        registerDomainEvent(new MessageSentEvent(id, senderId, recipientId, content, Instant.now()));
    }

    private static String validContent(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Message content is required");
        String trimmed = value.trim();
        if (trimmed.length() > CONTENT_MAX_LENGTH)
            throw new IllegalArgumentException(
                    "Message content must be at most %d characters".formatted(CONTENT_MAX_LENGTH));
        return trimmed;
    }

    public Long getId() {
        return id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public String getContent() {
        return content;
    }

    public Instant getSentAt() {
        return sentAt;
    }
}
