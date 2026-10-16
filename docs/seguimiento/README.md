# Seguimiento de avance

Panel general del proyecto. **Todo agente lo lee antes de empezar**, junto con el archivo de su módulo.
Se actualiza en cada fusión a `main`. Última actualización: 2026-10-16.

**Estados:** ✅ en `main` · 🔨 en curso o en PR abierto · ⏳ pendiente.

## Piezas de la ruta crítica

| # | Pieza | Dueño | Estado | Desbloquea a |
|---|---|---|---|---|
| B0 | Andamiaje y `shared` | André | ✅ | Todos |
| B1 | `iam`: `University` completo | André | ✅ | Modelo para todos |
| B2 | `StudentJpaEntity` (tabla `students` con FK) | Fernando | ✅ | La persistencia de todos |
| B5 | Capa de aplicación de `iam` (CQRS) y clase base pura en `shared` | André | 🔨 | La capa de aplicación de todos |
| B6 | `Message` mínimo con `MessageSentEvent` | Mafer | ⏳ | El WebSocket |
| B7 | WebSocket: configuración STOMP, manejador del evento y `/chat-test.html` | André | ⏳ | Chat en tiempo real |

## Qué hace cada integrante ahora

| Integrante | Ahora | Después |
|---|---|---|
| **André** | B5; luego `Account` y B7 | `subscriptions`, revisar PR |
| **Fernando** | Repositorio de dominio, mapper, adapter y test H2 de `Student` | Perfil, intereses y habilidades; capa de aplicación (tras B5) |
| **Ubes** | Dominio de `connections` en su rama aparte; persistencia de `moderation` (B2 ya está) | Persistencia de `connections`; capa de aplicación (tras B5) |
| **Mafer** | Cerrar el PR #4 de `communities`; persistencia de `communities` (B2 ya está) y controllers | B6 (`Message` mínimo); capa de aplicación (tras B5) |
| **Jesús** | Dominio de `meetups` con tests, ya | Persistencia con bloqueo pesimista (B2 ya está) |

## Capa de aplicación: espera a B5

El profesor enseñó el 15-oct la capa de aplicación con **CQRS**: `application/commands`, `application/queries`,
`application/handlers/commands` y `handlers/queries`, `application/dtos` y `application/assemblers`.
**No la escribas hasta que B5 esté en `main`**: `iam` será el modelo y evita que cada módulo la invente distinta.
Mientras tanto, termina dominio, persistencia con test H2 y, si te toca, resources.

## Reglas de este panel

- Solo André edita este archivo. Cada dueño edita únicamente `docs/seguimiento/<modulo>.md`.
- El dueño de un módulo actualiza su archivo en el último commit de cada PR.
- Un agente sin `gh` sabe qué hay hecho con este panel, el archivo del módulo y `git log origin/main`.
