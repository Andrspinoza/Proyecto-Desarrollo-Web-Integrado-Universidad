package com.utp.quesale.messaging.application.handlers.queries;

import com.utp.quesale.messaging.application.assemblers.MessagingDtoAssembler;
import com.utp.quesale.messaging.application.dtos.MessageDto;
import com.utp.quesale.messaging.application.queries.GetConversationQuery;
import com.utp.quesale.messaging.domain.repositories.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetConversationQueryHandler {

    private final MessageRepository messageRepository;

    public GetConversationQueryHandler(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    // Historial entre dos estudiantes, del mensaje más antiguo al más reciente
    public List<MessageDto> handle(GetConversationQuery query) {
        return messageRepository.findConversation(query.requesterId(), query.otherStudentId()).stream()
                .map(MessagingDtoAssembler::toDto)
                .toList();
    }
}
