package com.utp.quesale.profiles.application.handlers.queries;

import com.utp.quesale.profiles.application.assemblers.ProfilesDtoAssembler;
import com.utp.quesale.profiles.application.dtos.SkillDto;
import com.utp.quesale.profiles.application.queries.ListSkillsQuery;
import com.utp.quesale.profiles.domain.repositories.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListSkillsQueryHandler {
    private final SkillRepository skillRepository;

    public ListSkillsQueryHandler(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    public List<SkillDto> handle(ListSkillsQuery query) {
        return skillRepository.findAll().stream().map(ProfilesDtoAssembler::toDto).toList();
    }
}
