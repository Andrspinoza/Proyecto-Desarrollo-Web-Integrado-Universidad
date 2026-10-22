package com.utp.quesale.messaging.interfaces.assemblers;

import com.utp.quesale.messaging.application.commands.SendMessageCommand;
import com.utp.quesale.messaging.application.dtos.MessageDto;
import com.utp.quesale.messaging.application.queries.GetConversationQuery;
import com.utp.quesale.messaging.interfaces.resources.MessageResource;
import com.utp.quesale.messaging.interfaces.resources.SendMessageResource;

public class MessageResourceAssembler {

    public static SendMessageCommand toSendCommand(Long senderId, SendMessageResource resource) {
        return new SendMessageCommand(senderId, resource.recipientId(), resource.content());
    }

    public static GetConversationQuery toConversationQuery(Long requesterId, Long otherStudentId) {
        return new GetConversationQuery(requesterId, otherStudentId);
    }

    public static MessageResource toResourceFromDto(MessageDto dto) {
        return new MessageResource(dto.id(), dto.senderId(), dto.recipientId(), dto.content(), dto.sentAt());
    }
}
