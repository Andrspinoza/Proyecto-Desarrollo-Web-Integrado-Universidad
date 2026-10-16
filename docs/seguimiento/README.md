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

## Qué falta para la entrega del 22-oct

Cada agregado avanza por niveles: dominio con tests → persistencia con test H2 → crear, obtener y
listar → una o dos acciones clave. Endpoints simples, sin sobreingeniería. Swagger es requisito.

| Integrante | Módulo | Ya está en `main` | Falta para el 22-oct |
|---|---|---|---|
| **André** | `iam`, `shared` | `University` completo; base de `shared` | **B5**; `Account` con BCrypt; clase base pura y despachador de eventos; **B7** (WebSocket); pruebas en Swagger; tag `v0.2.0` |
| **Fernando** | `profiles` | `Student`, `FullName`, `StudentRecordId`, `StudyCycle`, `StudentJpaEntity` | `StudentRepository`, mapper, adapter y test H2; `Profile` (carrera, ciclo, descripción, visibilidad), `Interest` y `Skill` con su persistencia; endpoints de estudiante; **EER Diagram y diccionario de datos** |
| **Ubes** | `connections`, `moderation` | Dominio, repositorios, resources y colección Postman de `moderation` | `connections`: dominio, repositorio, resources y Postman en su PR; persistencia con test H2 de ambos; endpoints de crear, listar y aceptar conexión, y de reportar y bloquear; `00-setup` de Postman |
| **Mafer** | `communities`, `messaging` | Dominio completo de `communities` (con `promote`), contratos y resources | Persistencia con test H2 (comunidades, miembros, publicaciones); endpoints de crear, listar, unirse y publicar; **B6** (`Message` mínimo y `MessageSentEvent`) |
| **Jesús** | `meetups` | Nada todavía | Todo: dominio con tests (cupo, organizador, cancelación; eventos con `meetupId`), persistencia con bloqueo pesimista y test H2, endpoints de crear, listar, solicitar y confirmar participación, y cancelar |
| **Todos** | Capa de aplicación | — | Cuando B5 esté en `main`: commands, queries, handlers, DTOs y assembler de su módulo, copiando `iam`; el controller pasa a usar los handlers |

## Después de la entrega

Fachadas entre contextos y reglas que cruzan módulos (no escribir a un bloqueado, límites del plan),
`subscriptions`, notificaciones en tiempo real, `markAsRead`, JWT, búsqueda con filtros (JPQL) y el
frontend Angular en su propio repositorio.

## Capa de aplicación: espera a B5

El profesor enseñó el 15-oct la capa de aplicación con **CQRS**: `application/commands`, `application/queries`,
`application/handlers/commands` y `handlers/queries`, `application/dtos` y `application/assemblers`.
**No la escribas hasta que B5 esté en `main`**: `iam` será el modelo y evita que cada módulo la invente distinta.
Mientras tanto, termina dominio, persistencia con test H2 y, si te toca, resources.

## Reglas de este panel

- Solo André edita este archivo. Cada dueño edita únicamente `docs/seguimiento/<modulo>.md`.
- **Al terminar cada PR**, el dueño actualiza su archivo de módulo en el último commit (bloque, estado y número de PR); si el trabajo se reparte en varios PR, lo hace en cada uno.
- Un agente sin `gh` sabe qué hay hecho con este panel, el archivo del módulo y `git log origin/main`.
