# AGENTS.md — ¿Qué sale? (API backend)

Instrucciones para **todas las personas del equipo y todos los agentes de IA** (Codex, Claude
Code, Gemini/Antigravity, GitHub Copilot u otro) que trabajen en este repositorio.

> **Lectura obligatoria.** Antes de cada tarea, el agente lee **este archivo completo** y, antes
> de escribir código, [`docs/guia-backend.md`](docs/guia-backend.md). Si encuentra una
> contradicción entre ellos, con el código o con lo que le piden, **se detiene y lo dice**; no
> elige por su cuenta.

| Archivo | Qué regula |
|---|---|
| `AGENTS.md` (este) | Producto, decisiones, **estado actual y quién sigue**, preguntas frecuentes, equipo, git, commits, orden de trabajo, calendario |
| [`docs/guia-backend.md`](docs/guia-backend.md) | Guía técnica: molde del profesor, capas, contextos, endpoints, pruebas, base de datos, guion de exposición |
| `docs/seguimiento/<modulo>.md` | Avance de cada módulo: bloques hechos, PR y siguiente paso |

**Cada regla vive en un solo archivo.** No se copian reglas de uno a otro. La guía técnica está
en `docs/` porque Codex solo carga los primeros 32 KB de los `AGENTS.md`.

### Si eres el agente de un integrante

Tu trabajo es también **responder cualquier duda del proyecto**: en qué orden va todo, qué le
toca a la persona, a quién espera, qué puede hacer mientras, cómo se hace un commit o un PR.
Responde **desde este archivo** (secciones 2, 3 y 8 primero), cita la sección y, si la
respuesta no está aquí, dilo y sugiere preguntarle a André. No inventes decisiones.

---

## 1. El producto

**¿Qué sale?** — título formal: *Sistema de Conexión y Organización de Meetups Académicos y
Sociales para Estudiantes Universitarios*. Curso **Desarrollo Web Integrado (100000ST61)**,
Ciclo VII, UTP, 2026-2. Docente: **Iván Robles Fernández**. Clase: jueves 4:00 p. m.

Plataforma social universitaria: descubrir estudiantes con gustos afines, conectar y concretar
salidas de dos personas o juntadas grupales; también admite encuentros de estudio. Puede
surgir amistad o interés consensuado; no es una plataforma de contenido sexual.

- **Usuarios: exclusivamente alumnos universitarios** con correo institucional de una
  universidad admitida, más administradores. No se admite público general.
- **Match = solicitud de conexión aceptada.** El porcentaje de afinidad **no** es un match ni
  habilita mensajes.
- Un **meetup** es una reunión presencial académica o social de dos o más personas.

Este repositorio es **solo la API** (Spring Boot). El frontend (Angular) irá en **otro
repositorio** desde la semana 11 (D-15).

### Fuentes de verdad, en orden

1. Indicaciones del profesor (sección 13) y su código de ejemplo (molde descrito en la guía).
2. Modelo de dominio oficial: diagrama de clases y modelo relacional del *Avance de Proyecto
   Final 2* del curso Integrador I (resumidos en la guía, sección 7).
3. Historias de usuario HU01–HU10 y requerimientos funcionales del mismo avance.
4. Comportamientos del prototipo presentado en el APF1: bloqueo, motivos de reporte, límites
   del plan gratuito, meetups para dos y grupales.

---

## 2. Estado actual y quién sigue — se actualiza en cada merge

**Esta es la tabla que manda.** Quien fusiona un PR que cambia una fila, la actualiza en ese
mismo PR (o André al fusionar).

Qué son **B0, B1, B2…**: piezas que **otros necesitan para avanzar** (ruta crítica). Van en
cadena por las llaves foráneas de MySQL: sin el proyecto (B0) nada compila; sin la tabla
`universities` (B1) no existe la FK de `students`; sin la tabla `students` (B2) no existen las
FKs de conexiones, meetups, comunidades, mensajes ni reportes.

