package com.utp.quesale.messaging.application.handlers.commands;

import com.utp.quesale.connections.interfaces.acl.ConnectionsContextFacade;
import com.utp.quesale.messaging.application.assemblers.MessagingDtoAssembler;
import com.utp.quesale.messaging.application.commands.SendMessageCommand;
import com.utp.quesale.messaging.application.dtos.MessageDto;
import com.utp.quesale.messaging.domain.aggregates.Message;
import com.utp.quesale.messaging.domain.exceptions.ConversationBlockedException;
import com.utp.quesale.messaging.domain.exceptions.StudentsNotConnectedException;
import com.utp.quesale.messaging.domain.repositories.MessageRepository;
import com.utp.quesale.moderation.interfaces.acl.ModerationContextFacade;
import com.utp.quesale.shared.domain.events.DomainEventDispatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageCommandHandler {

    private final MessageRepository messageRepository;
    private final ConnectionsContextFacade connections;
    private final ModerationContextFacade moderation;
    private final DomainEventDispatcher events;

    public MessageCommandHandler(MessageRepository messageRepository, ConnectionsContextFacade connections,
                                 ModerationContextFacade moderation, DomainEventDispatcher events) {
        this.messageRepository = messageRepository;
        this.connections = connections;
        this.moderation = moderation;
        this.events = events;
    }

    @Transactional
    public MessageDto handle(SendMessageCommand command) {
        Message message = new Message(command.senderId(), command.recipientId(), command.content());
        if (!connections.areConnected(command.senderId(), command.recipientId()))
            throw new StudentsNotConnectedException(command.senderId(), command.recipientId());
        if (moderation.isBlocked(command.senderId(), command.recipientId()))
            throw new ConversationBlockedException(command.senderId(), command.recipientId());
        Message saved = messageRepository.save(message);
        saved.markAsSent();
        events.dispatch(saved);
        return MessagingDtoAssembler.toDto(saved);
    }
}
