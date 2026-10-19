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
| B5 | Capa de aplicación de `iam` (CQRS) y clase base pura en `shared` | André | ✅ | La capa de aplicación de todos |
| B6 | `Message` mínimo con `MessageSentEvent` | Mafer | ⏳ | El WebSocket |
| B7 | WebSocket: configuración STOMP, manejador del evento y `/chat-test.html` | André | ⏳ | Chat en tiempo real |

## Qué falta para la entrega del 22-oct

Cada agregado avanza por niveles: dominio con tests → persistencia con test H2 → crear, obtener y
listar → una o dos acciones clave. Endpoints simples, sin sobreingeniería. Swagger es requisito.

| Integrante | Módulo | Ya está en `main` | Falta para el 22-oct |
|---|---|---|---|
| **André** | `iam`, `shared` | `University` completo con su capa de aplicación (B5); clase base pura y despachador de eventos | `Account` con BCrypt; **B7** (WebSocket); pruebas en Swagger; tag `v0.2.0` |
| **Fernando** | `profiles` | `Student`, `FullName`, `StudentRecordId`, `StudyCycle`, `StudentJpaEntity` | `StudentRepository`, mapper, adapter y test H2; `Profile` (carrera, ciclo, descripción, visibilidad), `Interest` y `Skill` con su persistencia; endpoints de estudiante; **EER Diagram y diccionario de datos** |
| **Ubes** | `connections`, `moderation` | Dominio, repositorios, resources y colección Postman de `moderation` | `connections`: dominio, repositorio, resources y Postman en su PR; persistencia con test H2 de ambos; endpoints de crear, listar y aceptar conexión, y de reportar y bloquear; `00-setup` de Postman |
| **Mafer** | `communities`, `messaging` | Dominio completo de `communities` (con `promote`), contratos y resources | Persistencia con test H2 (comunidades, miembros, publicaciones); endpoints de crear, listar, unirse y publicar; **B6** (`Message` mínimo y `MessageSentEvent`) |
| **Jesús** | `meetups` | Nada todavía en `main` (dominio avanzado en su rama, sin subir) | Dominio con tests (cupo, organizador, cancelación, **visibilidad `PUBLIC` y `PRIVATE` e invitaciones**, eventos y excepciones según la guía 8.4); persistencia con bloqueo pesimista y test H2; endpoints de crear, listar, solicitar, invitar, confirmar y cancelar. Antes de abrir su PR: `git pull origin main` |
| **Todos** | Capa de aplicación | — | Commands, queries, handlers, DTOs y assembler de su módulo, copiando `iam` (B5, ya en `main`); el controller pasa a usar los handlers |

## En qué orden se hace

Dentro de cada ola, todos trabajan **en paralelo**; no se pasa a la siguiente ola sin lo que pide.

| Ola | Cuándo | Qué se hace | Quién | Necesita |
|---|---|---|---|---|
| **1** | Vie 16 – Sáb 17 | Cerrar PR de dominio de `meetups` (eventos y excepciones, guía 8.4) | Jesús | Nada |
| | | `StudentRepository`, mapper, adapter y test H2 | Fernando | B2 ✅ |
| | | PR de dominio de `connections`; persistencia de `moderation` | Ubes | B2 ✅ |
| | | Persistencia de `communities` | Mafer | B2 ✅ |
| | | **B5**: clase base pura, despachador y capa de aplicación de `iam` | André | Nada |
| **2** | Dom 18 – Lun 19 | Persistencia con bloqueo pesimista de `meetups` | Jesús | Dominio de `meetups` fusionado |
| | | `Profile`, `Interest`, `Skill` y su persistencia | Fernando | Adapter de `Student` |
| | | Persistencia de `connections` | Ubes | Dominio de `connections` fusionado |
| | | **B6**: `Message` mínimo con `MessageSentEvent` | Mafer | Persistencia de `communities` |
| | | **Capa de aplicación** de cada módulo, copiando `iam` | Cada dueño | **B5 en `main`** |
| **3** | Mar 20 | Controllers de cada módulo, con crear, obtener, listar y una o dos acciones clave | Cada dueño | Su persistencia y su capa de aplicación |
| | | **B7**: WebSocket y `/chat-test.html` | André | B6 |
| **4** | Mié 21 | Colecciones Postman y `00-setup`; pruebas en Swagger | Ubes y cada dueño | Los controllers |
| | | **EER Diagram y diccionario de datos** | Fernando | Todas las tablas creadas |
| | | `Account` y ensayo de la demo; tag `v0.2.0` | André | Todo lo anterior |
| **Entrega** | Jue 22 | APF2 | Todos | — |

Si una ola se atrasa, **se recorta el final de la lista (controllers de acciones secundarias,
`Account`), nunca la persistencia**: sin tablas no hay EER ni demo.

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
