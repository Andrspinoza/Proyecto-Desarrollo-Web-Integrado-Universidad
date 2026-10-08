# AGENTS.md — ¿Qué sale? (reglas generales)

Instrucciones para **todas las personas del equipo y todos los agentes de IA** (Codex, Claude
Code, Gemini/Antigravity, GitHub Copilot u otro) que trabajen en este repositorio.

> **Lectura obligatoria.** Antes de cada tarea, el agente relee este archivo y el `AGENTS.md`
> de la carpeta donde trabaja. Si encuentra una contradicción entre ellos, con el código o con
> lo que le piden, **se detiene y lo dice**; no elige por su cuenta.

| Archivo | Qué regula |
|---|---|
| `AGENTS.md` (este) | Producto, decisiones, equipo, git, commits, orden de trabajo, calendario |
| [`backend/AGENTS.md`](backend/AGENTS.md) | Arquitectura, molde del profesor, contextos, endpoints, base de datos |
| `frontend/AGENTS.md` | Se crea en la semana 11 |

**Cada regla vive en un solo `AGENTS.md`.** No se copian reglas de un archivo a otro.

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

### Fuentes de verdad, en orden

1. Indicaciones del profesor (sección 13 de este archivo) y su código de ejemplo (molde
   descrito en `backend/AGENTS.md`).
2. Modelo de dominio oficial: diagrama de clases y modelo relacional del *Avance de Proyecto
   Final 2* del curso Integrador I (reproducidos en `backend/AGENTS.md`).
3. Historias de usuario HU01–HU10 y requerimientos funcionales del mismo avance.
4. Comportamientos del prototipo presentado en el APF1: bloqueo, motivos de reporte, límites
   del plan gratuito, meetups para dos y grupales.

---

## 2. Decisiones

Estados: ✅ decidida · 🟡 propuesta, se puede cambiar · ❓ pendiente.

| ID | Estado | Decisión | Motivo |
|---|---|---|---|
| D-01 | ✅ | Spring Boot **4.1.1**, Java 21, Maven, Jar, Properties | Igual al molde del profesor |
| D-02 | ✅ | **Sin Lombok en `domain` ni en `application`**; solo en entidades JPA | Indicación del profesor: el dominio no depende de librerías externas |
| D-03 | 🟡 | Capa `application` **provisional** | El profesor la enseña en próximas clases; se ajustará a su versión |
| D-04 | 🟡 | Identidad por header **`X-Account-Id`** hasta ver Spring Security + JWT | Permite exponer y probar los endpoints ya |
| D-05 | 🟡 | Entre contextos, FKs reales en MySQL vía `@ManyToOne` en las entidades JPA; el dominio usa `Long` | Sin FKs, el EER Diagram de Workbench sale sin relaciones |
| D-06 | ✅ | **Swagger** (springdoc) como tecnología nueva propuesta; pruebas también en Postman y Thunder Client | Documenta y prueba la API en el navegador |
| D-07 | ✅ | Pull Requests fusionados con **«Create a merge commit»**, nunca *squash* | *Squash* aplasta los commits progresivos en uno solo |
| D-08 | ✅ | **Sin línea `Co-Authored-By` de IA** en los commits | El historial muestra solo a los integrantes |
| D-09 | ✅ | Producción en **AWS (Ubuntu)**; Contabo o DigitalOcean como staging opcional | Diagrama del profesor |
| D-10 | 🟡 | Tablero Kanban con issues de GitHub, si alcanza el tiempo | Buena práctica, no bloquea |
| D-11 | 🟡 | **Monorepo**: `backend/` y `frontend/` en este repositorio | Pendiente de confirmar con el profesor. Si pide dos repos, `backend/` se separa con `git subtree split --prefix=backend` conservando el historial |
| D-12 | ✅ | Código en inglés; comentarios, commits y documentación en español | El código del profesor está en inglés |
| D-13 | ✅ | La fachada de `subscriptions` solo **informa límites** del plan; cada contexto cuenta su propio uso | Ningún contexto lee tablas de otro |

