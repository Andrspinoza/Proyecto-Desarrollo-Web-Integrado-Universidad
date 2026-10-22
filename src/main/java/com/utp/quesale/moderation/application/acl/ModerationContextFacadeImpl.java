package com.utp.quesale.moderation.application.acl;

import com.utp.quesale.moderation.domain.repositories.BlockRepository;
import com.utp.quesale.moderation.interfaces.acl.ModerationContextFacade;
import org.springframework.stereotype.Service;

@Service
public class ModerationContextFacadeImpl implements ModerationContextFacade {

    private final BlockRepository blockRepository;

    public ModerationContextFacadeImpl(BlockRepository blockRepository) {
        this.blockRepository = blockRepository;
    }

    @Override
    public boolean isBlocked(Long studentId, Long otherStudentId) {
        if (studentId == null || otherStudentId == null || studentId <= 0 || otherStudentId <= 0) {
            return false;
        }
        return blockRepository.existsBetween(studentId, otherStudentId);
    }
}
