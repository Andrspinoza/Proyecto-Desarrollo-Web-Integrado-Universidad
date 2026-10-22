package com.utp.quesale.messaging.interfaces.controllers;

import com.utp.quesale.messaging.application.handlers.commands.MessageCommandHandler;
import com.utp.quesale.messaging.application.handlers.queries.GetConversationQueryHandler;
import com.utp.quesale.messaging.interfaces.assemblers.MessageResourceAssembler;
import com.utp.quesale.messaging.interfaces.resources.MessageResource;
import com.utp.quesale.messaging.interfaces.resources.SendMessageResource;
import com.utp.quesale.shared.interfaces.rest.RequestHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@Tag(name = "Messages", description = "Mensajes entre estudiantes conectados")
public class MessagesController {
    private final MessageCommandHandler commandHandler;
    private final GetConversationQueryHandler conversationHandler;

    public MessagesController(MessageCommandHandler commandHandler, GetConversationQueryHandler conversationHandler) {
        this.commandHandler = commandHandler;
        this.conversationHandler = conversationHandler;
    }

    @PostMapping
    @Operation(summary = "Enviar un mensaje a una conexión; el destinatario lo recibe también por WebSocket")
    public ResponseEntity<MessageResource> send(
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId,
            @Valid @RequestBody SendMessageResource resource) {
        var dto = commandHandler.handle(MessageResourceAssembler.toSendCommand(studentId, resource));
        return ResponseEntity.created(URI.create("/api/v1/messages/conversations/" + dto.recipientId()))
                .body(MessageResourceAssembler.toResourceFromDto(dto));
    }

    @GetMapping("/conversations/{otherStudentId}")
    @Operation(summary = "Ver la conversación con otro estudiante, del mensaje más antiguo al más reciente")
    public List<MessageResource> findConversation(
            @RequestHeader(RequestHeaders.STUDENT_ID) Long studentId,
            @PathVariable Long otherStudentId) {
        return conversationHandler.handle(MessageResourceAssembler.toConversationQuery(studentId, otherStudentId))
                .stream()
                .map(MessageResourceAssembler::toResourceFromDto)
                .toList();
    }
}
