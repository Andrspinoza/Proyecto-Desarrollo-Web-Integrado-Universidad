package com.utp.quesale.iam.interfaces.assemblers;

import com.utp.quesale.iam.domain.aggregates.University;
import com.utp.quesale.iam.interfaces.resources.UniversityResource;

public class UniversityResourceAssembler {

    public static UniversityResource toResourceFromEntity(University university) {
        return new UniversityResource(
                university.getId(),
                university.getName(),
                university.getEmailDomain().value(),
                university.isActive(),
                university.getCreatedAt()
        );
    }
}