| # | Pieza | Dueño | Desbloquea a | Estado |
|---|---|---|---|---|
| B0 | Andamiaje: `pom.xml`, Maven Wrapper, `application.properties`, `compose.yaml`, `shared` (excepciones, manejador de errores, header `X-Student-Id`, Swagger) | André | **Todos** (sin esto nada compila) | ✅ Entregado en `main` |
| B1 | `iam`: agregado `University` completo (dominio → JPA → endpoint → test H2). **Es el ejemplo de referencia del repo** | André | Fernando (FK de `students`) y todos como modelo | ✅ Entregado en `main` |
| B2 | `profiles`: `StudentJpaEntity` con FK a `universities` | Fernando | Ubes, Jesús, Mafer (sus FKs a `students`) | 🔨 Puede empezar — Fernando |
| B3 | Interfaces de las fachadas (solo firmas, guía 6.3) | Cada dueño | Quien las consume | ⏳ Después de cada dominio |
| B4 | Eventos de dominio de `connections` y `meetups` | Ubes, Jesús | Mafer (`messaging`) | ⏳ |

**Quién sigue ahora:** B0 y B1 ya están en `main`. **Todos** traen `main` a su rama
(sección 5) y arrancan su **dominio con tests** en paralelo. **Fernando hace B2 primero**
(`StudentJpaEntity`); los demás agregan su persistencia cuando B2 esté en `main`.

| Integrante | Qué hace hoy | Puede empezar cuando | Después |
|---|---|---|---|
| André | `Account`, fachadas de `iam` y `subscriptions` | Ya | `subscriptions`, revisar PRs |
| Fernando | Dominio de `profiles` y **B2** | Ya | Resto de persistencia → endpoints → EER y diccionario |
| Ubes | Dominio de `moderation` y `connections` | Ya | Fachadas → persistencia (tras B2) → endpoints → Postman `00-setup` |
| Jesús | Dominio de `meetups` con tests | Ya | Persistencia con bloqueo pesimista (tras B2) → endpoints |
| Mafer | Dominio de `communities` | Ya | Persistencia (tras B2) → luego `messaging` (tras B3 de Ubes y B4) |

**Mientras esperas B2** haz lo que no depende de nadie: dominio y tests, resources y
assemblers, colección de Postman. Toma `iam` como modelo. No crees entidades de otro contexto.

---

## 3. Preguntas frecuentes

**¿Qué me toca y en qué orden?** Sección 2 (tabla «Quién sigue») y sección 7.

**¿Por qué no puedo empezar ya?** Porque sin B0 no hay `pom.xml` y nada compila. Con B0 en
`main` ya puedes hacer **todo tu dominio y sus tests**: el dominio solo depende de `shared`.

**Necesito la tabla de otro (FK) y aún no está.** Haz lo que no depende de nadie: dominio,
tests de dominio, resources y assemblers, colección de Postman. **Nunca** crees tú la entidad
de otro contexto para adelantar; avísale al dueño.

**¿Copio la demo del profesor / la demo de André?** No. La demo (`_demo-quesale`, fuera del
repo) fue una prueba local que al profesor le pareció bien; el contexto `iam` del repo (B1) es
su versión oficial. Úsalo **como modelo**: mismo estilo, mismas capas, mismos nombres de
carpetas, pero escribes **tu** contexto, archivo por archivo, entendiendo cada uno y con
commits progresivos. El profesor revisa quién contribuyó y qué puede explicar.

**¿Dónde veo un ejemplo completo?** `src/main/java/com/utp/quesale/iam/` y su test en
`src/test/java/com/utp/quesale/iam/`.

**¿Y la capa de aplicación?** Todavía no. El profesor la enseña el 15-oct. Hasta entonces el
controller usa la interfaz del repositorio de dominio (guía 1.4). Cuando la enseñe, se agrega
sin tocar el dominio ni la persistencia.

**¿Cómo sé quién hace la petición si no hay login?** Header `X-Student-Id` con el id del
estudiante (D-16). JWT llegará cuando el profesor lo enseñe.

**¿Qué hago antes de cada commit?** `./mvnw -q compile` y, si hay tests, `./mvnw test`. Luego
`git add` archivo por archivo (sección 6).

**¿Quién revisa y fusiona?** André, con «Create a merge commit» (D-07). Tu rama no se borra.

**¿Necesito MySQL para los tests?** No: los tests usan H2 en memoria (D-14). MySQL solo para
correr la API y ver Swagger.

**¿Dónde corto y abro el PR?** Al final de cada bloque (sección 6, «Dónde termina un PR»).
El agente te muestra el plan completo y te dice hasta dónde puedes avanzar.

