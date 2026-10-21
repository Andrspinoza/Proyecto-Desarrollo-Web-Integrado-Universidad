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
| B7 parte 1: WebSocket del chat | 🔨 | #25 | Starter, configuración STOMP en `/ws`, identidad por `studentId` (D-16), pruebas y página `chat-test.html` |
| B7 parte 2: entrega de `MessageSentEvent` | ⏳ | | Manejador en `messaging` con prueba; va en un PR aparte tras fusionar el #25 |

**Siguiente paso:** fusionar el #25; luego abrir el PR del manejador de `MessageSentEvent` (B7 parte 2). `Account` (cuenta con contraseña cifrada) queda para después si hay tiempo

**Última actualización:** 2026-10-21 · André
