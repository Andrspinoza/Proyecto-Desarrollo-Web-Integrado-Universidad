package com.utp.quesale.iam.infrastructure.security;

import com.utp.quesale.iam.domain.services.VerificationEmailSender;
import com.utp.quesale.iam.domain.aggregates.University;
import com.utp.quesale.iam.domain.repositories.UniversityRepository;
import com.utp.quesale.iam.domain.valueobjects.EmailDomain;
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
        "spring.datasource.url=jdbc:h2:mem:jwtflow;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.auth.jwt-secret=a-secret-with-at-least-32-bytes-long"
})
class AuthFlowIntegrationTest {
    @Autowired WebApplicationContext context;
    @Autowired UniversityRepository universities;
    @MockitoBean VerificationEmailSender sender;

    @Test void registrationVerificationAndLogin() throws Exception {
        var university = universities.save(new University("Prueba", new EmailDomain("example.edu")));
        var jwt = new JwtService("a-secret-with-at-least-32-bytes-long");
        var mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(new JwtHttpFilter(jwt, true)).build();
        String registration = "{\"universityId\":" + university.getId() + ",\"email\":\"student@example.edu\",\"password\":\"Password123\",\"firstName\":\"Ana\",\"lastName\":\"Perez\"}";
        mvc.perform(post("/api/v1/auth/register").contentType("application/json").content(registration)).andExpect(status().isCreated());
        String login = "{\"email\":\"student@example.edu\",\"password\":\"Password123\"}";
        mvc.perform(post("/api/v1/auth/login").contentType("application/json").content(login)).andExpect(status().isForbidden());
        var link = ArgumentCaptor.forClass(String.class);
        verify(sender, timeout(3000)).send(anyString(), anyString(), link.capture());
        String token = link.getValue().substring(link.getValue().indexOf("token=") + 6);
        mvc.perform(get("/api/v1/email-verifications/confirm").param("token", token)).andExpect(status().isOk());
        mvc.perform(post("/api/v1/auth/login").contentType("application/json").content(login))
                .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").isNotEmpty());
        mvc.perform(get("/api/v1/meetups").header("X-Student-Id", "1")).andExpect(status().isUnauthorized());
    }
}
