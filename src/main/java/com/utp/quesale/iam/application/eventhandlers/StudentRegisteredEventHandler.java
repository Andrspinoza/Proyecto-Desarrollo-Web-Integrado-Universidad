package com.utp.quesale.iam.application.eventhandlers;

import com.utp.quesale.iam.application.commands.IssueEmailVerificationCommand;
import com.utp.quesale.iam.application.handlers.commands.EmailVerificationCommandHandler;
import com.utp.quesale.profiles.domain.events.StudentRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.stereotype.Component;

@Component
public class StudentRegisteredEventHandler {

    private static final Logger log = LoggerFactory.getLogger(StudentRegisteredEventHandler.class);

    private final EmailVerificationCommandHandler handler;

    public StudentRegisteredEventHandler(EmailVerificationCommandHandler handler) {
        this.handler = handler;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(StudentRegisteredEvent event) {
        try {
            handler.handle(new IssueEmailVerificationCommand(event.studentId(), event.email(), event.firstName()));
        } catch (RuntimeException ex) {
            log.warn("No se pudo emitir la verificación de correo del estudiante {}: {}",
                    event.studentId(), ex.getMessage());
        }
    }
}
