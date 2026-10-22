package com.utp.quesale.messaging.domain.repositories;

import com.utp.quesale.messaging.domain.aggregates.Message;

import java.util.List;
import java.util.Optional;

public interface MessageRepository {
    Message save(Message message);

    Optional<Message> findById(Long id);

    List<Message> findConversation(Long studentId, Long otherStudentId);
}
