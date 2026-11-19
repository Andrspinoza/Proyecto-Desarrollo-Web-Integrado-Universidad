package com.utp.quesale.iam.interfaces;

import com.utp.quesale.iam.application.InMemoryEmailVerificationRepository;
import com.utp.quesale.iam.application.RecordingVerificationEmailSender;
import com.utp.quesale.iam.application.commands.IssueEmailVerificationCommand;
import com.utp.quesale.iam.application.handlers.commands.EmailVerificationCommandHandler;
import com.utp.quesale.iam.application.handlers.queries.GetEmailVerificationStatusQueryHandler;
import com.utp.quesale.iam.interfaces.controllers.EmailVerificationsController;
import com.utp.quesale.shared.interfaces.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmailVerificationsControllerTest {

    private static final Instant NOW = Instant.parse("2026-11-18T12:00:00Z");

    private MockMvc mockMvc;
    private EmailVerificationCommandHandler commandHandler;
    private RecordingVerificationEmailSender sender;

    @BeforeEach
    void setUp() {
        InMemoryEmailVerificationRepository repository = new InMemoryEmailVerificationRepository();
        sender = new RecordingVerificationEmailSender();
        commandHandler = new EmailVerificationCommandHandler(repository, sender,
                Clock.fixed(NOW, ZoneOffset.UTC), "https://quesale.example", 24, 0);
        EmailVerificationsController controller = new EmailVerificationsController(commandHandler,
                new GetEmailVerificationStatusQueryHandler(repository));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private String issueToken(Long studentId) {
        commandHandler.handle(new IssueEmailVerificationCommand(studentId, "camila@utp.edu.pe", "Camila"));
        return sender.last().token();
    }

    @Test
    @DisplayName("GET /confirm con el token del correo deja al estudiante verificado")
    void confirmWithValidToken() throws Exception {
        String token = issueToken(3L);

        mockMvc.perform(get("/api/v1/email-verifications/confirm").param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(3))
                .andExpect(jsonPath("$.verified").value(true));
    }

    @Test
    @DisplayName("GET /confirm con un token desconocido responde 404")
    void confirmWithUnknownToken() throws Exception {
        mockMvc.perform(get("/api/v1/email-verifications/confirm").param("token", "inventado"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /confirm sin token responde 400")
    void confirmWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/email-verifications/confirm"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /status refleja pendiente y luego verificado")
    void statusBeforeAndAfter() throws Exception {
        String token = issueToken(3L);

        mockMvc.perform(get("/api/v1/email-verifications/status").header("X-Student-Id", 3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(false));

        mockMvc.perform(get("/api/v1/email-verifications/confirm").param("token", token));

        mockMvc.perform(get("/api/v1/email-verifications/status").header("X-Student-Id", 3))
                .andExpect(jsonPath("$.verified").value(true));
    }

    @Test
    @DisplayName("GET /status sin el header de estudiante responde 400")
    void statusWithoutHeader() throws Exception {
        mockMvc.perform(get("/api/v1/email-verifications/status"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /resend responde 202 y envía otro correo")
    void resendAccepted() throws Exception {
        issueToken(3L);

        mockMvc.perform(post("/api/v1/email-verifications/resend").header("X-Student-Id", 3))
                .andExpect(status().isAccepted());

        org.junit.jupiter.api.Assertions.assertEquals(2, sender.sent().size());
    }

    @Test
    @DisplayName("POST /resend de un correo ya verificado responde 409")
    void resendWhenVerified() throws Exception {
        String token = issueToken(3L);
        mockMvc.perform(get("/api/v1/email-verifications/confirm").param("token", token));

        mockMvc.perform(post("/api/v1/email-verifications/resend").header("X-Student-Id", 3))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /resend de un estudiante sin verificación previa responde 404")
    void resendWithoutPrevious() throws Exception {
        mockMvc.perform(post("/api/v1/email-verifications/resend").header("X-Student-Id", 99))
                .andExpect(status().isNotFound());
    }
}
