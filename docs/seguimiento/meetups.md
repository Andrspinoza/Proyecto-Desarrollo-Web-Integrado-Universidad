# Seguimiento — meetups (Jesús)

Solo Jesús edita este archivo. Actualizarlo al cerrar cada bloque y antes de abrir su PR.

| Bloque | Estado | PR / commit | Notas |
|---|---|---|---|
| Dominio y tests | ✅ | PR #8, fusionado | Agregado `Meetup`, participaciones, visibilidad, invitaciones, eventos y excepciones. |
| Persistencia y test H2 | ✅ | PR #8, fusionado | Entidades JPA, bloqueo pesimista, mapper, adapter y prueba de actualización que conserva los IDs de participaciones. |
| Capa de aplicación | ✅ | PR #20, fusionado | Commands, queries, DTOs y handlers para crear, listar, consultar y gestionar meetups; pruebas de aplicación pasan. |
| Resources y endpoints | 🔍 en revisión | PR #32 | Resources, endpoints y colección Postman; incluye la validación de universidad en solicitud e invitaciones. |

**Siguiente paso:** esperar la revisión y fusión del PR #32; después, traer `main` y revisar el panel para confirmar si queda otro bloque de meetups. El filtro público y la validación de universidad usan `ProfilesContextFacade`.

**Última actualización:** 2026-10-21 · Jesús
