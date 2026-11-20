package com.utp.quesale.iam.infrastructure.security;

import com.utp.quesale.iam.domain.aggregates.University;
import com.utp.quesale.iam.domain.repositories.UniversityRepository;
import com.utp.quesale.iam.domain.services.VerificationEmailSender;
import com.utp.quesale.iam.domain.valueobjects.EmailDomain;
import com.utp.quesale.iam.infrastructure.mail.PasswordResetEmailSender;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:pwreset;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.auth.jwt-secret=a-secret-with-at-least-32-bytes-long",
        "app.verification.base-url=https://que-sale.me"
})
class PasswordResetFlowIntegrationTest {
    @Autowired WebApplicationContext context;
    @Autowired UniversityRepository universities;
    @MockitoBean VerificationEmailSender verificationSender;
    @MockitoBean PasswordResetEmailSender resetSender;

    @Test void forgotThenResetThenLoginWithTheNewPassword() throws Exception {
        var university = universities.save(new University("Reset U", new EmailDomain("reset.edu")));
        var jwt = new JwtService("a-secret-with-at-least-32-bytes-long");
        var mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(new JwtHttpFilter(jwt, true)).build();
        String registration = "{\"universityId\":" + university.getId()
                + ",\"email\":\"ana@reset.edu\",\"password\":\"Vieja-clave-1\",\"firstName\":\"Ana\",\"lastName\":\"Perez\"}";
        mvc.perform(post("/api/v1/auth/register").contentType("application/json").content(registration)).andExpect(status().isCreated());
        var verificationLink = ArgumentCaptor.forClass(String.class);
        verify(verificationSender, timeout(3000)).send(anyString(), anyString(), verificationLink.capture());
        String verificationToken = verificationLink.getValue().substring(verificationLink.getValue().indexOf("token=") + 6);
        mvc.perform(get("/api/v1/email-verifications/confirm").param("token", verificationToken)).andExpect(status().isOk());

        mvc.perform(post("/api/v1/auth/forgot-password").contentType("application/json").content("{\"email\":\"nadie@reset.edu\"}"))
                .andExpect(status().isAccepted());
        verifyNoInteractions(resetSender);

        mvc.perform(post("/api/v1/auth/forgot-password").contentType("application/json").content("{\"email\":\"ana@reset.edu\"}"))
                .andExpect(status().isAccepted());
        var resetLink = ArgumentCaptor.forClass(String.class);
        verify(resetSender).send(eq("ana@reset.edu"), eq("ana"), resetLink.capture());
        String token = resetLink.getValue().substring(resetLink.getValue().indexOf("token=") + 6);

        String body = "{\"token\":\"" + token + "\",\"password\":\"Nueva-clave-2\"}";
        mvc.perform(post("/api/v1/auth/reset-password").contentType("application/json").content(body)).andExpect(status().isNoContent());
        mvc.perform(post("/api/v1/auth/reset-password").contentType("application/json").content(body)).andExpect(status().isBadRequest());

        mvc.perform(post("/api/v1/auth/login").contentType("application/json")
                .content("{\"email\":\"ana@reset.edu\",\"password\":\"Vieja-clave-1\"}")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/login").contentType("application/json")
                .content("{\"email\":\"ana@reset.edu\",\"password\":\"Nueva-clave-2\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test void resetWithAShortPasswordOrMissingTokenIsRejected() throws Exception {
        var jwt = new JwtService("a-secret-with-at-least-32-bytes-long");
        var mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(new JwtHttpFilter(jwt, true)).build();
        mvc.perform(post("/api/v1/auth/reset-password").contentType("application/json")
                .content("{\"token\":\"x\",\"password\":\"corta\"}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/auth/reset-password").contentType("application/json")
                .content("{\"password\":\"Nueva-clave-2\"}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/auth/forgot-password").contentType("application/json")
                .content("{\"email\":\"no-es-correo\"}")).andExpect(status().isBadRequest());
    }
}
