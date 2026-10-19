package com.utp.quesale.iam.interfaces.assemblers;

import com.utp.quesale.iam.application.commands.CreateUniversityCommand;
import com.utp.quesale.iam.application.dtos.UniversityDto;
import com.utp.quesale.iam.interfaces.resources.CreateUniversityResource;
import com.utp.quesale.iam.interfaces.resources.UniversityResource;

public class UniversityResourceAssembler {

    public static CreateUniversityCommand toCommandFromResource(CreateUniversityResource resource) {
        return new CreateUniversityCommand(resource.name(), resource.emailDomain());
    }

    public static UniversityResource toResourceFromDto(UniversityDto dto) {
        return new UniversityResource(
                dto.id(),
                dto.name(),
                dto.emailDomain(),
                dto.active(),
                dto.createdAt()
        );
    }
}
