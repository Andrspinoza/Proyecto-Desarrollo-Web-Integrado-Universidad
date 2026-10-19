package com.utp.quesale.profiles.infrastructure;

import com.utp.quesale.profiles.domain.aggregates.Skill;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.adapters.SkillRepositoryAdapter;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.mappers.SkillPersistenceMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({SkillRepositoryAdapter.class, SkillPersistenceMapper.class})
class SkillRepositoryAdapterTest {
    @Autowired
    private SkillRepositoryAdapter repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesAndReadsCatalogData() {
        Skill saved = repository.save(new Skill("Programacion"));
        // Fuerza la lectura desde H2 en lugar de usar la caché de JPA.
        entityManager.flush();
        entityManager.clear();

        Skill found = repository.findById(saved.getId()).orElseThrow();

        assertNotNull(saved.getId());
        assertEquals(saved.getId(), found.getId());
        assertEquals("Programacion", found.getName());
        assertTrue(found.pullDomainEvents().isEmpty());
    }

    @Test
    void findsNameIgnoringCase() {
        repository.save(new Skill("Programacion"));
        entityManager.flush();
        entityManager.clear();

        assertTrue(repository.existsByName("PROGRAMACION"));
        assertTrue(repository.existsByName("programacion"));
        assertFalse(repository.existsByName("Musica"));
    }

    @Test
    void listsCatalogAndChecksExistence() {
        Skill first = repository.save(new Skill("Programacion"));
        Skill second = repository.save(new Skill("Musica"));
        entityManager.flush();
        entityManager.clear();

        assertEquals(Set.of(first.getId(), second.getId()), repository.findAll().stream()
                .map(Skill::getId).collect(Collectors.toSet()));
        assertTrue(repository.existsById(first.getId()));
        assertTrue(repository.existsById(second.getId()));
        assertFalse(repository.existsById(Long.MAX_VALUE));
        assertTrue(repository.findById(Long.MAX_VALUE).isEmpty());
    }

    @Test
    void rejectsDuplicateName() {
        repository.save(new Skill("Programacion"));
        entityManager.flush();
        entityManager.clear();

        assertThrows(DataIntegrityViolationException.class,
                () -> repository.save(new Skill("Programacion")));
    }
}
