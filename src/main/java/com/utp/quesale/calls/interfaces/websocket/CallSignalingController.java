package com.utp.quesale.calls.interfaces.websocket;

import com.utp.quesale.calls.application.commands.RelayCallSignalCommand;
import com.utp.quesale.calls.application.handlers.commands.CallSignalCommandHandler;
import com.utp.quesale.calls.interfaces.resources.CallErrorResource;
import com.utp.quesale.calls.interfaces.resources.CallSignalResource;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class CallSignalingController {

    private final CallSignalCommandHandler handler;

    public CallSignalingController(CallSignalCommandHandler handler) {
        this.handler = handler;
    }

    @MessageMapping("/calls/signal")
    public void signal(@Payload CallSignalResource resource, Principal principal) {
        if (principal == null) throw new IllegalArgumentException("Student identity is required to signal a call");
        handler.handle(new RelayCallSignalCommand(Long.valueOf(principal.getName()), resource.toStudentId(),
                resource.type(), resource.payload()));
    }

    @MessageExceptionHandler
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public CallErrorResource handleError(Exception ex) {
        return new CallErrorResource(ex.getClass().getSimpleName(), ex.getMessage());
    }
}
