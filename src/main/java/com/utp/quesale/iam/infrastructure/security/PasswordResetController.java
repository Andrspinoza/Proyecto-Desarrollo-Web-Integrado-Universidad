package com.utp.quesale.iam.infrastructure.security;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Registro, inicio de sesión y restablecimiento de contraseña")
@RestController
@RequestMapping("/api/v1/auth")
public class PasswordResetController {
    private final PasswordResetService service;

    public PasswordResetController(PasswordResetService service) {
        this.service = service;
    }

    public record ForgotPassword(@NotBlank @Email @Size(max = 120) String email) {}

    public record ResetPassword(@NotBlank @Size(max = 200) String token,
                                @NotBlank @Size(min = 8, max = 72) String password) {}

    @Operation(summary = "Pedir el enlace para restablecer la contraseña",
            description = "Responde 202 exista o no la cuenta, para no revelar qué correos están registrados. El enlace vence en 1 hora y se envía al correo institucional.")
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void forgot(@Valid @RequestBody ForgotPassword body) {
        service.request(body.email());
    }

    @Operation(summary = "Elegir una contraseña nueva con el token del correo",
            description = "El token es de un solo uso. Con token inválido o vencido responde 400.")
    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reset(@Valid @RequestBody ResetPassword body) {
        service.reset(body.token(), body.password());
    }
}
