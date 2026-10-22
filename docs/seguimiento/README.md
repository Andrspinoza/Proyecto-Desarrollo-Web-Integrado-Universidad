# Seguimiento de avance

Panel general del proyecto. **Todo agente lo lee antes de empezar**, junto con el archivo de su módulo.
Se actualiza en cada fusión a `main`. Última actualización: 2026-10-22.

**Estados:** ✅ en `main` · 🔨 en curso o en PR abierto · ⏳ pendiente.

## Piezas de la ruta crítica

| # | Pieza | Dueño | Estado | Desbloquea a |
|---|---|---|---|---|
| B0 | Andamiaje y `shared` | André | ✅ | Todos |
| B1 | `iam`: `University` completo | André | ✅ | Modelo para todos |
| B2 | `StudentJpaEntity` (tabla `students` con FK) | Fernando | ✅ | La persistencia de todos |
| B3 | Interfaces de las fachadas | Cada dueño | ✅ `profiles`, `connections`, `moderation` e `iam` con interfaz e implementación; `subscriptions` queda para después de la entrega | Quien las consume |
| B4 | Eventos de dominio de `connections` y `meetups` | Ubes, Jesús | ✅ | `messaging` |
| B5 | Capa de aplicación de `iam` (CQRS) y clase base pura en `shared` | André | ✅ | La capa de aplicación de todos |
| B6 | `Message` mínimo con `MessageSentEvent` | Mafer | ✅ | El WebSocket |
| B7 | WebSocket: configuración STOMP, manejador del evento y `/chat-test.html` | André | ✅ | Chat en tiempo real |

## Qué hay en `main` (22-oct)

Endpoints vivos: `universities`, `students`, `interests`, `skills`, `connections`, `blocks`, `reports`, `communities` y
`meetups` (con participaciones e invitaciones). Además, el WebSocket `/ws` y la página `/chat-test.html`. La colección
Postman `docs/api/00-setup.postman_collection.json` prepara los datos de prueba (se corre **una sola vez con la base
vacía**). Modelos para copiar: `iam`, `moderation` y `connections` (persistencia, aplicación, controllers y pruebas HTTP).

| Módulo | Dueño | Dominio | Persistencia | Aplicación | Endpoints |
|---|---|---|---|---|---|
| `iam` | André | ✅ | ✅ | ✅ | ✅ |
| `profiles` | Fernando | ✅ | ✅ | 🔨 Catálogos y Student ✅; falta acciones de Profile | 🔨 `students`, `interests`, `skills` ✅; falta Profile |
| `meetups` | Jesús | ✅ | ✅ | ✅ | ✅ |
| `communities` | Mafer | ✅ | ✅ | ✅ | ✅ |
| `messaging` | Mafer | 🔨 `Message` mínimo (B6) | ✅ | 🔨 Manejador del evento ✅; falta enviar y consultar | ⏳ |
| `connections` | Ubes | ✅ | ✅ | ✅ | ✅ |
| `moderation` | Ubes | ✅ | ✅ | ✅ | ✅ |
| `subscriptions` | André | ⏳ | ⏳ | ⏳ | ⏳ Después de la entrega |

## Qué falta para la entrega del 22-oct (4 p. m.)

Prioridad: lo que se **muestra en Swagger y en la presentación**. Sin sobreingeniería.

| Integrante | Módulo | Falta |
|---|---|---|
| **André** | `iam`, `shared` | Prueba con MySQL real; tag `v0.2.0`. `Account` y `subscriptions` quedan fuera de esta entrega |
| **Fernando** | `profiles` | **EER Diagram y diccionario de datos** (ya existen todas las tablas); acciones de perfil solo si sobra tiempo |
| **Jesús** | `meetups` | Nada obligatorio: ensayar la presentación de su módulo |
| **Mafer** | `messaging` | Capa de aplicación (enviar mensaje y ver la conversación, con las fachadas de `connections` y `moderation`, ya en `main`) y `POST /api/v1/messages` + `GET /api/v1/messages/conversations/{id}`; además el guion de exposición |
| **Ubes** | `connections`, `moderation` | Nada obligatorio: casos de prueba y ensayo de la presentación |

Si el tiempo aprieta, **se recorta el final de la lista** (acciones de perfil, afinidad, `Notification`, endpoints de
`messaging`), nunca la persistencia de lo que ya existe.

## Quién depende de quién

- El **EER Diagram** (Fernando) ya no espera a nadie: están todas las tablas, incluida `messages`.
- El **chat de punta a punta** depende solo de Mafer (aplicación y controller de `messaging`); las fachadas que usa ya
  están en `main`.
- El tag `v0.2.0` espera la prueba con MySQL y el EER.
- Jesús, Mafer y Ubes no se bloquean entre sí.

## Después de la entrega

Fachadas entre contextos y reglas que cruzan módulos (no escribir a un bloqueado, límites del plan),
`subscriptions`, notificaciones en tiempo real, `markAsRead`, JWT, búsqueda con filtros (JPQL) y el
frontend Angular en su propio repositorio.

## Capa de aplicación: B5 ya es el modelo

El profesor enseñó el 15-oct la capa de aplicación con **CQRS**. B5 la deja hecha en `iam`, y **tu módulo la copia**
cuando termine su persistencia. Mira `src/main/java/com/utp/quesale/iam/application/`:

| Pieza | Dónde está en `iam` | Qué copias |
|---|---|---|
| Command y queries | `application/commands`, `application/queries` | Un `record` por operación |
| DTO y assembler | `application/dtos`, `application/assemblers/IamDtoAssembler` | El resultado y su conversión desde el agregado |
| Handlers | `application/handlers/commands`, `handlers/queries` | Uno de commands por agregado y uno por cada query |
| Controller | `interfaces/controllers/UniversitiesController` | Llama a los handlers; el assembler de `interfaces` arma el command y el resource desde el DTO |
| Pruebas | `src/test/.../iam/application` y `iam/interfaces` | Repositorio en memoria para los handlers y una prueba HTTP del controller sin base de datos |

El handler de commands lleva `@Transactional` y despacha el agregado con `DomainEventDispatcher` después de guardar.
Si tu módulo todavía no terminó dominio y persistencia con test H2, termínalos primero.

## Reglas de este panel

- Solo André edita este archivo. Cada dueño edita únicamente `docs/seguimiento/<modulo>.md`.
- **Al terminar cada PR**, el dueño actualiza su archivo de módulo en el último commit (bloque, estado y número de PR); si el trabajo se reparte en varios PR, lo hace en cada uno.
- Un agente sin `gh` sabe qué hay hecho con este panel, el archivo del módulo y `git log origin/main`.
