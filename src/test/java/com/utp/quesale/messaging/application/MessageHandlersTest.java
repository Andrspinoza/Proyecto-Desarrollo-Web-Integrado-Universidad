package com.utp.quesale.messaging.application;

import com.utp.quesale.connections.interfaces.acl.ConnectionsContextFacade;
import com.utp.quesale.messaging.application.commands.SendMessageCommand;
import com.utp.quesale.messaging.application.dtos.MessageDto;
import com.utp.quesale.messaging.application.handlers.commands.MessageCommandHandler;
import com.utp.quesale.messaging.application.handlers.queries.GetConversationQueryHandler;
import com.utp.quesale.messaging.application.queries.GetConversationQuery;
import com.utp.quesale.messaging.domain.aggregates.Message;
import com.utp.quesale.messaging.domain.events.MessageSentEvent;
import com.utp.quesale.messaging.domain.exceptions.ConversationBlockedException;
import com.utp.quesale.messaging.domain.exceptions.StudentsNotConnectedException;
import com.utp.quesale.messaging.domain.repositories.MessageRepository;
import com.utp.quesale.moderation.interfaces.acl.ModerationContextFacade;
import com.utp.quesale.shared.domain.events.DomainEventDispatcher;
import com.utp.quesale.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MessageHandlersTest {

    private static final Long ANA = 1L;
    private static final Long MARCO = 2L;
    private static final Long ALICIA = 3L;

    private final InMemoryMessageRepository repository = new InMemoryMessageRepository();
    private final FakeConnectionsContextFacade connections = new FakeConnectionsContextFacade();
    private final FakeModerationContextFacade moderation = new FakeModerationContextFacade();
    private final List<AbstractDomainAggregateRoot<?>> dispatched = new ArrayList<>();
    private final DomainEventDispatcher events = dispatched::add;
    private final MessageCommandHandler commandHandler =
            new MessageCommandHandler(repository, connections, moderation, events);
    private final GetConversationQueryHandler queryHandler = new GetConversationQueryHandler(repository);

    @Test
    @DisplayName("Envía un mensaje entre conexiones y despacha el evento con el id guardado")
    void sendsMessageBetweenConnections() {
        connections.connect(ANA, MARCO);

        MessageDto dto = commandHandler.handle(new SendMessageCommand(ANA, MARCO, "  Hola Marco  "));

        assertEquals(1L, dto.id());
        assertEquals(ANA, dto.senderId());
        assertEquals(MARCO, dto.recipientId());
        assertEquals("Hola Marco", dto.content());
        assertNotNull(dto.sentAt());
        assertEquals(1, dispatched.size());
        MessageSentEvent event = (MessageSentEvent) dispatched.get(0).pullDomainEvents().get(0);
        assertEquals(1L, event.messageId());
        assertEquals(MARCO, event.recipientId());
    }

    @Test
    @DisplayName("No envía un mensaje a quien no es tu conexión")
    void rejectsMessageToNonConnection() {
        assertThrows(StudentsNotConnectedException.class,
                () -> commandHandler.handle(new SendMessageCommand(ANA, MARCO, "Hola")));
        assertTrue(repository.messages.isEmpty());
        assertTrue(dispatched.isEmpty());
    }

    @Test
    @DisplayName("No envía un mensaje si hay un bloqueo entre los dos")
    void rejectsMessageWhenBlocked() {
        connections.connect(ANA, MARCO);
        moderation.block(MARCO, ANA);

        assertThrows(ConversationBlockedException.class,
                () -> commandHandler.handle(new SendMessageCommand(ANA, MARCO, "Hola")));
        assertTrue(repository.messages.isEmpty());
        assertTrue(dispatched.isEmpty());
    }

    @Test
    @DisplayName("No envía un mensaje a uno mismo ni un mensaje vacío")
    void rejectsInvalidMessage() {
        connections.connect(ANA, MARCO);

        assertThrows(IllegalArgumentException.class,
                () -> commandHandler.handle(new SendMessageCommand(ANA, ANA, "Hola yo")));
        assertThrows(IllegalArgumentException.class,
                () -> commandHandler.handle(new SendMessageCommand(ANA, MARCO, "   ")));
        assertTrue(repository.messages.isEmpty());
    }

    @Test
    @DisplayName("La conversación incluye los mensajes en ambos sentidos, del más antiguo al más reciente")
    void listsConversationInOrder() {
        connections.connect(ANA, MARCO);
        commandHandler.handle(new SendMessageCommand(ANA, MARCO, "Primero"));
        commandHandler.handle(new SendMessageCommand(MARCO, ANA, "Segundo"));

        List<MessageDto> conversation = queryHandler.handle(new GetConversationQuery(ANA, MARCO));

        assertEquals(List.of("Primero", "Segundo"), conversation.stream().map(MessageDto::content).toList());
        assertEquals(2, queryHandler.handle(new GetConversationQuery(MARCO, ANA)).size());
    }

    @Test
    @DisplayName("La conversación no incluye mensajes con otros estudiantes y puede estar vacía")
    void conversationIgnoresOtherChats() {
        connections.connect(ANA, MARCO);
        connections.connect(ANA, ALICIA);
        commandHandler.handle(new SendMessageCommand(ANA, MARCO, "Para Marco"));
        commandHandler.handle(new SendMessageCommand(ANA, ALICIA, "Para Alicia"));

        assertEquals(1, queryHandler.handle(new GetConversationQuery(ANA, MARCO)).size());
        assertTrue(queryHandler.handle(new GetConversationQuery(MARCO, ALICIA)).isEmpty());
    }

    private static class InMemoryMessageRepository implements MessageRepository {
        private final List<Message> messages = new ArrayList<>();

        @Override
        public Message save(Message message) {
            Message stored = new Message((long) messages.size() + 1, message.getSenderId(),
                    message.getRecipientId(), message.getContent(), message.getSentAt());
            messages.add(stored);
            return copy(stored);
        }

        @Override
        public Optional<Message> findById(Long id) {
            return messages.stream().filter(m -> m.getId().equals(id)).findFirst().map(this::copy);
        }

        @Override
        public List<Message> findConversation(Long studentId, Long otherStudentId) {
            return messages.stream()
                    .filter(m -> (m.getSenderId().equals(studentId) && m.getRecipientId().equals(otherStudentId))
                            || (m.getSenderId().equals(otherStudentId) && m.getRecipientId().equals(studentId)))
                    .map(this::copy)
                    .toList();
        }

        private Message copy(Message m) {
            return new Message(m.getId(), m.getSenderId(), m.getRecipientId(), m.getContent(), m.getSentAt());
        }
    }

    private static class FakeConnectionsContextFacade implements ConnectionsContextFacade {
        private final Set<String> connected = new HashSet<>();

        void connect(Long a, Long b) {
            connected.add(key(a, b));
        }

        @Override
        public boolean areConnected(Long studentId, Long otherStudentId) {
            return connected.contains(key(studentId, otherStudentId));
        }
    }

    private static class FakeModerationContextFacade implements ModerationContextFacade {
        private final Set<String> blocks = new HashSet<>();

        void block(Long blocker, Long blocked) {
            blocks.add(key(blocker, blocked));
        }

        @Override
        public boolean isBlocked(Long studentId, Long otherStudentId) {
            return blocks.contains(key(studentId, otherStudentId));
        }
    }

    private static String key(Long a, Long b) {
        return Math.min(a, b) + "-" + Math.max(a, b);
    }
}
