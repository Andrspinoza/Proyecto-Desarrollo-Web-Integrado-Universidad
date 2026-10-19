package com.utp.quesale.iam.application.assemblers;

import com.utp.quesale.iam.application.dtos.UniversityDto;
import com.utp.quesale.iam.domain.aggregates.University;

// Pasa del agregado al resultado de la aplicación; no depende de HTTP
public final class IamDtoAssembler {

    private IamDtoAssembler() {
    }

    public static UniversityDto toDto(University university) {
        return new UniversityDto(
                university.getId(),
                university.getName(),
                university.getEmailDomain().value(),
                university.isActive(),
                university.getCreatedAt()
        );
    }
}
