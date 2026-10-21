package com.utp.quesale.moderation.application.handlers.queries;

import com.utp.quesale.moderation.application.assemblers.ModerationDtoAssembler;
import com.utp.quesale.moderation.application.dtos.BlockDto;
import com.utp.quesale.moderation.application.queries.ListBlocksQuery;
import com.utp.quesale.moderation.domain.repositories.BlockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListBlocksQueryHandler {

    private final BlockRepository blockRepository;

    public ListBlocksQueryHandler(BlockRepository blockRepository) {
        this.blockRepository = blockRepository;
    }

    public List<BlockDto> handle(ListBlocksQuery query) {
        return blockRepository.findAllByBlockerId(query.blockerId()).stream()
                .map(ModerationDtoAssembler::toDto)
                .toList();
    }
}
