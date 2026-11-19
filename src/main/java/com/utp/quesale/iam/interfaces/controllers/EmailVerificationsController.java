package com.utp.quesale.iam.interfaces.controllers;

import com.utp.quesale.iam.application.commands.ConfirmEmailVerificationCommand;
import com.utp.quesale.iam.application.commands.ResendEmailVerificationCommand;
import com.utp.quesale.iam.application.handlers.commands.EmailVerificationCommandHandler;
import com.utp.quesale.iam.application.handlers.queries.GetEmailVerificationStatusQueryHandler;
import com.utp.quesale.iam.application.queries.GetEmailVerificationStatusQuery;
import com.utp.quesale.iam.interfaces.assemblers.EmailVerificationResourceAssembler;
import com.utp.quesale.iam.interfaces.resources.EmailVerificationStatusResource;
import com.utp.quesale.shared.interfaces.rest.RequestHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/email-verifications")
@Tag(name = "Email verifications", description = "Verificación del correo institucional al registrarse")
public class EmailVerificationsController {
    private final EmailVerificationCommandHandler commandHandler;
    private final GetEmailVerificationStatusQueryHandler statusHandler;

    public EmailVerificationsController(EmailVerificationCommandHandler commandHandler,
                                        GetEmailVerificationStatusQueryHandler statusHandler) {
        this.commandHandler = commandHandler;
        this.statusHandler = statusHandler;
    }

    @GetMapping("/confirm")
    @Operation(summary = "Confirmar el correo con el token recibido por email")
    public EmailVerificationStatusResource confirm(@RequestParam String token) {
        return EmailVerificationResourceAssembler.toResourceFromDto(
                commandHandler.handle(new ConfirmEmailVerificationCommand(token)));
    }

    @PostMapping("/resend")
    @Operation(summary = "Reenviar el correo de verificación")
    public ResponseEntity<Void> resend(@RequestHeader(RequestHeaders.STUDENT_ID) Long studentId) {
        commandHandler.handle(new ResendEmailVerificationCommand(studentId));
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/status")
    @Operation(summary = "Consultar si el correo del estudiante ya fue verificado")
    public EmailVerificationStatusResource status(@RequestHeader(RequestHeaders.STUDENT_ID) Long studentId) {
        return EmailVerificationResourceAssembler.toResourceFromDto(
                statusHandler.handle(new GetEmailVerificationStatusQuery(studentId)));
    }
}
