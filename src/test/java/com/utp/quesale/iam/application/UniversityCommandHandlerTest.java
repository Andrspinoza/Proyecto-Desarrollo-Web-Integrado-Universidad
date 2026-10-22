package com.utp.quesale.iam.application;

import com.utp.quesale.iam.application.commands.CreateUniversityCommand;
import com.utp.quesale.iam.application.dtos.UniversityDto;
import com.utp.quesale.iam.application.handlers.commands.UniversityCommandHandler;
import com.utp.quesale.iam.domain.exceptions.UniversityAlreadyExistsException;
import com.utp.quesale.shared.domain.events.DomainEventDispatcher;
import com.utp.quesale.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UniversityCommandHandlerTest {

    private final InMemoryUniversityRepository repository = new InMemoryUniversityRepository();
    private final List<AbstractDomainAggregateRoot<?>> dispatched = new ArrayList<>();
    private final DomainEventDispatcher events = dispatched::add;
    private final UniversityCommandHandler handler = new UniversityCommandHandler(repository, events);

    @Test
    @DisplayName("Registra la universidad y devuelve su DTO con el id generado")
    void createsUniversity() {
        UniversityDto dto = handler.handle(new CreateUniversityCommand("Universidad Tecnológica del Perú", "UTP.edu.pe"));

        assertEquals(1L, dto.id());
        assertEquals("Universidad Tecnológica del Perú", dto.name());
        assertEquals("utp.edu.pe", dto.emailDomain());
        assertTrue(dto.active());
        assertTrue(repository.existsById(1L));
    }

    @Test
    @DisplayName("No registra dos universidades con el mismo dominio de correo")
    void rejectsDuplicatedDomain() {
        handler.handle(new CreateUniversityCommand("Universidad Tecnológica del Perú", "utp.edu.pe"));

        assertThrows(UniversityAlreadyExistsException.class,
                () -> handler.handle(new CreateUniversityCommand("Otra universidad", "UTP.EDU.PE")));
        assertEquals(1, repository.findAll().size());
    }

    @Test
    @DisplayName("Rechaza un dominio de correo inválido y no guarda nada")
    void rejectsInvalidDomain() {
        assertThrows(IllegalArgumentException.class,
                () -> handler.handle(new CreateUniversityCommand("Universidad", "utp")));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    @DisplayName("Despacha el agregado guardado para publicar sus eventos")
    void dispatchesSavedAggregate() {
        handler.handle(new CreateUniversityCommand("Universidad Tecnológica del Perú", "utp.edu.pe"));

        assertEquals(1, dispatched.size());
    }

    @Test
    @DisplayName("Si la regla falla, no se despacha ningún evento")
    void doesNotDispatchWhenItFails() {
        assertThrows(IllegalArgumentException.class,
                () -> handler.handle(new CreateUniversityCommand(" ", "utp.edu.pe")));

        assertTrue(dispatched.isEmpty());
    }
}
