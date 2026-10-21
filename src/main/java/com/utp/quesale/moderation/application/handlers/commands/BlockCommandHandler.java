package com.utp.quesale.moderation.application.handlers.commands;

import com.utp.quesale.moderation.application.assemblers.ModerationDtoAssembler;
import com.utp.quesale.moderation.application.commands.CreateBlockCommand;
import com.utp.quesale.moderation.application.commands.DeleteBlockCommand;
import com.utp.quesale.moderation.application.dtos.BlockDto;
import com.utp.quesale.moderation.domain.aggregates.Block;
import com.utp.quesale.moderation.domain.exceptions.BlockNotFoundException;
import com.utp.quesale.moderation.domain.repositories.BlockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BlockCommandHandler {

    private final BlockRepository blockRepository;

    public BlockCommandHandler(BlockRepository blockRepository) {
        this.blockRepository = blockRepository;
    }

    @Transactional
    public BlockDto handle(CreateBlockCommand command) {
        if (blockRepository.findByBlockerIdAndBlockedId(command.blockerId(), command.blockedId()).isPresent()) {
            throw new IllegalStateException("Student is already blocked");
        }
        return ModerationDtoAssembler.toDto(
                blockRepository.save(new Block(command.blockerId(), command.blockedId())));
    }

    @Transactional
    public void handle(DeleteBlockCommand command) {
        if (!blockRepository.deleteByBlockerIdAndBlockedId(command.blockerId(), command.blockedId())) {
            throw new BlockNotFoundException(command.blockerId(), command.blockedId());
        }
    }
}
