package com.utp.quesale.profiles.domain;

import com.utp.quesale.profiles.domain.aggregates.Skill;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class SkillTest {
    @Test
    void createsSkillWithTrimmedName() {
        Skill skill = new Skill("  Programacion  ");

        assertNull(skill.getId());
        assertEquals("Programacion", skill.getName());
    }

    @Test
    void rehydratesIdentityWithoutEvents() {
        Skill skill = new Skill(7L, "Java");

        assertEquals(7L, skill.getId());
        assertEquals("Java", skill.getName());
        assertTrue(skill.pullDomainEvents().isEmpty());
    }

    @Test
    void acceptsFortyCharactersAfterTrimming() {
        String name = "a".repeat(40);
        assertEquals(name, new Skill(" " + name + " ").getName());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void rejectsMissingName(String name) {
        assertThrows(IllegalArgumentException.class, () -> new Skill(name));
    }

    @Test
    void rejectsMoreThanFortyCharacters() {
        assertThrows(IllegalArgumentException.class, () -> new Skill("a".repeat(41)));
    }
}
