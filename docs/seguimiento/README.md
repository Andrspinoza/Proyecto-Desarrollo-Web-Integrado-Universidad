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
| B3 | Interfaces de las fachadas (solo firmas) | Cada dueño | ✅ | Los consumidores de cada fachada |
| B4 | Eventos de dominio de `connections` y `meetups` | Ubes, Jesús | ✅ | `messaging` |
| B5 | Capa de aplicación de `iam` (CQRS) y clase base pura en `shared` | André | ✅ | La capa de aplicación de todos |
| B6 | `Message` mínimo con `MessageSentEvent` | Mafer | ✅ | El WebSocket |
| B7 | WebSocket: configuración STOMP, manejador del evento y `/chat-test.html` | André | ⏳ | Chat en tiempo real |

## Qué hay en `main` (21-oct)

Endpoints vivos: `universities` y `moderation` (reportes y bloqueos). Modelos para copiar: `iam` (capa de aplicación)
y `moderation` (persistencia, aplicación, controllers y pruebas HTTP con MockMvc).

| Módulo | Dueño | Dominio | Persistencia | Aplicación | Endpoints |
|---|---|---|---|---|---|
| `iam` | André | ✅ | ✅ | ✅ | ✅ |
| `profiles` | Fernando | ✅ | ✅ | 🔨 Catálogos ✅; falta Student y Profile | ⏳ |
| `meetups` | Jesús | ✅ | ✅ | ✅ | ⏳ |
| `communities` | Mafer | ✅ | ✅ | ⏳ | ⏳ Resources listos |
| `messaging` | Mafer | 🔨 `Message` mínimo (B6) | ⏳ | ⏳ | ⏳ |
| `connections` | Ubes | ✅ | ⏳ | ⏳ | ⏳ Resources listos |
| `moderation` | Ubes | ✅ | ✅ | ✅ | ✅ |
| `subscriptions` | André | ⏳ | ⏳ | ⏳ | ⏳ Después de la entrega |

## Qué falta para la entrega del 22-oct

Prioridad: **endpoints visibles en Swagger** (crear, obtener, listar y una o dos acciones clave). Sin sobreingeniería.
Cada módulo abre un PR por bloque en cuanto lo termina; no se espera a tener todo.

| Integrante | Módulo | Falta para el 22-oct |
|---|---|---|
| **André** | `iam`, `shared` | **B7** (WebSocket); pruebas en Swagger; panel al día; tag `v0.2.0`. `Account` solo si sobra tiempo |
| **Fernando** | `profiles` | Controllers de intereses y habilidades (crear y listar); capa de aplicación de Student y Profile y endpoints de estudiante; **EER Diagram y diccionario de datos** al final, cuando existan todas las tablas |
| **Jesús** | `meetups` | Resources, assembler y `MeetupsController` con los handlers de `main`: crear, listar, obtener, solicitar, confirmar, invitar y cancelar; prueba HTTP y colección Postman |
| **Mafer** | `communities`, `messaging` | Capa de aplicación de `communities` (copiar `iam`) y controllers: crear, listar, unirse y publicar. `messaging` (persistencia, aplicación, endpoints) queda para después |
| **Ubes** | `connections`, `moderation` | `connections`: persistencia con test H2, capa de aplicación y controllers (crear, listar y aceptar conexión); `00-setup` de Postman. `moderation` está completo |

Si el tiempo aprieta, **se recorta el final de la lista** (`Account`, afinidad, `Notification`, persistencia de `messaging`,
acciones secundarias), nunca la persistencia de lo que ya existe.

## Quién depende de quién

Para la entrega, **nadie bloquea a nadie**: los handlers, la tabla `students` y la fachada de `profiles` ya están en `main`.
Las únicas cadenas son: el **EER Diagram** (Fernando) espera a que existan todas las tablas, en particular la persistencia de
`connections` (Ubes); el chat completo de punta a punta (después de la entrega) necesita la persistencia y los controllers de
`messaging` (Mafer) y la fachada de `connections` (Ubes); el tag `v0.2.0` espera a que todo esté fusionado.

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