**¿Cómo sé qué hicieron los demás, sin `gh`?** Lee `docs/seguimiento/<modulo>.md` en `main` y
`git log origin/main`. `gh` es opcional.

**¿Qué se presenta el 15-oct y qué el 22-oct?** Sección 8.

**Encontré algo que contradice este archivo.** Detente, avísale a André y anótalo; no elijas.

---

## 4. Decisiones

Estados: ✅ decidida · 🟡 propuesta, se puede cambiar · ❓ pendiente · ⛔ reemplazada.

| ID | Estado | Decisión | Motivo |
|---|---|---|---|
| D-01 | ✅ | Spring Boot **4.1.1**, Java 21, Maven, Jar, Properties | Igual al molde del profesor |
| D-02 | ✅ | **Sin Lombok en `domain` ni en `application`**; solo en entidades JPA | Indicación del profesor: el dominio no depende de librerías externas |
| D-03 | 🟡 | Capa `application` **pendiente**: se escribe cuando el profesor la enseñe (15-oct). Mientras, el controller usa el repositorio de dominio | Indicación del profesor del 11-oct |
| D-04 | ⛔ | ~~Identidad por header `X-Account-Id`~~ — reemplazada por D-16 | `Account` aún no existe |
| D-05 | ✅ | Entre contextos, FKs reales en MySQL vía `@ManyToOne` en las entidades JPA; el dominio usa `Long` | Sin FKs, el EER Diagram de Workbench sale sin relaciones |
| D-06 | ✅ | **Swagger** (springdoc) como tecnología nueva propuesta; pruebas también en Postman y Thunder Client | Documenta y prueba la API en el navegador |
| D-07 | ✅ | Pull Requests fusionados con **«Create a merge commit»**, nunca *squash* | *Squash* aplasta los commits progresivos en uno solo |
| D-08 | ✅ | **Sin línea `Co-Authored-By` de IA** en los commits | El historial muestra solo a los integrantes |
| D-09 | ✅ | Producción en **AWS (Ubuntu)**; Contabo o DigitalOcean como staging opcional | Diagrama del profesor |
| D-10 | 🟡 | Tablero Kanban con issues de GitHub, si alcanza el tiempo | Buena práctica, no bloquea |
| D-11 | ⛔ | ~~Monorepo `backend/` + `frontend/`~~ — reemplazada por D-15 | El profesor pidió repos separados |
| D-12 | ✅ | Código en inglés; comentarios, commits y documentación en español | El código del profesor está en inglés |
| D-13 | ✅ | La fachada de `subscriptions` solo **informa límites** del plan; cada contexto cuenta su propio uso | Ningún contexto lee tablas de otro |
| D-14 | ✅ | Cada adapter tiene un test `@DataJpaTest` sobre **H2** en memoria; los tests de dominio son JUnit puro | Indicación del profesor: probar la persistencia sin MySQL |
| D-15 | ✅ | **Repos separados**: este repo es solo la API, con el proyecto Maven **en la raíz**; el frontend va en otro repo | Indicación del profesor |
| D-16 | 🟡 | Identidad provisional por header **`X-Student-Id`** hasta ver Spring Security + JWT | Permite probar endpoints sin login ni `Account` |
| D-17 | ✅ | **Chat en tiempo real por WebSocket (STOMP)**: REST envía y guarda, el WebSocket entrega al instante. Detalle en la guía, 8.6 | Un chat no puede entregar con retraso; es requisito del producto |

**Cómo cambia una decisión:** no se borra. Su fila pasa a ⛔ «reemplazada por D-NN», se
agrega la fila nueva y se anota en el Registro de cambios (sección 15). Si una decisión no está
en esta tabla, **no está decidida**: ni los chats, ni los planes locales de un agente, ni los
archivos puntero cuentan.

---

## 5. Equipo, ramas y git

| Integrante | Rama | Contextos | Transversal |
|---|---|---|---|
| **André Espinoza** | `dev-qa/andre` | `shared`, `iam`, `subscriptions` | Tech Lead: andamiaje, revisión de todos los PR, configuración del repo |
| **Jesús Fernando Espinoza** | `dev/jesus` | `meetups` | — |
| **Bruno Fernando Escobedo** (Fernando) | `dev/fernando` | `profiles` | MySQL, EER Diagram de Workbench, diccionario de datos, despliegue |
| **María Fernanda Licas** (Mafer) | `dev/mafer` | `communities`, `messaging` | Guion de exposición |
| **Ubes Manuel Páez** | `dev/ubes` | `moderation`, `connections` | QA: colecciones Postman/Thunder y casos de prueba |

