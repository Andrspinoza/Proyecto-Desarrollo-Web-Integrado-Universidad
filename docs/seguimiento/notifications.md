# Seguimiento — notifications (André)

Solo André edita este archivo, en el último commit de cada PR.

| Bloque | Estado | PR | Notas |
|---|---|---|---|
| Dominio y aplicación | ✅ | #44 | `MatchNotification`; `ConnectionAcceptedEventHandler` avisa a los dos estudiantes; el fallo de uno no impide al otro |
| Entrega por WebSocket | ✅ | #44 | `/user/queue/matches`; `ProfilesContextFacade.firstNameOfStudent` para el nombre; el pop-up lo muestra la web app |

**Siguiente paso:** persistir los avisos (`GET /notifications` y marcar leídos) para quien estaba desconectado; hoy solo lo ve quien tiene el WebSocket abierto. Probar con dos cuentas reales en staging.

**Última actualización:** 2026-11-19 · André
