package com.utp.quesale.messaging.application.assemblers;

import com.utp.quesale.messaging.application.dtos.MessageDto;
import com.utp.quesale.messaging.domain.aggregates.Message;

public final class MessagingDtoAssembler {

    private MessagingDtoAssembler() {
    }

    public static MessageDto toDto(Message message) {
        return new MessageDto(
                message.getId(),
                message.getSenderId(),
                message.getRecipientId(),
                message.getContent(),
                message.getSentAt()
        );
    }
}