**Cómo cambia una decisión:** no se borra. Su fila pasa a «reemplazada por D-NN», se agrega la
fila nueva y se anota en el Registro de cambios (sección 15). Si una decisión no está en esta
tabla, **no está decidida**: ni los chats, ni los planes locales de un agente, ni los archivos
puntero cuentan.

---

## 3. Equipo y responsabilidades

| Integrante | Rama | Contextos del backend | Transversal |
|---|---|---|---|
| **André Espinoza** | `dev-qa/andre` | `shared`, `iam`, `subscriptions` | Tech Lead: andamiaje, revisión de todos los PR, configuración del repo |
| **Jesús Fernando Espinoza** | `dev/jesus` | `meetups` | — |
| **Bruno Fernando Escobedo** (Fernando) | `dev/fernando` | `profiles` | MySQL, EER Diagram de Workbench, diccionario de datos, despliegue |
| **María Fernanda Licas** (Mafer) | `dev/mafer` | `communities`, `messaging` | Guion de exposición |
| **Ubes Manuel Páez** | `dev/ubes` | `moderation`, `connections` | QA: colecciones Postman/Thunder y casos de prueba |

🟡 El reparto puede ajustarse; si cambia, se actualiza esta tabla.

**Nadie toca el contexto de otro integrante**, ni `pom.xml` ni `backend/src/main/java/com/utp/quesale/shared/`
sin avisar antes al dueño. Si algo de otro contexto bloquea, se le avisa al dueño.

---

## 4. Onboarding — primera vez

Si alguien escribe algo como *«hola, soy Jesús»*, el agente lo guía por esta lista **un paso a
la vez**, esperando confirmación antes de seguir.

1. **Instalar:** Git, JDK 21 (`java -version` debe decir 21), Docker Desktop (o MySQL 8),
   MySQL Workbench y un IDE (IntelliJ IDEA o VS Code con Extension Pack for Java).
2. **Identidad de git — con tus datos reales** (el profesor revisa quién contribuyó):

   ```bash
   git config --global user.name "Tu Nombre"
   git config --global user.email "el-correo-de-tu-cuenta-de-github"
   ```

3. **Aceptar la invitación** al repositorio (es privado) y clonar:

   ```bash
   git clone https://github.com/Andrspinoza/Proyecto-Desarrollo-Web-Integrado-Universidad.git
   ```

4. **Crear tu rama personal una sola vez** (ejemplo para Jesús):

   ```bash
   git checkout -b dev/jesus
   git push -u origin dev/jesus
   ```

5. **Verificar el backend:** `cd backend`, `docker compose up -d`, `./mvnw spring-boot:run`
   (en Windows `mvnw.cmd`) y abrir http://localhost:8080/swagger-ui.html.

---

## 5. Git: lo ejecuta la persona, no el agente

El agente **no ejecuta** `git add`, `commit`, `push`, `pull`, `merge`, `rebase`, `reset`,
`checkout`, `stash` ni `clean`. Solo puede leer: `status`, `log`, `diff`, `show`, `branch`.
Si la persona le pide explícitamente ejecutar algo, lo hace solo para esa vez.

### Flujo diario (GitHub Flow con ramas personales)

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

---

## 6. Commits progresivos y punto de control — aplica a todos

El historial debe contar cómo se construyó el proyecto, paso a paso. **Nada de commits con
muchos cambios mezclados.**

**Reglas:**

- **Un commit = un cambio lógico que compila.** Se construye de abajo hacia arriba para que cada
  paso compile solo (el orden exacto por capa está en `backend/AGENTS.md`).
- Antes de cada commit: `./mvnw -q compile`, y `./mvnw test` si el paso trae tests.
- No se mezclan capas ni contextos en un commit; tampoco un refactor con una funcionalidad.
- Cada persona commitea **solo su trabajo, con su propia identidad**. Nadie commitea el contexto
  de otro.
