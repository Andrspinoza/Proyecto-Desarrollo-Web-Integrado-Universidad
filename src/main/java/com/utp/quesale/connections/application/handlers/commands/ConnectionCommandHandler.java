package com.utp.quesale.connections.application.handlers.commands;

import com.utp.quesale.connections.application.assemblers.ConnectionsDtoAssembler;
import com.utp.quesale.connections.application.commands.AcceptConnectionCommand;
import com.utp.quesale.connections.application.commands.CancelConnectionCommand;
import com.utp.quesale.connections.application.commands.CreateConnectionCommand;
import com.utp.quesale.connections.application.commands.RejectConnectionCommand;
import com.utp.quesale.connections.application.dtos.ConnectionDto;
import com.utp.quesale.connections.domain.aggregates.Connection;
import com.utp.quesale.connections.domain.exceptions.ConnectionNotFoundException;
import com.utp.quesale.connections.domain.exceptions.ConnectionOperationForbiddenException;
import com.utp.quesale.connections.domain.repositories.ConnectionRepository;
import com.utp.quesale.connections.domain.valueobjects.ConnectionStatus;
import com.utp.quesale.moderation.interfaces.acl.ModerationContextFacade;
import com.utp.quesale.profiles.interfaces.acl.ProfilesContextFacade;
import com.utp.quesale.shared.domain.events.DomainEventDispatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class ConnectionCommandHandler {

    private static final Set<ConnectionStatus> ACTIVE_STATUSES =
            Set.of(ConnectionStatus.PENDING, ConnectionStatus.ACCEPTED);

    private final ConnectionRepository connectionRepository;
    private final DomainEventDispatcher events;
    private final ProfilesContextFacade profiles;
    private final ModerationContextFacade moderation;

    public ConnectionCommandHandler(
            ConnectionRepository connectionRepository,
            DomainEventDispatcher events,
            ProfilesContextFacade profiles,
            ModerationContextFacade moderation) {
        this.connectionRepository = connectionRepository;
        this.events = events;
        this.profiles = profiles;
        this.moderation = moderation;
    }

    @Transactional
    public ConnectionDto handle(CreateConnectionCommand command) {
        Long requesterUniversity = profiles.universityOfStudent(command.requesterId())
                .orElseThrow(() -> new ConnectionOperationForbiddenException(
                        "Requester does not have a student profile"));
        Long recipientUniversity = profiles.universityOfStudent(command.recipientId())
                .orElseThrow(() -> new ConnectionOperationForbiddenException(
                        "Recipient does not have a student profile"));
        if (!requesterUniversity.equals(recipientUniversity)) {
            throw new ConnectionOperationForbiddenException(
                    "Connections are only allowed between students of the same university");
        }
        if (moderation.isBlocked(command.requesterId(), command.recipientId())) {
            throw new ConnectionOperationForbiddenException(
                    "A connection cannot be requested between blocked students");
        }
        if (connectionRepository.existsBetweenWithStatuses(
                command.requesterId(), command.recipientId(), ACTIVE_STATUSES)) {
            throw new IllegalStateException("An active connection already exists between these students");
        }

        Connection saved = connectionRepository.save(
                new Connection(command.requesterId(), command.recipientId()));
        saved.markAsCreated();
        events.dispatch(saved);
        return ConnectionsDtoAssembler.toDto(saved);
    }

    @Transactional
    public ConnectionDto handle(AcceptConnectionCommand command) {
        Connection connection = requireConnection(command.connectionId());
        connection.accept(command.recipientId());
        connectionRepository.update(connection);
        events.dispatch(connection);
        return ConnectionsDtoAssembler.toDto(connection);
    }

    @Transactional
    public ConnectionDto handle(RejectConnectionCommand command) {
        Connection connection = requireConnection(command.connectionId());
        connection.reject(command.recipientId());
        connectionRepository.update(connection);
        return ConnectionsDtoAssembler.toDto(connection);
    }

    @Transactional
    public ConnectionDto handle(CancelConnectionCommand command) {
        Connection connection = requireConnection(command.connectionId());
        connection.cancel(command.requesterId());
        connectionRepository.update(connection);
        return ConnectionsDtoAssembler.toDto(connection);
    }

    private Connection requireConnection(Long connectionId) {
        return connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ConnectionNotFoundException(connectionId));
    }
}
