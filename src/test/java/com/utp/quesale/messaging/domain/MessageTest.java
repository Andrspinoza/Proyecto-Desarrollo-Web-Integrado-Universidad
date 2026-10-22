package com.utp.quesale.messaging.domain;

import com.utp.quesale.messaging.domain.aggregates.Message;
import com.utp.quesale.messaging.domain.events.MessageSentEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    private static final Long SENDER_ID = 10L;
    private static final Long RECIPIENT_ID = 20L;

    private Message savedMessage() {
        return new Message(1L, SENDER_ID, RECIPIENT_ID, "Hola", Instant.now());
    }

    @Test
    @DisplayName("Un mensaje nuevo guarda el contenido sin espacios sobrantes")
    void trimsContent() {
        Message message = new Message(SENDER_ID, RECIPIENT_ID, "  Hola  ");

        assertEquals("Hola", message.getContent());
        assertEquals(SENDER_ID, message.getSenderId());
        assertEquals(RECIPIENT_ID, message.getRecipientId());
        assertNull(message.getId());
        assertNotNull(message.getSentAt());
    }

    @Test
    @DisplayName("El contenido es obligatorio")
    void rejectsBlankContent() {
        assertThrows(IllegalArgumentException.class, () -> new Message(SENDER_ID, RECIPIENT_ID, " "));
        assertThrows(IllegalArgumentException.class, () -> new Message(SENDER_ID, RECIPIENT_ID, null));
    }

    @Test
    @DisplayName("El contenido admite de 1 a 1000 caracteres")
    void validatesContentLength() {
        assertDoesNotThrow(() -> new Message(SENDER_ID, RECIPIENT_ID, "a"));
        assertDoesNotThrow(() -> new Message(SENDER_ID, RECIPIENT_ID, "a".repeat(1000)));
        assertThrows(IllegalArgumentException.class,
                () -> new Message(SENDER_ID, RECIPIENT_ID, "a".repeat(1001)));
    }

    @Test
    @DisplayName("Emisor y destinatario son obligatorios")
    void requiresSenderAndRecipient() {
        assertThrows(IllegalArgumentException.class, () -> new Message(null, RECIPIENT_ID, "Hola"));
        assertThrows(IllegalArgumentException.class, () -> new Message(SENDER_ID, null, "Hola"));
    }

    @Test
    @DisplayName("Nadie se escribe a sí mismo")
    void rejectsMessageToYourself() {
        assertThrows(IllegalArgumentException.class, () -> new Message(SENDER_ID, SENDER_ID, "Hola"));
    }

    @Test
    @DisplayName("Un mensaje nuevo no registra eventos porque todavía no tiene id")
    void newMessageRegistersNoEvents() {
        Message message = new Message(SENDER_ID, RECIPIENT_ID, "Hola");

        assertTrue(message.pullDomainEvents().isEmpty());
    }

    @Test
    @DisplayName("No se marca como enviado un mensaje que todavía no se guardó")
    void cannotMarkAsSentBeforeSaving() {
        Message message = new Message(SENDER_ID, RECIPIENT_ID, "Hola");

        assertThrows(IllegalStateException.class, message::markAsSent);
    }

    @Test
    @DisplayName("Marcar como enviado registra MessageSentEvent con el id real")
    void markAsSentRegistersEvent() {
        Message message = savedMessage();

        message.markAsSent();

        List<Object> events = message.pullDomainEvents();
        assertEquals(1, events.size());
        MessageSentEvent event = assertInstanceOf(MessageSentEvent.class, events.get(0));
        assertEquals(1L, event.messageId());
        assertEquals(SENDER_ID, event.senderId());
        assertEquals(RECIPIENT_ID, event.recipientId());
        assertEquals("Hola", event.content());
        assertNotNull(event.occurredAt());
    }

    @Test
    @DisplayName("Los eventos se entregan una sola vez")
    void eventsAreDeliveredOnce() {
        Message message = savedMessage();
        message.markAsSent();
        message.pullDomainEvents();

        assertTrue(message.pullDomainEvents().isEmpty());
    }
}
