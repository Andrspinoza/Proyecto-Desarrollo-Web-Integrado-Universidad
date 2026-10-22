# Seguimiento — messaging (Mafer)

Solo Mafer edita este archivo, en el último commit de cada PR.

| Bloque | Estado | PR | Notas |
|---|---|---|---|
| Dominio y tests | ✅ | #23 | B6: `Message` mínimo y `MessageSentEvent`; faltan `Notification` y `markAsRead` |
| Contrato de repositorio | 🔨 | #38 | `MessageRepository` con `save`, `findById` y `findConversation` |
| Persistencia y test H2 | 🔨 | #38 | `MessageJpaEntity` con FKs a `students`, adapter, mapper y 6 pruebas H2 |
| Capa de aplicación | ⏳ | | Espera las fachadas de Ubes y B4 |
| Resources y endpoints | ⏳ | | |

**Siguiente paso:** fusionar el PR #38; la capa de aplicación espera la fachada de conexiones de Ubes

**Última actualización:** 2026-10-22 · Mafer
