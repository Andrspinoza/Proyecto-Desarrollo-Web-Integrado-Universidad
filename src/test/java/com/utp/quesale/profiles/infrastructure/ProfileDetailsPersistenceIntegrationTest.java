package com.utp.quesale.profiles.infrastructure;

import com.utp.quesale.iam.domain.aggregates.University;
import com.utp.quesale.iam.domain.repositories.UniversityRepository;
import com.utp.quesale.iam.domain.valueobjects.EmailDomain;
import com.utp.quesale.profiles.application.commands.CreateInterestCommand;
import com.utp.quesale.profiles.application.handlers.commands.InterestCommandHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:profiledetails;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.auth.enabled=false"
})
class ProfileDetailsPersistenceIntegrationTest {
    @Autowired WebApplicationContext context;
    @Autowired UniversityRepository universities;
    @Autowired InterestCommandHandler interests;

    @Test void genderBirthDateCampusAndInterestsSurviveARoundTripThroughTheDatabase() throws Exception {
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
        var university = universities.save(new University("Detalles U", new EmailDomain("detalles.edu")));
        var cafe = interests.handle(new CreateInterestCommand("Cafe"));
        var voley = interests.handle(new CreateInterestCommand("Voley"));
        mvc.perform(post("/api/v1/students").contentType("application/json").content(
                "{\"universityId\":" + university.getId() + ",\"email\":\"luis@detalles.edu\",\"firstName\":\"Luis\",\"lastName\":\"Alanya\"}"))
                .andExpect(status().isCreated());
        String body = "{\"career\":\"Ingenieria de Sistemas\",\"cycle\":7,\"description\":\"Hola\",\"visibility\":\"PUBLIC\","
                + "\"gender\":\"MALE\",\"birthDate\":\"2002-03-04\",\"campus\":\"Lima Centro\",\"interestIds\":["
                + cafe.id() + "," + voley.id() + "],\"skillIds\":[]}";
        mvc.perform(put("/api/v1/students/me/profile").header("X-Student-Id", 1).contentType("application/json").content(body))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/students/me").header("X-Student-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.gender").value("MALE"))
                .andExpect(jsonPath("$.profile.campus").value("Lima Centro"))
                .andExpect(jsonPath("$.profile.birthDate").value("2002-03-04"))
                .andExpect(jsonPath("$.profile.interestIds.length()").value(2))
                .andExpect(jsonPath("$.profile.age").isNumber());
        mvc.perform(put("/api/v1/students/me/profile").header("X-Student-Id", 1).contentType("application/json")
                .content(body.replace("[" + cafe.id() + "," + voley.id() + "]", "[" + voley.id() + "]")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.profile.interestIds.length()").value(1));
        mvc.perform(put("/api/v1/students/me/profile").header("X-Student-Id", 1).contentType("application/json")
                .content(body.replace("[" + cafe.id() + "," + voley.id() + "]", "[9999]")))
                .andExpect(status().isBadRequest());
    }
}
