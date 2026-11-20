package com.utp.quesale.profiles.infrastructure;

import com.utp.quesale.iam.domain.aggregates.University;
import com.utp.quesale.iam.domain.repositories.UniversityRepository;
import com.utp.quesale.iam.domain.valueobjects.EmailDomain;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:studentphoto;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.auth.enabled=false"
})
class StudentPhotoIntegrationTest {
    @Autowired WebApplicationContext context;
    @Autowired UniversityRepository universities;

    private static byte[] jpeg() {
        byte[] data = new byte[2048];
        data[0] = (byte) 0xFF; data[1] = (byte) 0xD8; data[2] = (byte) 0xFF;
        for (int i = 3; i < data.length; i++) data[i] = (byte) i;
        return data;
    }

    @Test void uploadThenReadTheUrlThenDownloadReplaceAndDelete() throws Exception {
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
        var university = universities.save(new University("Foto U", new EmailDomain("foto.edu")));
        mvc.perform(post("/api/v1/students").contentType("application/json").content(
                "{\"universityId\":" + university.getId() + ",\"email\":\"ana@foto.edu\",\"firstName\":\"Ana\",\"lastName\":\"Perez\"}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/v1/students/me").header("X-Student-Id", 1)).andExpect(jsonPath("$.photoUrl").doesNotExist());

        var upload = mvc.perform(multipart("/api/v1/students/me/photo")
                        .file(new MockMultipartFile("file", "yo.jpg", "image/jpeg", jpeg())).with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }).header("X-Student-Id", 1))
                .andExpect(status().isOk()).andExpect(jsonPath("$.photoUrl").isNotEmpty()).andReturn();
        String url = com.jayway.jsonpath.JsonPath.read(upload.getResponse().getContentAsString(), "$.photoUrl");

        mvc.perform(get("/api/v1/students/me").header("X-Student-Id", 1)).andExpect(jsonPath("$.photoUrl").value(url));

        byte[] downloaded = mvc.perform(get(url))
                .andExpect(status().isOk()).andExpect(content().contentType("image/jpeg"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andReturn().getResponse().getContentAsByteArray();
        assertArrayEquals(jpeg(), downloaded);

        var replace = mvc.perform(multipart("/api/v1/students/me/photo")
                        .file(new MockMultipartFile("file", "otra.jpg", "image/jpeg", jpeg())).with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }).header("X-Student-Id", 1))
                .andExpect(status().isOk()).andReturn();
        String newUrl = com.jayway.jsonpath.JsonPath.read(replace.getResponse().getContentAsString(), "$.photoUrl");
        mvc.perform(get(url)).andExpect(status().isNotFound());
        mvc.perform(get(newUrl)).andExpect(status().isOk());

        mvc.perform(delete("/api/v1/students/me/photo").header("X-Student-Id", 1)).andExpect(status().isNoContent());
        mvc.perform(get(newUrl)).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/students/me").header("X-Student-Id", 1)).andExpect(jsonPath("$.photoUrl").doesNotExist());
    }

    @Test void aFileThatIsNotAnImageIsRejectedEvenWithAnImageName() throws Exception {
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
        var university = universities.save(new University("Foto Dos U", new EmailDomain("fotodos.edu")));
        mvc.perform(post("/api/v1/students").contentType("application/json").content(
                "{\"universityId\":" + university.getId() + ",\"email\":\"beto@fotodos.edu\",\"firstName\":\"Beto\",\"lastName\":\"Diaz\"}"));
        mvc.perform(multipart("/api/v1/students/me/photo")
                        .file(new MockMultipartFile("file", "falsa.jpg", "image/jpeg", "no soy una imagen".getBytes()))
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .header("X-Student-Id", 2))
                .andExpect(status().isBadRequest());
    }
}
