package com.utp.quesale.profiles.application;

import com.utp.quesale.profiles.application.commands.CreateSkillCommand;
import com.utp.quesale.profiles.application.dtos.SkillDto;
import com.utp.quesale.profiles.application.handlers.commands.SkillCommandHandler;
import com.utp.quesale.profiles.domain.aggregates.Skill;
import com.utp.quesale.profiles.domain.repositories.SkillRepository;
import com.utp.quesale.shared.domain.events.DomainEventDispatcher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SkillCommandHandlerTest {
    private final SkillRepository repository = mock(SkillRepository.class);
    private final DomainEventDispatcher events = mock(DomainEventDispatcher.class);
    private final SkillCommandHandler handler = new SkillCommandHandler(repository, events);

    @Test
    void savesNormalizedSkillAndDispatchesAfterSaving() {
        Skill saved = new Skill(7L, "Programacion");
        when(repository.save(any(Skill.class))).thenReturn(saved);

        assertEquals(new SkillDto(7L, "Programacion"), handler.handle(new CreateSkillCommand("  Programacion  ")));

        var order = inOrder(repository, events);
        order.verify(repository).existsByName("Programacion");
        order.verify(repository).save(argThat(skill -> skill.getId() == null
                && skill.getName().equals("Programacion")));
        order.verify(events).dispatch(saved);
    }

    @Test
    void rejectsDuplicateWithoutSavingOrDispatching() {
        when(repository.existsByName("Programacion")).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> handler.handle(new CreateSkillCommand(" Programacion ")));

        verify(repository, never()).save(any());
        verifyNoInteractions(events);
    }

    @Test
    void rejectsInvalidNamesBeforeQueryingRepository() {
        for (String name : new String[]{null, "", "   ", "a".repeat(41)}) {
            assertThrows(IllegalArgumentException.class,
                    () -> handler.handle(new CreateSkillCommand(name)));
        }
        verifyNoInteractions(repository, events);
    }

    @Test
    void doesNotDispatchWhenSavingFails() {
        when(repository.save(any(Skill.class))).thenThrow(new IllegalStateException("Save failed"));

        assertThrows(IllegalStateException.class,
                () -> handler.handle(new CreateSkillCommand("Programacion")));

        verifyNoInteractions(events);
    }
}

