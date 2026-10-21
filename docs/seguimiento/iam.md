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
| B7 parte 1: WebSocket del chat | ✅ | #25 | Starter, configuración STOMP en `/ws`, identidad por `studentId` (D-16), pruebas y página `chat-test.html` |
| B7 parte 2: entrega de `MessageSentEvent` | 🔨 | #27 | `ChatMessageDto` y `MessageSentEventHandler` en `messaging`, con su prueba; entrega el mensaje al destinatario por `/user/queue/messages` |

**Siguiente paso:** fusionar el #27 y probar el chat con la API corriendo. El envío por REST (`POST /api/v1/messages`) llega con `messaging` de Mafer. `Account` (cuenta con contraseña cifrada) queda para después si hay tiempo

**Última actualización:** 2026-10-21 · André
