package com.utp.quesale.calls.application.handlers.commands;

import com.utp.quesale.calls.application.commands.RelayCallSignalCommand;
import com.utp.quesale.calls.domain.exceptions.CallBlockedException;
import com.utp.quesale.calls.domain.exceptions.CallEmailNotVerifiedException;
import com.utp.quesale.calls.domain.exceptions.StudentsNotConnectedForCallException;
import com.utp.quesale.calls.domain.services.CallSignalPublisher;
import com.utp.quesale.calls.domain.valueobjects.CallSignal;
import com.utp.quesale.calls.domain.valueobjects.CallSignalType;
import com.utp.quesale.connections.interfaces.acl.ConnectionsContextFacade;
import com.utp.quesale.iam.interfaces.acl.IamContextFacade;
import com.utp.quesale.moderation.interfaces.acl.ModerationContextFacade;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
public class CallSignalCommandHandler {

    private final ConnectionsContextFacade connections;
    private final ModerationContextFacade moderation;
    private final IamContextFacade iam;
    private final CallSignalPublisher publisher;
    private final Clock clock;

    public CallSignalCommandHandler(ConnectionsContextFacade connections, ModerationContextFacade moderation,
                                    IamContextFacade iam, CallSignalPublisher publisher, Clock clock) {
        this.connections = connections;
        this.moderation = moderation;
        this.iam = iam;
        this.publisher = publisher;
        this.clock = clock;
    }

    public void handle(RelayCallSignalCommand command) {
        CallSignal signal = new CallSignal(command.fromStudentId(), command.toStudentId(),
                CallSignalType.fromWire(command.type()), command.payload(), clock.instant());
        if (iam.isEmailVerificationRequired() && !iam.isEmailVerified(signal.fromStudentId()))
            throw new CallEmailNotVerifiedException(signal.fromStudentId());
        if (!signal.type().isTerminal()) {
            if (!connections.areConnected(signal.fromStudentId(), signal.toStudentId()))
                throw new StudentsNotConnectedForCallException(signal.fromStudentId(), signal.toStudentId());
            if (moderation.isBlocked(signal.fromStudentId(), signal.toStudentId()))
                throw new CallBlockedException(signal.fromStudentId(), signal.toStudentId());
        }
        publisher.publish(signal);
    }
}