- Mensajes en español, formato convencional, imperativo y sin tildes en el mensaje:

  ```text
  feat(meetups): agregar value object Capacity con validacion de cupo minimo
  ```

  Tipos: `feat`, `fix`, `test`, `refactor`, `docs`, `chore`, `build`. El ámbito es el contexto
  (`meetups`, `profiles`…), `shared`, `backend`, `frontend` o `agents`.
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

---

## 7. Orden de trabajo y dependencias

El dominio de cada contexto depende solo de `shared`, así que **los cinco arrancan su dominio
en paralelo**. Los cruces aparecen en dos puntos: la **persistencia** (FKs hacia las tablas de
estudiantes y universidades, D-05) y la **aplicación** (fachadas de otros contextos). Por eso
cada uno hace primero lo que no depende de nadie, y los dueños de piezas compartidas las
entregan antes.

### Ruta crítica — piezas que otros necesitan

| # | Pieza | Dueño | La necesitan | En `main` |
|---|---|---|---|---|
| B0 | Andamiaje: pom, wrapper, `application.properties`, `compose.yaml`, `shared` | André | Todos | **Jue 8 noche** |
| B1 | `UniversityJpaEntity`, `AccountJpaEntity` | André | `profiles`, `meetups`, `communities` | Vie 9 mañana |
| B2 | `StudentJpaEntity` | Fernando | Todos los demás contextos | **Vie 9 mediodía** |
| B3 | Interfaces de las fachadas (solo firmas) | Cada dueño | Quien las consume | Vie 9 |
| B4 | Eventos de dominio de `connections` y `meetups` | Ubes, Jesús | `messaging` | Dom 11 |

Las firmas de las fachadas ya están fijadas en `backend/AGENTS.md` (contrato primero): cada
consumidor programa contra ellas aunque la implementación llegue después.

### Secuencia por integrante

| Integrante | Orden | Espera a |
|---|---|---|
| **André** | B0 → `iam` dominio → B1 → fachadas de `iam` y `subscriptions` → `iam` completo → `subscriptions` completo → configurar el repo y revisar PRs | — |
| **Fernando** | `profiles` dominio → **B2 primero** → resto de persistencia → fachada de `profiles` → aplicación y endpoints → EER Diagram y diccionario | B0, B1 |
| **Ubes** | `moderation` dominio → `connections` dominio → fachadas de ambos → persistencia → aplicación y endpoints → colección Postman `00-setup` | B0; B2 para persistencia |
| **Jesús** | `meetups` dominio con tests (es el más grande, arranca ya) → persistencia con bloqueo pesimista → aplicación y endpoints | B0; B2 para persistencia; fachadas de `profiles` y `subscriptions` |
| **Mafer** | `communities` completo (independiente) → **después** `messaging` y sus manejadores de eventos | B0, B2; para `messaging`, fachadas de Ubes y B4 |

**Mientras esperas:** avanza tus tests de dominio, tus resources y assemblers (no dependen de
persistencia) o tu colección de Postman. **Nunca** toques el contexto de otro para adelantar.

### Orden de merge a `main`

`shared` → `iam` → `profiles` → el resto en paralelo.

---

## 8. Calendario

| Fecha | Meta |
|---|---|
| **Jue 8-oct** (noche) | André: B0 en `main`. Todos clonan y crean su rama |
| **Vie 9-oct** | Avance con feedback: andamiaje, Swagger funcionando, dominio y tests de los contextos avanzados, `iam` y `profiles` con persistencia |
| Sáb 10 – Mar 13 | Todos los endpoints, reglas entre contextos, notificaciones, búsqueda con JPQL, colecciones Postman |
| Mié 14-oct | Integración, EER Diagram, diccionario de datos, tag `v0.2.0` |
| **Jue 15-oct, 4 p. m.** | **APF2 — 20%** |
| Cuando lo enseñe | Spring Security + JWT reemplaza `X-Account-Id` (D-04) |
| Semana 11 | Se crea `frontend/` con Angular + SCSS |

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
| IA de chat sin acceso al repo | La persona pega este archivo, el de su carpeta y su tarea al empezar |
| Otra que se sume | Verificar qué archivo lee; si no lee `AGENTS.md`, crear su puntero y registrarlo |

