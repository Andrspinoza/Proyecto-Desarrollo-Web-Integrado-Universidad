# Seguimiento — iam (André)

Solo André edita este archivo, en el último commit de cada PR.

| Bloque | Estado | PR | Notas |
|---|---|---|---|
| Dominio y tests | ✅ | #3 | University y EmailDomain |
| Contrato de repositorio | ✅ | #3 | |
| Persistencia y test H2 | ✅ | #3 | |
| Capa de aplicación (B5) | ✅ | | Commands, queries, DTO, assembler y handlers de University; modelo para los demás módulos |
| Resources y endpoints | ✅ | #3 | El controller usa los handlers de la capa de aplicación; prueba HTTP incluida |
| Base de `shared` para la capa de aplicación | ✅ | #11 | Raíz de agregado pura y `DomainEventDispatcher` |
| B7 parte 1: WebSocket del chat | ✅ | #25 | Starter, configuración STOMP en `/ws`, identidad por `studentId` (D-16), y pruebas |
| B7 parte 2: entrega de `MessageSentEvent` | ✅ | #27 | `ChatMessageDto` y `MessageSentEventHandler` en `messaging`, con su prueba; entrega el mensaje al destinatario por `/user/queue/messages` |
| Verificación de correo: dominio | 🔨 | #41 | `EmailVerification` y `VerificationToken` (solo se guarda el hash; vence a las 24 h) |
| Verificación de correo: aplicación | 🔨 | #41 | Emitir, confirmar y reenviar (espera de 60 s); se emite al recibir `StudentRegisteredEvent`; fachada con `isEmailVerified` |
| Verificación de correo: persistencia y SMTP | 🔨 | #41 | Tabla `email_verifications` con FK a `students`; envío por SMTP o, sin SMTP, el enlace va al log |
| Verificación de correo: endpoints y filtro | 🔨 | #41 | `confirm`, `resend` y `status`; con `EMAIL_VERIFICATION_REQUIRED=true` responde 403 a quien no verificó |
| Autenticación JWT | ✅ | #42 | Registro con BCrypt, `login`, filtro HTTP y autorización STOMP (D-22) |
| Límite de intentos de autenticación | ✅ | #45 | 30 solicitudes cada 5 min por dirección y 8 fallos por correo en 15 min (429) |
| Olvidé mi contraseña | ✅ | #50 | `POST /auth/forgot-password` (202 exista o no la cuenta) y `POST /auth/reset-password`; token de un solo uso, solo se guarda su hash, vence en 1 h; el enlace apunta a `/restablecer` de la web app |
| Catálogo de universidades | ✅ | #49 | UTP, UPC, UTEC, Ulima, USIL, UPN y PUCP, un dominio de correo por universidad; sembrado con `SEED_UNIVERSITIES=true` |

**Siguiente paso:** pruebas de punta a punta del registro con correo real (Brevo) y límites con varias réplicas si se escala la API (hoy el contador vive en memoria)

**Última actualización:** 2026-11-19 · André
