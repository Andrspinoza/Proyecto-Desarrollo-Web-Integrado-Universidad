package com.utp.quesale.profiles.interfaces;

import com.utp.quesale.profiles.application.handlers.commands.SkillCommandHandler;
import com.utp.quesale.profiles.application.handlers.queries.ListSkillsQueryHandler;
import com.utp.quesale.profiles.domain.aggregates.Skill;
import com.utp.quesale.profiles.domain.repositories.SkillRepository;
import com.utp.quesale.profiles.interfaces.controllers.SkillsController;
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

class SkillsControllerTest {
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        var repository = new MemorySkills();
        mvc = MockMvcBuilders.standaloneSetup(new SkillsController(
                new SkillCommandHandler(repository, aggregate -> { }),
                new ListSkillsQueryHandler(repository)))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void createsNormalizedSkill() throws Exception {
        mvc.perform(post("/api/v1/skills").header("X-Student-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"  Programacion  \"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/skills/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Programacion"));
        mvc.perform(get("/api/v1/skills")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Programacion"));
    }

    @Test
    void listsEmptyCatalog() throws Exception {
        mvc.perform(get("/api/v1/skills")).andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void duplicateNameReturnsConflict() throws Exception {
        mvc.perform(post("/api/v1/skills").header("X-Student-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Programacion\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/skills").header("X-Student-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\" programacion \"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidNamesReturnBadRequestWithoutSaving() throws Exception {
        for (String body : List.of("{}", "{\"name\":null}", "{\"name\":\"   \"}",
                "{\"name\":\"" + "a".repeat(41) + "\"}", "{")) {
            mvc.perform(post("/api/v1/skills").header("X-Student-Id", 1)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(get("/api/v1/skills")).andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void creationRequiresProvisionalAdminHeader() throws Exception {
        mvc.perform(post("/api/v1/skills").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Programacion\"}"))
                .andExpect(status().isBadRequest());
    }

    private static class MemorySkills implements SkillRepository {
        private final List<Skill> skills = new ArrayList<>();
        public Skill save(Skill skill) {
            Skill saved = new Skill((long) skills.size() + 1, skill.getName());
            skills.add(saved);
            return saved;
        }
        public Optional<Skill> findById(Long id) {
            return skills.stream().filter(skill -> skill.getId().equals(id)).findFirst();
        }
        public List<Skill> findAll() { return List.copyOf(skills); }
        public boolean existsById(Long id) { return findById(id).isPresent(); }
        public boolean existsByName(String name) {
            return skills.stream().anyMatch(skill -> skill.getName().equalsIgnoreCase(name));
        }
    }
}