Las reglas valen igual para cualquier herramienta. **La responsabilidad de cada commit es de la
persona**, no de la IA.

---

## 10. Este archivo es un documento vivo

Habrá correcciones: decisiones nuevas, cambios de rumbo y, sobre todo, el feedback del profesor.

- **Cualquier agente que cambie una regla, una decisión o el modelo** edita el `AGENTS.md`
  correspondiente y agrega una fila al **Registro de cambios** (sección 15).
- Ese cambio es **un commit propio**:
  `docs(agents): reemplazar D-04 por autenticacion JWT segun clase del 15-oct`.
- **Código y `AGENTS.md` nunca se contradicen.** Si un PR implementa algo distinto a lo escrito,
  en el mismo PR se corrige el código o se actualiza el `AGENTS.md`.
- Los archivos puntero (`CLAUDE.md`, `GEMINI.md`, `.github/...`) **nunca se editan con reglas**.

---

## 11. Kanban con issues de GitHub — 🟡 si alcanza el tiempo (D-10)

No bloquea el código.

- GitHub Project en vista Board: `Backlog · Por hacer · En progreso · En revisión · Hecho`.
- **Un issue por bloque** de la secuencia de commits, no por commit: «meetups — dominio»,
  «meetups — persistencia», «meetups — aplicación», «meetups — endpoints». Etiquetas
  `ctx:<contexto>` y `layer:<capa>`, asignado a su responsable.
- El PR lleva `Closes #NN` y el issue pasa a «Hecho» al fusionar.
- Si hay issue, el agente pide su número al empezar y trabaja solo dentro de lo que autoriza.
- Las observaciones del profesor se registran como issues con la etiqueta `feedback-profesor`.

---

## 12. Se presenta y se explica código

Desde el APF2 se expone **código real**. Por eso:

- Cada integrante explica **su contexto completo** y el recorrido de un endpoint por las cuatro
  capas: controller → assembler → servicio de aplicación → agregado (regla) → repositorio de
  dominio → adapter → mapper → entidad JPA → MySQL, y de vuelta como resource.
- El **guion de exposición** por contexto está en `backend/AGENTS.md`.
- Comentarios breves en español **solo** donde explican una invariante o por qué algo vive en
  esa capa.
- Nadie sube código que no puede explicar.

---

## 13. Indicaciones del profesor

Registro textual o resumido de lo que pide el docente. Es la primera fuente de verdad.

| Fecha | Indicación |
|---|---|
| 1-oct | No debe haber Lombok en el dominio: el dominio representa solo la lógica de negocio y, cuanto más puro, sin dependencias externas, mejor. Es lo que recomienda la metodología |
| 1-oct | JPA es una capa de abstracción: con JDBC hay que escribir cada método (`ClienteDAO`, `SQL_INSERT`…); con JPA se automatiza (`interface StudentJpaRepository extends JpaRepository<StudentEntity, Long>`) |
| 1-oct | La capa de infraestructura depende de la capa de dominio |
| 1-oct | El mapper existe para pasar de entidad JPA a objeto de dominio y viceversa |
| 1-oct | La inyección de dependencias se realiza en el constructor del adapter (`StudentRepositoryAdapter`) |
| 1-oct | Próximo avance: gran porcentaje de APIs y endpoints expuestos, todo el modelo de dominio expuesto, probar cada endpoint con la estructura indicada |
| 1-oct | Próximas clases: JPQL y terminar la capa de aplicación |
| Pendiente | ❓ ¿Un repositorio o dos (backend y frontend)? — preguntar en clase (D-11) |

---

## 14. Feedback del profesor

Se llena después de cada revisión. Cada observación genera una tarea o un cambio registrado.

| Fecha | Observación | Acción | Responsable | Estado |
|---|---|---|---|---|
| | | | | |

---

## 15. Registro de cambios

| Fecha | Quién (persona · agente) | Cambio | Motivo |
|---|---|---|---|
| 2026-10-08 | André · Claude Code | Versión inicial de las reglas generales | Inicio del desarrollo del backend para el APF2 |
