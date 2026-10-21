package com.utp.quesale.profiles.application;

import com.utp.quesale.profiles.application.assemblers.ProfilesDtoAssembler;
import com.utp.quesale.profiles.application.dtos.InterestDto;
import com.utp.quesale.profiles.application.dtos.SkillDto;
import com.utp.quesale.profiles.domain.aggregates.Interest;
import com.utp.quesale.profiles.domain.aggregates.Skill;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProfilesDtoAssemblerTest {
    @Test
    void convertsInterestToDto() {
        Interest interest = new Interest(7L, "Musica");

        assertEquals(new InterestDto(7L, "Musica"), ProfilesDtoAssembler.toDto(interest));
    }

    @Test
    void convertsSkillToDto() {
        Skill skill = new Skill(9L, "Programacion");

        assertEquals(new SkillDto(9L, "Programacion"), ProfilesDtoAssembler.toDto(skill));
    }
}
