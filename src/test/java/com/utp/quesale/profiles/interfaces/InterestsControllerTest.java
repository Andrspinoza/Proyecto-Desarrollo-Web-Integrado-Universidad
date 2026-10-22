package com.utp.quesale.profiles.interfaces;

import com.utp.quesale.profiles.application.handlers.commands.InterestCommandHandler;
import com.utp.quesale.profiles.application.handlers.queries.ListInterestsQueryHandler;
import com.utp.quesale.profiles.domain.aggregates.Interest;
import com.utp.quesale.profiles.domain.repositories.InterestRepository;
import com.utp.quesale.profiles.interfaces.controllers.InterestsController;
import com.utp.quesale.shared.interfaces.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class InterestsControllerTest {
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        var repository = new MemoryInterests();
        mvc = MockMvcBuilders.standaloneSetup(new InterestsController(
                new InterestCommandHandler(repository, aggregate -> { }),
                new ListInterestsQueryHandler(repository)))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void createsNormalizedInterest() throws Exception {
        mvc.perform(post("/api/v1/interests").header("X-Student-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"  Musica  \"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/interests/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Musica"));
        mvc.perform(get("/api/v1/interests")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Musica"));
    }

    @Test
    void listsEmptyCatalog() throws Exception {
        mvc.perform(get("/api/v1/interests")).andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void duplicateNameReturnsConflict() throws Exception {
        mvc.perform(post("/api/v1/interests").header("X-Student-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Musica\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/interests").header("X-Student-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\" musica \"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidNamesReturnBadRequestWithoutSaving() throws Exception {
        for (String body : List.of("{}", "{\"name\":null}", "{\"name\":\"   \"}",
                "{\"name\":\"" + "a".repeat(41) + "\"}", "{")) {
            mvc.perform(post("/api/v1/interests").header("X-Student-Id", 1)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(get("/api/v1/interests")).andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void creationRequiresProvisionalAdminHeader() throws Exception {
        mvc.perform(post("/api/v1/interests").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Musica\"}"))
                .andExpect(status().isBadRequest());
    }

    private static class MemoryInterests implements InterestRepository {
        private final List<Interest> interests = new ArrayList<>();
        public Interest save(Interest interest) {
            Interest saved = new Interest((long) interests.size() + 1, interest.getName());
            interests.add(saved);
            return saved;
        }
        public Optional<Interest> findById(Long id) {
            return interests.stream().filter(interest -> interest.getId().equals(id)).findFirst();
        }
        public List<Interest> findAll() { return List.copyOf(interests); }
        public boolean existsById(Long id) { return findById(id).isPresent(); }
        public boolean existsByName(String name) {
            return interests.stream().anyMatch(interest -> interest.getName().equalsIgnoreCase(name));
        }
    }
}
