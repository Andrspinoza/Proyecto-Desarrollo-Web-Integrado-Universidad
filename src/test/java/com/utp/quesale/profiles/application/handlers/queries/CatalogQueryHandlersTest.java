package com.utp.quesale.profiles.application.handlers.queries;

import com.utp.quesale.profiles.application.dtos.InterestDto;
import com.utp.quesale.profiles.application.dtos.SkillDto;
import com.utp.quesale.profiles.application.queries.ListInterestsQuery;
import com.utp.quesale.profiles.application.queries.ListSkillsQuery;
import com.utp.quesale.profiles.domain.aggregates.Interest;
import com.utp.quesale.profiles.domain.aggregates.Skill;
import com.utp.quesale.profiles.domain.repositories.InterestRepository;
import com.utp.quesale.profiles.domain.repositories.SkillRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CatalogQueryHandlersTest {
    @Test
    void listsInterestsAsDtos() {
        InterestRepository repository = mock(InterestRepository.class);
        when(repository.findAll()).thenReturn(List.of(new Interest(7L, "Musica"), new Interest(8L, "Cine")));
        ListInterestsQueryHandler handler = new ListInterestsQueryHandler(repository);

        assertEquals(List.of(new InterestDto(7L, "Musica"), new InterestDto(8L, "Cine")),
                handler.handle(new ListInterestsQuery()));
    }

    @Test
    void returnsEmptyListWhenThereAreNoInterests() {
        InterestRepository repository = mock(InterestRepository.class);
        when(repository.findAll()).thenReturn(List.of());
        ListInterestsQueryHandler handler = new ListInterestsQueryHandler(repository);

        assertEquals(List.of(), handler.handle(new ListInterestsQuery()));
    }

    @Test
    void listsSkillsAsDtos() {
        SkillRepository repository = mock(SkillRepository.class);
        when(repository.findAll()).thenReturn(List.of(new Skill(9L, "Programacion"), new Skill(10L, "Dibujo")));
        ListSkillsQueryHandler handler = new ListSkillsQueryHandler(repository);

        assertEquals(List.of(new SkillDto(9L, "Programacion"), new SkillDto(10L, "Dibujo")),
                handler.handle(new ListSkillsQuery()));
    }

    @Test
    void returnsEmptyListWhenThereAreNoSkills() {
        SkillRepository repository = mock(SkillRepository.class);
        when(repository.findAll()).thenReturn(List.of());
        ListSkillsQueryHandler handler = new ListSkillsQueryHandler(repository);

        assertEquals(List.of(), handler.handle(new ListSkillsQuery()));
    }
}
