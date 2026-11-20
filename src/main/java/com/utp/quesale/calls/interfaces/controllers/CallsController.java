package com.utp.quesale.calls.interfaces.controllers;

import com.utp.quesale.calls.application.dtos.IceServersDto;
import com.utp.quesale.calls.application.handlers.queries.GetIceServersQueryHandler;
import com.utp.quesale.calls.application.queries.GetIceServersQuery;
import com.utp.quesale.shared.interfaces.rest.RequestHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/calls")
@Tag(name = "Calls", description = "Llamadas de voz y video entre estudiantes conectados (WebRTC)")
public class CallsController {
    private final GetIceServersQueryHandler iceServersHandler;

    public CallsController(GetIceServersQueryHandler iceServersHandler) {
        this.iceServersHandler = iceServersHandler;
    }

    @GetMapping("/ice-servers")
    @Operation(summary = "Servidores STUN y TURN para iniciar una llamada")
    public IceServersDto iceServers(@RequestHeader(RequestHeaders.STUDENT_ID) Long studentId) {
        return iceServersHandler.handle(new GetIceServersQuery(studentId));
    }
}
