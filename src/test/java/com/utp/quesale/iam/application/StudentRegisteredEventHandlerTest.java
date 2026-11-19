package com.utp.quesale.iam.application;

import com.utp.quesale.iam.application.eventhandlers.StudentRegisteredEventHandler;
import com.utp.quesale.iam.application.handlers.commands.EmailVerificationCommandHandler;
import com.utp.quesale.iam.domain.services.VerificationEmailSender;
import com.utp.quesale.iam.domain.exceptions.VerificationEmailDeliveryException;
import com.utp.quesale.profiles.domain.events.StudentRegisteredEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class StudentRegisteredEventHandlerTest {

    private static final Instant NOW = Instant.parse("2026-11-18T12:00:00Z");

    private EmailVerificationCommandHandler commandHandler(InMemoryEmailVerificationRepository repository,
                                                           VerificationEmailSender sender) {
        return new EmailVerificationCommandHandler(repository, sender, Clock.fixed(NOW, ZoneOffset.UTC),
                "https://quesale.example", 24, 60);
    }

    @Test
    @DisplayName("Al registrarse un estudiante se emite su verificación y se envía el correo")
    void issuesVerificationWhenStudentRegisters() {
        InMemoryEmailVerificationRepository repository = new InMemoryEmailVerificationRepository();
        RecordingVerificationEmailSender sender = new RecordingVerificationEmailSender();
        StudentRegisteredEventHandler handler = new StudentRegisteredEventHandler(commandHandler(repository, sender));

        handler.on(new StudentRegisteredEvent(4L, "camila@utp.edu.pe", "Camila", NOW));

        assertEquals(1, sender.sent().size());
        assertEquals("camila@utp.edu.pe", sender.last().toEmail());
        assertEquals("Camila", sender.last().recipientName());
        assertTrue(repository.findLatestByStudentId(4L).isPresent());
    }

    @Test
    @DisplayName("Si el correo no se puede enviar, el registro no se rompe")
    void deliveryFailureDoesNotPropagate() {
        InMemoryEmailVerificationRepository repository = new InMemoryEmailVerificationRepository();
        VerificationEmailSender failing = (to, name, link) -> {
            throw new VerificationEmailDeliveryException();
        };
        StudentRegisteredEventHandler handler = new StudentRegisteredEventHandler(commandHandler(repository, failing));

        assertDoesNotThrow(() -> handler.on(new StudentRegisteredEvent(4L, "camila@utp.edu.pe", "Camila", NOW)));
    }
}
