# Seguimiento — calls (André)

Solo André edita este archivo, en el último commit de cada PR.

| Bloque | Estado | PR | Notas |
|---|---|---|---|
| Dominio y tests | ✅ | #44 | `CallSignal` y sus reglas; excepciones por conexión faltante, bloqueo y correo sin verificar |
| Capa de aplicación | ✅ | #44 | Handler de señales y de servidores ICE (credenciales TURN temporales HMAC-SHA1) |
| Infraestructura e interfaces | ✅ | #44 | STOMP `/app/calls/signal` → `/user/queue/calls`, errores a `/user/queue/errors`; `GET /api/v1/calls/ice-servers`; página `call-test.html` con inicio de sesión JWT |

**Siguiente paso:** probar una llamada real entre dos navegadores con coturn (TURN) detrás de HTTPS. No probado: audio y video de punta a punta ni TURN real.

**Última actualización:** 2026-11-19 · André