🟡 El reparto puede ajustarse; si cambia, se actualiza esta tabla.

**Nadie toca el contexto de otro integrante**, ni `pom.xml` ni
`src/main/java/com/utp/quesale/shared/` sin avisar antes a André. Si algo de otro contexto
bloquea, se le avisa al dueño.

### Git lo ejecuta la persona, no el agente

El agente **no ejecuta** `git add`, `commit`, `push`, `pull`, `merge`, `rebase`, `reset`,
`checkout`, `stash` ni `clean`. Solo puede leer: `status`, `log`, `diff`, `show`, `branch`.
Si la persona le pide explícitamente ejecutar algo, lo hace solo para esa vez.

**Flujo diario (GitHub Flow con ramas personales):**

1. Antes de cada tarea nueva, traer `main` a tu rama:

   ```bash
   git checkout dev/<tu-nombre>
   git fetch origin
   git merge origin/main
   ```

2. Trabajar en pasos con **punto de control** (sección 6).
3. `git push` y abrir un Pull Request de tu rama hacia `main`, asignado a André.
4. André revisa y fusiona con **«Create a merge commit»** (D-07).
5. Tu rama **no se borra**: sigues usándola.

Prohibido: trabajar sobre `main`, `git add .`, `git add -A`, `--amend` o `push --force` sobre
commits ya subidos, y `git clean`.

### Onboarding — primera vez

Si alguien escribe algo como *«hola, soy Jesús»*, el agente lo guía por esta lista **un paso a
la vez**, esperando confirmación antes de seguir, y al final le dice qué le toca (sección 2).

1. **Instalar:** Git, JDK 21 (`java -version` debe decir 21), MySQL 8 (instalado o con Docker
   Desktop), MySQL Workbench y un IDE (IntelliJ IDEA o VS Code con Extension Pack for Java).
2. **Identidad de git — con tus datos reales** (el profesor revisa quién contribuyó):

   ```bash
   git config --global user.name "Tu Nombre"
   git config --global user.email "el-correo-de-tu-cuenta-de-github"
   ```

3. **Aceptar la invitación** al repositorio (es privado) y clonar:

   ```bash
   git clone https://github.com/Andrspinoza/Proyecto-Desarrollo-Web-Integrado-Universidad.git
   ```

4. **Crear tu rama personal una sola vez** (ejemplo para Jesús). Si ya la creaste antes de B0,
   trae `main` con el flujo diario:

   ```bash
   git checkout -b dev/jesus
   git push -u origin dev/jesus
   ```

5. **Verificar la API**, desde la raíz del repo: `./mvnw test`
   (en Windows `mvnw.cmd test`), luego `./mvnw spring-boot:run` con MySQL encendido y abrir
   http://localhost:8080/swagger-ui.html. Si tu contraseña de MySQL no es `root`, ver la guía,
   sección 2.

---

## 6. Commits progresivos y punto de control — aplica a todos

El historial debe contar cómo se construyó el proyecto, paso a paso. **Nada de commits con
muchos cambios mezclados.**

- **Un commit = un cambio lógico que compila.** Se construye de abajo hacia arriba para que cada
  paso compile solo (orden exacto en la guía, sección 9).
- Antes de cada commit: `./mvnw -q compile`, y `./mvnw test` si el paso trae tests.
- No se mezclan capas ni contextos en un commit; tampoco un refactor con una funcionalidad.
- Cada persona commitea **solo su trabajo, con su propia identidad**. Nadie commitea el contexto
  de otro.
- Mensajes en español, formato convencional, imperativo y sin tildes en el mensaje:

  ```text
  feat(meetups): agregar value object Capacity con validacion de cupo minimo
  ```

  Tipos: `feat`, `fix`, `test`, `refactor`, `docs`, `chore`, `build`. El ámbito es el contexto
  (`meetups`, `profiles`…), `shared` o `agents`; sin ámbito para el proyecto en general
  (`build: agregar pom y Maven Wrapper`).
- **Sin línea `Co-Authored-By` de IA** (D-08).

