package com.utp.quesale.profiles.application;

import com.utp.quesale.profiles.application.commands.CreateInterestCommand;
import com.utp.quesale.profiles.application.dtos.InterestDto;
import com.utp.quesale.profiles.application.handlers.commands.InterestCommandHandler;
import com.utp.quesale.profiles.domain.aggregates.Interest;
import com.utp.quesale.profiles.domain.repositories.InterestRepository;
import com.utp.quesale.shared.domain.events.DomainEventDispatcher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InterestCommandHandlerTest {
    private final InterestRepository repository = mock(InterestRepository.class);
    private final DomainEventDispatcher events = mock(DomainEventDispatcher.class);
    private final InterestCommandHandler handler = new InterestCommandHandler(repository, events);

    @Test
    void savesNormalizedInterestAndDispatchesAfterSaving() {
        Interest saved = new Interest(7L, "Musica");
        when(repository.save(any(Interest.class))).thenReturn(saved);

        assertEquals(new InterestDto(7L, "Musica"), handler.handle(new CreateInterestCommand("  Musica  ")));

        var order = inOrder(repository, events);
        order.verify(repository).existsByName("Musica");
        order.verify(repository).save(argThat(interest -> interest.getId() == null
                && interest.getName().equals("Musica")));
        order.verify(events).dispatch(saved);
    }

    @Test
    void rejectsDuplicateWithoutSavingOrDispatching() {
        when(repository.existsByName("Musica")).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> handler.handle(new CreateInterestCommand(" Musica ")));

        verify(repository, never()).save(any());
        verifyNoInteractions(events);
    }

    @Test
    void rejectsInvalidNamesBeforeQueryingRepository() {
        for (String name : new String[]{null, "", "   ", "a".repeat(41)}) {
            assertThrows(IllegalArgumentException.class,
                    () -> handler.handle(new CreateInterestCommand(name)));
        }
        verifyNoInteractions(repository, events);
    }

    @Test
    void doesNotDispatchWhenSavingFails() {
        when(repository.save(any(Interest.class))).thenThrow(new IllegalStateException("Save failed"));

        assertThrows(IllegalStateException.class,
                () -> handler.handle(new CreateInterestCommand("Musica")));

        verifyNoInteractions(events);
    }
}
