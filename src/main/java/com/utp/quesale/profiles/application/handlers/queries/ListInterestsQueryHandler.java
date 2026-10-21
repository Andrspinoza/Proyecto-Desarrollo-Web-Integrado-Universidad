package com.utp.quesale.profiles.application.handlers.queries;

import com.utp.quesale.profiles.application.assemblers.ProfilesDtoAssembler;
import com.utp.quesale.profiles.application.dtos.InterestDto;
import com.utp.quesale.profiles.application.queries.ListInterestsQuery;
import com.utp.quesale.profiles.domain.repositories.InterestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListInterestsQueryHandler {
    private final InterestRepository interestRepository;

    public ListInterestsQueryHandler(InterestRepository interestRepository) {
        this.interestRepository = interestRepository;
    }

    public List<InterestDto> handle(ListInterestsQuery query) {
        return interestRepository.findAll().stream().map(ProfilesDtoAssembler::toDto).toList();
    }
}