### Protocolo de punto de control — obligatorio para todo agente

Al terminar cada paso, el agente **se detiene** con este formato y **no escribe más código**:

```text
⏸ PUNTO DE CONTROL — paso N de M (<contexto>)
Qué hice: <2-3 líneas>
Cómo verificarlo: ./mvnw -q compile   (y ./mvnw test si aplica)
Revisa estos archivos: <lista>
Cuando estés conforme, ejecuta:
  git add <archivos exactos, uno por uno>
  git commit -m "<tipo(ambito): mensaje>"
Escríbeme «listo» para seguir con el paso N+1: <qué viene>.
```

- Solo continúa cuando la persona responde que revisó e hizo el commit.
- Si la persona pide cambios, se corrigen **dentro del mismo paso**, antes del commit.
- El agente **explica lo que hizo** para que la persona pueda defenderlo en la exposición.

### Dónde termina un PR y cómo se sigue

Un PR es un **bloque**, no el trabajo completo de un módulo. Los bloques son: (1) dominio con
tests, (2) persistencia con test H2, (3) capa de aplicación, (4) resources y endpoints. Un
bloque grande se parte en dos PR; un PR no pasa de unos 15 commits.

1. **Al empezar**, el agente le muestra a la persona el plan completo en bloques: cuántos
   commits tiene cada uno, dónde cae cada PR y qué bloques **no puede hacer todavía** porque
   dependen de una pieza sin entregar (B2, una fachada…). Esos bloques quedan fuera y el
   agente lo dice con claridad: «hasta aquí puedes avanzar».
2. **Al terminar un bloque**, el agente se detiene con este aviso y no sigue:

   ```text
   ⏸ PUNTO DE PR — bloque N de M (<módulo>)
   Commits de este PR: <lista>
   Antes del último commit: actualiza docs/seguimiento/<modulo>.md
   Ejecuta: git push
   Abre el PR de <tu rama> hacia main con la plantilla y avisa a André.
   Siguiente bloque: <cuál> — empieza cuando este PR esté fusionado y hayas traído main.
   ```

3. **Mientras el PR está abierto**, la persona puede seguir con el siguiente bloque en commits
   locales, pero **no hace `push`** hasta que el PR se fusione y haya traído `main`. Si sube
   antes, esos commits entran al PR que ya está abierto.
4. Si el PR necesita correcciones, se agregan commits a la misma rama y el PR se actualiza solo.
   Nunca `--amend` ni `push --force`.

### Seguimiento de avance

La constancia del avance no puede depender de `gh` ni de los PR de GitHub. Cada módulo tiene su
archivo `docs/seguimiento/<modulo>.md`, con una fila por bloque, su estado, el número de PR y el
siguiente paso. **Solo el dueño del módulo edita su archivo**, y lo hace en el último commit de
cada PR, así que llega a `main` junto con el trabajo. Cualquier agente, aunque no tenga `gh`,
sabe qué hay hecho leyendo esos archivos y el `git log origin/main`.

---

## 7. Orden de trabajo y dependencias

El dominio de cada contexto depende solo de `shared`, así que **los cinco arrancan su dominio
en paralelo** apenas B0 esté en `main`. Los cruces aparecen en dos puntos: la **persistencia**
(FKs hacia `universities` y `students`, D-05) y, más adelante, la **aplicación** (fachadas de
otros contextos). Por eso cada uno hace primero lo que no depende de nadie, y los dueños de
piezas compartidas las entregan antes (sección 2).

Las firmas de las fachadas ya están fijadas en la guía (6.3, contrato primero): cada
consumidor programa contra ellas aunque la implementación llegue después.

| Integrante | Orden | Espera a |
|---|---|---|
| **André** | B0 → B1 (`University`) → `Account` → fachadas de `iam` y `subscriptions` → `subscriptions` → configurar el repo y revisar PRs | — |
| **Fernando** | `profiles` dominio → **B2 primero** → resto de persistencia → fachada de `profiles` → endpoints → EER Diagram y diccionario | B0; B1 para B2 |
| **Ubes** | `moderation` dominio → `connections` dominio → fachadas de ambos → persistencia → endpoints → colección Postman `00-setup` | B0; B2 para persistencia |
| **Jesús** | `meetups` dominio con tests (es el más grande) → persistencia con bloqueo pesimista → endpoints | B0; B2 para persistencia |
| **Mafer** | `communities` completo → **después** `messaging` y sus manejadores de eventos | B0, B2; para `messaging`, fachadas de Ubes y B4 |

