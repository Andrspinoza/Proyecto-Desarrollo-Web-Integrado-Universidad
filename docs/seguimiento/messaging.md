# Seguimiento — messaging (Mafer)

Solo Mafer edita este archivo, en el último commit de cada PR.

| Bloque | Estado | PR | Notas |
|---|---|---|---|
| Dominio y tests | ✅ | #23 | B6: `Message` mínimo y `MessageSentEvent`; faltan `Notification` y `markAsRead` |
| Contrato de repositorio | ✅ | #38 | `MessageRepository` con `save`, `findById` y `findConversation` |
| Persistencia y test H2 | ✅ | #38 | `MessageJpaEntity` con FKs a `students`, adapter, mapper y 6 pruebas H2 |
| Capa de aplicación | 🔨 | #39 | Enviar mensaje (solo conexiones sin bloqueo, con las fachadas de `connections` y `moderation`) y ver la conversación; 6 pruebas |
| Resources y endpoints | 🔨 | #39 | `MessagesController` con `POST /api/v1/messages` y `GET /api/v1/messages/conversations/{id}`; 8 pruebas con MockMvc |

**Siguiente paso:** fusionar el PR #39; después `markAsRead` y `Notification` si sobra tiempo

**Última actualización:** 2026-10-22 · Mafer
