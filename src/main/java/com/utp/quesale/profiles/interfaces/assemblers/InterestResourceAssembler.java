package com.utp.quesale.profiles.interfaces.assemblers;

import com.utp.quesale.profiles.application.commands.CreateInterestCommand;
import com.utp.quesale.profiles.application.dtos.InterestDto;
import com.utp.quesale.profiles.interfaces.resources.CreateInterestResource;
import com.utp.quesale.profiles.interfaces.resources.InterestResource;

public class InterestResourceAssembler {
    public static CreateInterestCommand toCommandFromResource(CreateInterestResource resource) {
        return new CreateInterestCommand(resource.name());
    }

    public static InterestResource toResourceFromDto(InterestDto dto) {
        return new InterestResource(dto.id(), dto.name());
    }
}
