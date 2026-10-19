package com.utp.quesale.iam.application.handlers.queries;

import com.utp.quesale.iam.application.assemblers.IamDtoAssembler;
import com.utp.quesale.iam.application.dtos.UniversityDto;
import com.utp.quesale.iam.application.queries.GetUniversityQuery;
import com.utp.quesale.iam.domain.exceptions.UniversityNotFoundException;
import com.utp.quesale.iam.domain.repositories.UniversityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetUniversityQueryHandler {

    private final UniversityRepository universityRepository;

    public GetUniversityQueryHandler(UniversityRepository universityRepository) {
        this.universityRepository = universityRepository;
    }

    public UniversityDto handle(GetUniversityQuery query) {
        return universityRepository.findById(query.id())
                .map(IamDtoAssembler::toDto)
                .orElseThrow(() -> new UniversityNotFoundException(query.id()));
    }
}
