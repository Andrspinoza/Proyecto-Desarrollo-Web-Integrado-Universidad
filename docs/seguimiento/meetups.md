# Seguimiento — meetups (Jesús)

Solo Jesús edita este archivo. Actualizarlo al cerrar cada bloque y antes de abrir su PR.

| Bloque | Estado | PR / commit | Notas |
|---|---|---|---|
| Dominio y tests | ✅ | PR #8, fusionado | Agregado `Meetup`, participaciones, visibilidad, invitaciones, eventos y excepciones. |
| Persistencia y test H2 | ✅ | PR #8, fusionado | Entidades JPA, bloqueo pesimista, mapper, adapter y prueba de actualización que conserva los IDs de participaciones. |
| Capa de aplicación | 🔨 | Commit local `5302237`; PR pendiente | Commands, queries, DTOs y handlers para crear, listar, consultar y gestionar meetups; pruebas de aplicación pasan. |
| Resources y endpoints | ⏳ | Siguiente bloque, en PR separado | Se presenta después de fusionar aplicación y traer `main`; incluirá los resources, endpoints y la colección Postman. |

**Siguiente paso:** abrir primero el PR de aplicación como bloque independiente. Después de fusionarlo, traer `main` y abrir el PR de resources/endpoints con la colección Postman. Antes de cada PR, actualizarse con `main`, usar un título descriptivo y completar la plantilla del repositorio. Meetups no define una fachada propia: el alta y el filtro público usan `ProfilesContextFacade`. La interfaz de profiles existe, pero todavía no se encontró una implementación Spring registrada, así que falta verificar las rutas en Swagger cuando profiles la entregue.

**Última actualización:** 2026-10-19 · Jesús
