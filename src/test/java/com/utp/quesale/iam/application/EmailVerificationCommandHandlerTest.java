package com.utp.quesale.iam.application;

import com.utp.quesale.iam.application.commands.ConfirmEmailVerificationCommand;
import com.utp.quesale.iam.application.commands.IssueEmailVerificationCommand;
import com.utp.quesale.iam.application.commands.ResendEmailVerificationCommand;
import com.utp.quesale.iam.application.dtos.EmailVerificationStatusDto;
import com.utp.quesale.iam.application.handlers.commands.EmailVerificationCommandHandler;
import com.utp.quesale.iam.application.handlers.queries.GetEmailVerificationStatusQueryHandler;
import com.utp.quesale.iam.application.queries.GetEmailVerificationStatusQuery;
import com.utp.quesale.iam.domain.exceptions.EmailAlreadyVerifiedException;
import com.utp.quesale.iam.domain.exceptions.EmailVerificationNotFoundException;
import com.utp.quesale.iam.domain.exceptions.InvalidVerificationTokenException;
import com.utp.quesale.iam.domain.exceptions.ResendTooSoonException;
import com.utp.quesale.iam.domain.exceptions.VerificationTokenExpiredException;
import com.utp.quesale.iam.domain.valueobjects.VerificationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class EmailVerificationCommandHandlerTest {

    private static final Instant NOW = Instant.parse("2026-11-18T12:00:00Z");
    private static final String BASE_URL = "https://quesale.example";

    private InMemoryEmailVerificationRepository repository;
    private RecordingVerificationEmailSender sender;
    private EmailVerificationCommandHandler handler;

    @BeforeEach
    void setUp() {
        repository = new InMemoryEmailVerificationRepository();
        sender = new RecordingVerificationEmailSender();
        handler = handlerAt(NOW);
    }

    private EmailVerificationCommandHandler handlerAt(Instant instant) {
        return new EmailVerificationCommandHandler(repository, sender, Clock.fixed(instant, ZoneOffset.UTC),
                BASE_URL + "/", 24, 60);
    }

    private void issueFor(Long studentId) {
        handler.handle(new IssueEmailVerificationCommand(studentId, "Camila@UTP.edu.pe", "Camila"));
    }

    @Test
    @DisplayName("Emitir guarda el hash del token y envía el enlace con el token en claro")
    void issueStoresHashAndSendsLink() {
        issueFor(1L);

        RecordingVerificationEmailSender.Sent sent = sender.last();
        assertEquals("camila@utp.edu.pe", sent.toEmail());
        assertEquals("Camila", sent.recipientName());
        assertTrue(sent.link().startsWith(BASE_URL + "/api/v1/email-verifications/confirm?token="));
        assertTrue(repository.findByTokenHash(VerificationToken.hashOf(sent.token())).isPresent());
        assertTrue(repository.findByTokenHash(sent.token()).isEmpty());
    }

    @Test
    @DisplayName("Confirmar con el token correcto deja el correo verificado")
    void confirmMarksAsVerified() {
        issueFor(1L);

        EmailVerificationStatusDto status = handler.handle(new ConfirmEmailVerificationCommand(sender.last().token()));

        assertTrue(status.verified());
        assertEquals(1L, status.studentId());
        assertEquals(NOW, status.verifiedAt());
        assertTrue(repository.findVerifiedByStudentId(1L).isPresent());
    }

    @Test
    @DisplayName("Confirmar dos veces con el mismo token no falla")
    void confirmIsIdempotent() {
        issueFor(1L);
        String token = sender.last().token();

        handler.handle(new ConfirmEmailVerificationCommand(token));
        EmailVerificationStatusDto again = handler.handle(new ConfirmEmailVerificationCommand(token));

        assertTrue(again.verified());
    }

    @Test
    @DisplayName("Un token desconocido no se encuentra")
    void unknownTokenIsRejected() {
        assertThrows(InvalidVerificationTokenException.class,
                () -> handler.handle(new ConfirmEmailVerificationCommand("token-inventado")));
    }

    @Test
    @DisplayName("Un token en blanco es una petición inválida")
    void blankTokenIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> handler.handle(new ConfirmEmailVerificationCommand(" ")));
    }

    @Test
    @DisplayName("Un token vencido no confirma y el correo sigue sin verificar")
    void expiredTokenIsRejected() {
        issueFor(1L);
        String token = sender.last().token();

        assertThrows(VerificationTokenExpiredException.class,
                () -> handlerAt(NOW.plusSeconds(25 * 3600)).handle(new ConfirmEmailVerificationCommand(token)));
        assertTrue(repository.findVerifiedByStudentId(1L).isEmpty());
    }

    @Test
    @DisplayName("Reenviar antes del tiempo de espera se rechaza")
    void resendBeforeCooldownIsRejected() {
        issueFor(1L);

        assertThrows(ResendTooSoonException.class,
                () -> handlerAt(NOW.plusSeconds(10)).handle(new ResendEmailVerificationCommand(1L)));
        assertEquals(1, sender.sent().size());
    }

    @Test
    @DisplayName("Reenviar después del tiempo de espera emite un token nuevo")
    void resendAfterCooldownIssuesNewToken() {
        issueFor(1L);
        String first = sender.last().token();

        handlerAt(NOW.plusSeconds(61)).handle(new ResendEmailVerificationCommand(1L));

        assertEquals(2, sender.sent().size());
        assertNotEquals(first, sender.last().token());
        assertEquals("camila@utp.edu.pe", sender.last().toEmail());
        assertEquals("camila", sender.last().recipientName());
        assertEquals(2, repository.size());
    }

    @Test
    @DisplayName("Reenviar a un correo ya verificado se rechaza")
    void resendWhenAlreadyVerifiedIsRejected() {
        issueFor(1L);
        handler.handle(new ConfirmEmailVerificationCommand(sender.last().token()));

        assertThrows(EmailAlreadyVerifiedException.class,
                () -> handlerAt(NOW.plusSeconds(120)).handle(new ResendEmailVerificationCommand(1L)));
    }

    @Test
    @DisplayName("Reenviar sin una verificación previa no se encuentra")
    void resendWithoutPreviousVerificationIsRejected() {
        assertThrows(EmailVerificationNotFoundException.class,
                () -> handler.handle(new ResendEmailVerificationCommand(99L)));
    }

    @Test
    @DisplayName("La consulta de estado distingue verificado de pendiente")
    void statusQueryReflectsVerification() {
        GetEmailVerificationStatusQueryHandler query = new GetEmailVerificationStatusQueryHandler(repository);
        issueFor(1L);

        assertFalse(query.handle(new GetEmailVerificationStatusQuery(1L)).verified());

        handler.handle(new ConfirmEmailVerificationCommand(sender.last().token()));

        EmailVerificationStatusDto status = query.handle(new GetEmailVerificationStatusQuery(1L));
        assertTrue(status.verified());
        assertEquals(NOW, status.verifiedAt());
        assertFalse(query.handle(new GetEmailVerificationStatusQuery(2L)).verified());
    }
}
