package com.utp.quesale.profiles.interfaces.assemblers;

import com.utp.quesale.profiles.application.commands.CreateSkillCommand;
import com.utp.quesale.profiles.application.dtos.SkillDto;
import com.utp.quesale.profiles.interfaces.resources.CreateSkillResource;
import com.utp.quesale.profiles.interfaces.resources.SkillResource;

public class SkillResourceAssembler {
    public static CreateSkillCommand toCommandFromResource(CreateSkillResource resource) {
        return new CreateSkillCommand(resource.name());
    }

    public static SkillResource toResourceFromDto(SkillDto dto) {
        return new SkillResource(dto.id(), dto.name());
    }
}
