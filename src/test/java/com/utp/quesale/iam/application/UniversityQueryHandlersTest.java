package com.utp.quesale.iam.application;

import com.utp.quesale.iam.application.dtos.UniversityDto;
import com.utp.quesale.iam.application.handlers.queries.GetUniversityQueryHandler;
import com.utp.quesale.iam.application.handlers.queries.ListUniversitiesQueryHandler;
import com.utp.quesale.iam.application.queries.GetUniversityQuery;
import com.utp.quesale.iam.application.queries.ListUniversitiesQuery;
import com.utp.quesale.iam.domain.aggregates.University;
import com.utp.quesale.iam.domain.exceptions.UniversityNotFoundException;
import com.utp.quesale.iam.domain.valueobjects.EmailDomain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Pruebas de los handlers de consulta: sin Spring ni base de datos
class UniversityQueryHandlersTest {

    private final InMemoryUniversityRepository repository = new InMemoryUniversityRepository();
    private final GetUniversityQueryHandler getHandler = new GetUniversityQueryHandler(repository);
    private final ListUniversitiesQueryHandler listHandler = new ListUniversitiesQueryHandler(repository);

    private University saveUniversity(String name, String domain) {
        return repository.save(new University(name, new EmailDomain(domain)));
    }

    @Test
    @DisplayName("Devuelve la universidad pedida por su id")
    void getsUniversityById() {
        University saved = saveUniversity("Universidad Tecnológica del Perú", "utp.edu.pe");

        UniversityDto dto = getHandler.handle(new GetUniversityQuery(saved.getId()));

        assertEquals(saved.getId(), dto.id());
        assertEquals("utp.edu.pe", dto.emailDomain());
    }

    @Test
    @DisplayName("Si la universidad no existe, lanza UniversityNotFoundException")
    void failsWhenUniversityDoesNotExist() {
        assertThrows(UniversityNotFoundException.class, () -> getHandler.handle(new GetUniversityQuery(99L)));
    }

    @Test
    @DisplayName("Lista todas las universidades registradas")
    void listsAllUniversities() {
        saveUniversity("Universidad Tecnológica del Perú", "utp.edu.pe");
        saveUniversity("Universidad Peruana de Ciencias Aplicadas", "upc.edu.pe");

        List<UniversityDto> dtos = listHandler.handle(new ListUniversitiesQuery());

        assertEquals(2, dtos.size());
        assertEquals("upc.edu.pe", dtos.get(1).emailDomain());
    }

    @Test
    @DisplayName("Sin universidades registradas, devuelve una lista vacía")
    void listsNothingWhenEmpty() {
        assertTrue(listHandler.handle(new ListUniversitiesQuery()).isEmpty());
    }
}
