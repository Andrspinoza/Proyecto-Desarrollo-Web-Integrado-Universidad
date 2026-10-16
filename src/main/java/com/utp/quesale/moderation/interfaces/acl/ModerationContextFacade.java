package com.utp.quesale.moderation.interfaces.acl;

public interface ModerationContextFacade {
    boolean isBlocked(Long studentId, Long otherStudentId);
}
