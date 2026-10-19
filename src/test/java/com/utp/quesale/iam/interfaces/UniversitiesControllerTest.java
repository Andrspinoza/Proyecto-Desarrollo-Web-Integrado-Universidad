package com.utp.quesale.iam.interfaces;

import com.utp.quesale.iam.application.InMemoryUniversityRepository;
import com.utp.quesale.iam.application.handlers.commands.UniversityCommandHandler;
import com.utp.quesale.iam.application.handlers.queries.GetUniversityQueryHandler;
import com.utp.quesale.iam.application.handlers.queries.ListUniversitiesQueryHandler;
import com.utp.quesale.iam.interfaces.controllers.UniversitiesController;
import com.utp.quesale.shared.domain.events.DomainEventDispatcher;
import com.utp.quesale.shared.interfaces.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Prueba el controller con HTTP simulado y handlers reales sobre un repositorio en memoria
class UniversitiesControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        InMemoryUniversityRepository repository = new InMemoryUniversityRepository();
        DomainEventDispatcher noEvents = aggregate -> { };
        UniversitiesController controller = new UniversitiesController(
                new UniversityCommandHandler(repository, noEvents),
                new GetUniversityQueryHandler(repository),
                new ListUniversitiesQueryHandler(repository));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private void createUniversity(String name, String domain) throws Exception {
        mockMvc.perform(post("/api/v1/universities")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + name + "\",\"emailDomain\":\"" + domain + "\"}"));
    }

    @Test
    @DisplayName("POST registra la universidad y responde 201 con su ubicación")
    void createsUniversity() throws Exception {
        mockMvc.perform(post("/api/v1/universities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Universidad Tecnologica del Peru\",\"emailDomain\":\"UTP.edu.pe\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/universities/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.emailDomain").value("utp.edu.pe"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("POST con un dominio ya registrado responde 409")
    void rejectsDuplicatedDomain() throws Exception {
        createUniversity("Universidad Tecnologica del Peru", "utp.edu.pe");

        mockMvc.perform(post("/api/v1/universities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Otra\",\"emailDomain\":\"utp.edu.pe\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST con el nombre vacío responde 400")
    void rejectsBlankName() throws Exception {
        mockMvc.perform(post("/api/v1/universities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"emailDomain\":\"utp.edu.pe\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET por id devuelve la universidad")
    void findsUniversityById() throws Exception {
        createUniversity("Universidad Tecnologica del Peru", "utp.edu.pe");

        mockMvc.perform(get("/api/v1/universities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Universidad Tecnologica del Peru"));
    }

    @Test
    @DisplayName("GET de una universidad que no existe responde 404")
    void failsWhenUniversityDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/universities/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET lista todas las universidades")
    void listsUniversities() throws Exception {
        createUniversity("Universidad Tecnologica del Peru", "utp.edu.pe");
        createUniversity("Universidad Peruana de Ciencias Aplicadas", "upc.edu.pe");

        mockMvc.perform(get("/api/v1/universities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].emailDomain").value("upc.edu.pe"));
    }
}
