# Seguimiento de avance

Panel general del proyecto. **Todo agente lo lee antes de empezar**, junto con el archivo de su módulo.
Se actualiza en cada fusión a `main`. Última actualización: 2026-10-21.

**Estados:** ✅ en `main` · 🔨 en curso o en PR abierto · ⏳ pendiente.

## Piezas de la ruta crítica

| # | Pieza | Dueño | Estado | Desbloquea a |
|---|---|---|---|---|
| B0 | Andamiaje y `shared` | André | ✅ | Todos |
| B1 | `iam`: `University` completo | André | ✅ | Modelo para todos |
| B2 | `StudentJpaEntity` (tabla `students` con FK) | Fernando | ✅ | La persistencia de todos |
| B3 | Interfaces de las fachadas | Cada dueño | 🔨 `profiles`, `connections` y `moderation` con interfaz e implementación ✅; `iam` (universidades) pendiente de fusionar; `subscriptions` ⏳ (después de la entrega) | Quien consume cada fachada |
| B4 | Eventos de dominio de `connections` y `meetups` | Ubes, Jesús | ✅ | `messaging` |
| B5 | Capa de aplicación de `iam` (CQRS) y clase base pura en `shared` | André | ✅ | La capa de aplicación de todos |
| B6 | `Message` mínimo con `MessageSentEvent` | Mafer | ✅ | El WebSocket |
| B7 | WebSocket: configuración STOMP, manejador del evento y `/chat-test.html` | André | ✅ | Chat en tiempo real |

## Qué hay en `main` (21-oct)

Endpoints vivos: `universities`, `connections`, `blocks` y `reports`. Además, el WebSocket `/ws` y la página
`/chat-test.html`. Modelos para copiar: `iam`, `moderation` y `connections` (persistencia, aplicación, controllers y
pruebas HTTP con MockMvc).

| Módulo | Dueño | Dominio | Persistencia | Aplicación | Endpoints |
|---|---|---|---|---|---|
| `iam` | André | ✅ | ✅ | ✅ | ✅ |
| `profiles` | Fernando | ✅ | ✅ | 🔨 Catálogos ✅; falta Student y Profile | ⏳ |
| `meetups` | Jesús | ✅ | ✅ | ✅ | ⏳ |
| `communities` | Mafer | ✅ | ✅ | ✅ | ⏳ Resources listos |
| `messaging` | Mafer | 🔨 `Message` mínimo (B6) | ⏳ | 🔨 Manejador del evento ✅ | ⏳ |
| `connections` | Ubes | ✅ | ✅ | ✅ | ✅ |
| `moderation` | Ubes | ✅ | ✅ | ✅ | ✅ |
| `subscriptions` | André | ⏳ | ⏳ | ⏳ | ⏳ Después de la entrega |

## Qué falta para la entrega del 22-oct

Prioridad: **endpoints visibles en Swagger** (crear, obtener, listar y una o dos acciones clave). Sin sobreingeniería.
Cada módulo abre un PR por bloque en cuanto lo termina; no se espera a tener todo.

| Integrante | Módulo | Falta para el 22-oct |
|---|---|---|
| **André** | `iam`, `shared` | Fusionar la fachada de universidades de `iam`; pruebas con MySQL y Swagger; panel al día; tag `v0.2.0`. `Account` solo si sobra tiempo |
| **Fernando** | `profiles` | **1.º** `POST` y `GET` de estudiantes (`CreateStudentCommandHandler` con la fachada de `iam`) y su controller; **2.º** controllers de intereses y habilidades; **3.º** **EER Diagram y diccionario de datos** al final, cuando existan todas las tablas |
| **Jesús** | `meetups` | Resources, assembler y `MeetupsController` con los handlers de `main`: crear, listar, obtener, solicitar, confirmar, invitar y cancelar; prueba HTTP y colección Postman |
| **Mafer** | `communities`, `messaging` | `CommunitiesController` (crear, listar, unirse y publicar); después, persistencia de `messaging` y `POST /api/v1/messages` para el chat de punta a punta |
| **Ubes** | `connections`, `moderation` | Sus dos módulos están completos. Le queda el `00-setup` de Postman |

Si el tiempo aprieta, **se recorta el final de la lista** (`Account`, afinidad, `Notification`, persistencia de
`messaging`, acciones secundarias), nunca la persistencia de lo que ya existe.

## Quién depende de quién

- **No existe todavía un endpoint para crear estudiantes**: `connections`, `meetups` y `communities` no se pueden
  demostrar sin estudiantes. Por eso los estudiantes de Fernando son lo primero, y dependen de la fachada de universidades
  de `iam` (André).
- El **EER Diagram** (Fernando) espera las tablas: ya existen todas salvo `messages`.
- El **chat de punta a punta** necesita la persistencia y los controllers de `messaging` (Mafer); las fachadas de
  `connections` y `moderation` que usa ya están.
- El tag `v0.2.0` espera a que todo esté fusionado.
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
