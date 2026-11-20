package com.utp.quesale.connections.interfaces.acl;

import java.util.Set;

public interface ConnectionsContextFacade {
    boolean areConnected(Long studentId, Long otherStudentId);

    default Set<Long> relatedStudentIds(Long studentId) {
        return Set.of();
    }
}
