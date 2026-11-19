package com.utp.quesale.iam.infrastructure.security;

import com.utp.quesale.iam.domain.services.VerificationEmailSender;
import com.utp.quesale.iam.domain.aggregates.University;
import com.utp.quesale.iam.domain.repositories.UniversityRepository;
import com.utp.quesale.iam.domain.valueobjects.EmailDomain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
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
        "spring.datasource.url=jdbc:mysql://127.0.0.1:13307/jwt_test?allowPublicKeyRetrieval=true&useSSL=false",
        "spring.datasource.username=root", "spring.datasource.password=temporary_demo_test_49183",
        "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver", "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.auth.jwt-secret=a-secret-with-at-least-32-bytes-long"
})
@EnabledIfEnvironmentVariable(named = "RUN_ISOLATED_MYSQL_TEST", matches = "true")
class AuthFlowMySqlTest {
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