Cada agregado avanza por niveles (guía, sección 8): dominio + test → persistencia + test H2 →
crear, obtener y listar → una o dos acciones clave.

**Orden de merge a `main`:** B0 → B1 → B2 → el resto en paralelo.

---

## 8. Calendario

| Fecha | Meta |
|---|---|
| **Dom 11 – Lun 12-oct** | André: B0 en `main`; luego B1. Todos traen `main` y arrancan su dominio con tests |
| Mar 13 – Mié 14 | Fernando: B2. Dominio y tests de todos en `main`; persistencia de `iam` y `profiles` |
| **Jue 15-oct, 4 p. m.** | **Avance** ante el profesor (base: lo que le gustó de la demo). En clase enseña la **capa de aplicación** |
| Vie 16 – Mié 21 | Persistencia con test H2 de todos los agregados, endpoints, capa de aplicación según el profesor, colecciones Postman, EER Diagram y diccionario |
| **Jue 22-oct (semana 11)** | **Entrega APF2 (20%)** — tag `v0.2.0` |
| Cuando lo enseñe | Spring Security + JWT reemplaza `X-Student-Id` (D-16) |
| Semana 11 en adelante | Frontend Angular + SCSS en su propio repositorio (D-15) |

---

## 9. Herramientas de IA

Este archivo es la **única fuente** de reglas para cualquier herramienta. Las que buscan otro
archivo tienen un **puntero** que solo remite aquí y **nunca lleva contenido propio**:

| Herramienta | Archivo |
|---|---|
| Codex | Lee `AGENTS.md` directamente |
| Claude Code | `CLAUDE.md` → `@AGENTS.md` |
| Gemini CLI / Antigravity | `GEMINI.md` → `@AGENTS.md` |
| GitHub Copilot | `.github/copilot-instructions.md` y `.github/instructions/backend.instructions.md` |
| IA de chat sin acceso al repo | La persona pega este archivo, la sección de la guía de su contexto y su tarea |
| Otra que se sume | Verificar qué archivo lee; si no lee `AGENTS.md`, crear su puntero y registrarlo |

Las reglas valen igual para cualquier herramienta. **La responsabilidad de cada commit es de la
persona**, no de la IA.

---

## 10. Este archivo es un documento vivo

Habrá correcciones: decisiones nuevas, cambios de rumbo y, sobre todo, el feedback del profesor.

- **Cualquier agente que cambie una regla, una decisión o el modelo** edita `AGENTS.md` o la
  guía y agrega una fila a su **Registro de cambios**.
- Ese cambio es **un commit propio**: `docs(agents): reemplazar D-16 por autenticacion JWT`.
- La **tabla de estado (sección 2)** se actualiza cuando se fusiona una pieza B.
- **Código y documentación nunca se contradicen.** Si un PR implementa algo distinto a lo
  escrito, en el mismo PR se corrige el código o se actualiza el documento.
- Los archivos puntero (`CLAUDE.md`, `GEMINI.md`, `.github/...`) **nunca se editan con reglas**.
- Este archivo debe seguir **por debajo de 32 KB** (límite de Codex); lo técnico va en la guía.

---

## 11. Kanban con issues de GitHub — 🟡 si alcanza el tiempo (D-10)

No bloquea el código. GitHub Project en vista Board (`Backlog · Por hacer · En progreso · En
revisión · Hecho`), **un issue por bloque** («meetups — dominio», «meetups — persistencia»…),
etiquetas `ctx:<contexto>` y `layer:<capa>`. El PR lleva `Closes #NN`. Si hay issue, el agente
pide su número y trabaja solo dentro de lo que autoriza. Las observaciones del profesor se
registran con la etiqueta `feedback-profesor`.

---

## 12. Se presenta y se explica código

- Cada integrante explica **su contexto completo** y el recorrido de un endpoint por las capas:
  controller → assembler → agregado (regla) → repositorio de dominio → adapter → mapper →
  entidad JPA → MySQL, y de vuelta como resource. Con la capa de aplicación, el servicio entra
  entre el controller y el agregado.
