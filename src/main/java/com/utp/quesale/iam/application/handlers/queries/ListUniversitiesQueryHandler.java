package com.utp.quesale.iam.application.handlers.queries;

import com.utp.quesale.iam.application.assemblers.IamDtoAssembler;
import com.utp.quesale.iam.application.dtos.UniversityDto;
import com.utp.quesale.iam.application.queries.ListUniversitiesQuery;
import com.utp.quesale.iam.domain.repositories.UniversityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListUniversitiesQueryHandler {

    private final UniversityRepository universityRepository;

    public ListUniversitiesQueryHandler(UniversityRepository universityRepository) {
        this.universityRepository = universityRepository;
    }

    public List<UniversityDto> handle(ListUniversitiesQuery query) {
        return universityRepository.findAll().stream().map(IamDtoAssembler::toDto).toList();
    }
}
