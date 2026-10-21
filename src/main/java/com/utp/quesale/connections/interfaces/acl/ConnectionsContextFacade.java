package com.utp.quesale.connections.interfaces.acl;

public interface ConnectionsContextFacade {
    boolean areConnected(Long studentId, Long otherStudentId);
}