- El **guion de exposición** por contexto está en la guía, sección 12.
- Comentarios breves en español **solo** donde explican una invariante o por qué algo vive en
  esa capa.
- Nadie sube código que no puede explicar.

---

## 13. Indicaciones del profesor

Registro textual o resumido de lo que pide el docente. Es la primera fuente de verdad.

| Fecha | Indicación |
|---|---|
| 1-oct | No debe haber Lombok en el dominio: el dominio representa solo la lógica de negocio y, cuanto más puro, sin dependencias externas, mejor |
| 1-oct | JPA es una capa de abstracción: con JDBC hay que escribir cada método; con JPA se automatiza (`interface StudentJpaRepository extends JpaRepository<StudentEntity, Long>`) |
| 1-oct | La capa de infraestructura depende de la capa de dominio |
| 1-oct | El mapper existe para pasar de entidad JPA a objeto de dominio y viceversa |
| 1-oct | La inyección de dependencias se realiza en el constructor del adapter (`StudentRepositoryAdapter`) |
| 1-oct | Próximo avance: gran porcentaje de APIs y endpoints expuestos, todo el modelo de dominio expuesto, probar cada endpoint |
| 8-oct | Backend y frontend en **repositorios separados** (D-15) |
| 8-oct | Implementar **todos los agregados con su persistencia** y exponer endpoints; el endpoint llega al adapter |
| 8-oct | Todavía **no** JWT |
| 8-oct | Probar la persistencia con **H2** (D-14) |
| 8-oct | EER Diagram por ingeniería inversa en MySQL Workbench |
| 11-oct | La demo va bien; falta la **capa de aplicación**, que enseña en la próxima clase (D-03) |
| 11-oct | La entrega se **extiende a la semana 11** (22-oct); el 15-oct se presenta un avance |
| 15-oct | Capa de aplicación con **CQRS**: commands y queries, un handler por cada uno, DTOs y assembler en `application`; el handler guarda y despacha los eventos (código `learning-platform-semana9-corregido`) |
| 15-oct | Separar el sistema en **varios contextos delimitados está bien: así se trabaja en DDD**. Su ejemplo usa uno solo (`learning`) porque es pequeño |
| 15-oct | **Swagger es requisito**. Se evalúa el código completo y lo que hizo cada integrante; se exponen algunos endpoints simples, sin sobreingeniería |

---

## 14. Feedback del profesor

Se llena después de cada revisión. Cada observación genera una tarea o un cambio registrado.

| Fecha | Observación | Acción | Responsable | Estado |
|---|---|---|---|---|
| 11-oct | Falta la capa de aplicación | Esperar la clase del 15-oct; mientras, controller con repositorio | Todos | ⏳ |

---

## 15. Registro de cambios

| Fecha | Quién (persona · agente) | Cambio | Motivo |
|---|---|---|---|
| 2026-10-08 | André · Claude Code | Versión inicial de las reglas generales | Inicio del desarrollo del backend para el APF2 |
| 2026-10-11 | André · Claude Code | Repo solo de API con Maven en la raíz (D-15 reemplaza D-11); `X-Student-Id` (D-16 reemplaza D-04); tests H2 (D-14); D-03 pendiente; D-05 decidida; secciones «Estado actual y quién sigue» y «Preguntas frecuentes»; calendario a la semana 11; guía técnica movida a `docs/guia-backend.md` | Indicaciones del profesor del 8 y 11-oct; límite de 32 KB de Codex |
| 2026-10-11 | André · Claude Code | B0 y B1 marcadas como entregadas en la tabla de estado; Fernando puede empezar B2 | PR #2 fusionado; B1 en el PR siguiente |
| 2026-10-15 | André · Claude Code | Indicaciones del profesor del 15-oct en la sección 13: CQRS, varios contextos delimitados, Swagger y criterio de evaluación | Clase y conversación con el profesor del 15-oct |
| 2026-10-15 | André · Claude Code | D-17: chat en tiempo real por WebSocket (STOMP) y su contrato en la guía, 8.6 | El diseño de `messaging` solo contemplaba REST; un chat debe entregar al instante |
| 2026-10-15 | André · Claude Code | Reglas para partir el trabajo en PR («Punto de PR») y archivos `docs/seguimiento/<modulo>.md` | Los agentes no sabían dónde cortar un PR y el avance solo constaba en commits y PR, ilegibles sin `gh` |
