package com.utp.quesale.profiles.application.assemblers;

import com.utp.quesale.profiles.application.dtos.InterestDto;
import com.utp.quesale.profiles.application.dtos.SkillDto;
import com.utp.quesale.profiles.domain.aggregates.Interest;
import com.utp.quesale.profiles.domain.aggregates.Skill;

public final class ProfilesDtoAssembler {
    private ProfilesDtoAssembler() {
    }

    public static InterestDto toDto(Interest interest) {
        return new InterestDto(interest.getId(), interest.getName());
    }

    public static SkillDto toDto(Skill skill) {
        return new SkillDto(skill.getId(), skill.getName());
    }
}
