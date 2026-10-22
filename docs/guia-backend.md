# Guía técnica del backend de ¿Qué sale?

Reglas técnicas de la API. **Lectura obligatoria antes de escribir código.** Se leen **junto
con** el [`AGENTS.md`](../AGENTS.md) de la raíz, que contiene las decisiones (D-NN), el estado
actual, quién sigue, git, commits y el protocolo de punto de control. Nada de aquí se repite
allá ni al revés.

> Esta guía vive en `docs/` y no en un `AGENTS.md` porque Codex, por defecto, solo carga los
> primeros 32 KB de los `AGENTS.md`. Así el `AGENTS.md` raíz se carga completo y esta guía se
> lee cuando hace falta.

---

## 1. El molde del profesor

El profesor construyó un sistema de matrículas (`Student`, `Course`, `Enrollment`) como
ejemplo. **Se calca su estructura, su estilo y sus nombres**; solo cambia el dominio.

| Referencia | Qué aporta | Dónde conseguirla |
|---|---|---|
| `learning-platform-semana9` | Capas `domain` e `infrastructure` (molde vigente; igual a la semana 8) | Aula virtual |
| `sales` | Capa `interfaces`: controllers, resources, assemblers | Aula virtual, semana 2 |
| **Contexto `iam` de este repo** | **Ejemplo completo dentro del repo**: dominio → persistencia → endpoint → test con H2. **Úsalo como modelo para tu contexto** | `src/main/java/com/utp/quesale/iam/` y su test |

Los fragmentos de esta sección reproducen ese estilo para que nadie dependa de tener los
archivos. Si el profesor publica una versión nueva, se actualiza esta sección y su registro.

### 1.1 Agregado (dominio puro: sin Lombok, sin JPA)

```java
public class Student extends AbstractDomainAggregateRoot<Student> {

    private Long id;
    private final StudentRecordId recordId;
    private final String email;
    private boolean active;
    private final Instant createdAt;

    // Constructor de creación: estado inicial válido
    public Student(String email) {
        this(null, new StudentRecordId(), email, true, Instant.now());
    }

    // Constructor de rehidratación: lo usa SOLO el mapper; nunca registra eventos
    public Student(Long id, StudentRecordId recordId, String email, boolean active, Instant createdAt) {
        if (recordId == null) throw new IllegalArgumentException("Student record id is required");
        if (createdAt == null) throw new IllegalArgumentException("Created at is required");
        this.id = id;
        this.recordId = recordId;
        this.email = validEmail(email);
        this.active = active;
        this.createdAt = createdAt;
    }

    // Comportamiento con nombre de negocio y su regla
    public void deactivate() {
        if (!active) throw new IllegalStateException("Student is already inactive");
        active = false;
    }

    private static String validEmail(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Email is required");
        return value.trim();
    }

    // Getters escritos a mano; colecciones con Collections.unmodifiableList(...)
    public Long getId() { return id; }
    public boolean isActive() { return active; }
}
```

### 1.2 Value object, enum, evento, excepción y contrato de repositorio

```java
public record StudentRecordId(String studentRecordId) {
    public StudentRecordId() { this(UUID.randomUUID().toString()); }
    public StudentRecordId {
        if (studentRecordId == null || studentRecordId.isBlank())
            throw new IllegalArgumentException("Student record id cannot be null or empty");
        UUID.fromString(studentRecordId); // valida el formato
    }
}

public enum EnrollmentStatus { REQUESTED, CONFIRMED, REJECTED, CANCELLED }

public record EnrollmentConfirmedEvent(Long enrollmentId, Long studentId, Long courseId, Instant occurredAt) {}

public class CourseNotFoundException extends RuntimeException {
    public CourseNotFoundException(Long courseId) {
        super(String.format("Course with ID %s not found", courseId));
    }
}

public interface CourseRepository {          // en domain/repositories: solo el contrato
    Course save(Course course);
    Course update(Course course);
    Optional<Course> findById(Long id);
    List<Course> findAll();
    boolean existsById(Long id);
}
```

### 1.3 Infraestructura: entidad JPA, repositorio Spring Data, mapper y adapter

```java
@Getter @Setter @NoArgsConstructor            // Lombok SOLO aquí
@Entity
@Table(name = "students", uniqueConstraints = @UniqueConstraint(name = "uk_student_email", columnNames = "email"))
public class StudentJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String email;
    @Column(nullable = false)
    private boolean active;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}

public interface StudentJpaRepository extends JpaRepository<StudentJpaEntity, Long> {
    boolean existsByEmailIgnoreCase(String email);   // consultas derivadas por nombre
}

@Component
public class StudentPersistenceMapper {               // entidad JPA <-> objeto de dominio
    public Student toDomain(StudentJpaEntity e) { return new Student(e.getId(), /* ... */ e.getCreatedAt()); }
    public StudentJpaEntity toEntity(Student s) {
        StudentJpaEntity e = new StudentJpaEntity();
        e.setId(s.getId());
        /* ... */
        return e;
    }
}

@Repository
public class StudentRepositoryAdapter implements StudentRepository {   // implementa el contrato del dominio
    private final StudentJpaRepository repository;
    private final StudentPersistenceMapper mapper;

    // Inyección de dependencias por constructor (indicación del profesor)
    public StudentRepositoryAdapter(StudentJpaRepository repository, StudentPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Student save(Student student) {
        return mapper.toDomain(repository.save(mapper.toEntity(student)));
    }
}
```

Relación entre entidades JPA: el adapter carga la entidad relacionada y el mapper la asigna,
como hace el profesor con `Enrollment`:

```java
StudentJpaEntity student = studentRepo.findById(enrollment.getStudentId())
        .orElseThrow(() -> new IllegalArgumentException("Student not found: " + enrollment.getStudentId()));
EnrollmentJpaEntity entity = mapper.toEntity(enrollment, student, course);
```

Colecciones hijas: `@OneToMany(mappedBy = ..., cascade = CascadeType.ALL, orphanRemoval = true)`
y un método `replaceXxxItems(...)` en la entidad padre que fija la referencia inversa.

### 1.4 Capa `interfaces` (estilo `sales`)

**Hoy, sin capa de aplicación** (indicación del profesor: el endpoint va por el adapter). El
controller recibe por constructor la **interfaz del repositorio de dominio**; Spring le inyecta
el adapter. El controller crea el agregado y lo guarda: **las reglas siguen en el agregado**.

```java
public record CreateMeetupResource(
        @NotBlank @Size(max = 100) String title,
        @NotNull MeetupType type,
        @NotNull @Future LocalDateTime scheduledAt,
        @NotNull @Min(2) @Max(50) Integer maxCapacity) {}

public class MeetupResourceAssembler {
    public static MeetupResource toResourceFromEntity(Meetup meetup) { /* ... */ }
}

@RestController
@RequestMapping("/api/v1/meetups")
@Tag(name = "Meetups")
public class MeetupsController {
    private final MeetupRepository meetupRepository;    // interfaz del dominio; Spring inyecta el adapter
    private final StudentRepository studentRepository;

    public MeetupsController(MeetupRepository meetupRepository, StudentRepository studentRepository) {
        this.meetupRepository = meetupRepository;
        this.studentRepository = studentRepository;
    }

    @PostMapping
    @Operation(summary = "Crear un meetup")
    public ResponseEntity<MeetupResource> create(@RequestHeader(RequestHeaders.STUDENT_ID) Long studentId,
                                                 @Valid @RequestBody CreateMeetupResource resource) {
        Student organizer = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));
        Meetup saved = meetupRepository.save(new Meetup(organizer.getId(), organizer.getUniversityId(), /* ... */));
        return ResponseEntity.created(URI.create("/api/v1/meetups/" + saved.getId()))
                .body(MeetupResourceAssembler.toResourceFromEntity(saved));
    }
}
```

**Cuando el profesor enseñe la capa de aplicación** (clase del 15-oct), la coordinación sale del
controller y pasa a un servicio de aplicación: el controller llamará al servicio y el servicio al
repositorio. El dominio y la persistencia **no cambian**. Ver sección 4, `application`.

---

## 2. Stack

Spring Boot **4.1.1** · Java 21 · Maven con Maven Wrapper · Packaging Jar · Properties ·
`groupId` `com.utp.quesale` · `artifactId` `quesale-api` · paquete raíz `com.utp.quesale`.

| Dependencia | Origen | Para qué |
|---|---|---|
| `spring-boot-starter-webmvc` | Profesor | API REST (en Boot 4 **no** es `-web`) |
| `spring-boot-starter-data-jpa` | Profesor | JPA / Hibernate |
| `mysql-connector-j` (runtime) | Profesor | Driver MySQL |
| `lombok` (optional) | Profesor | Solo entidades JPA |
| `spring-boot-devtools` (runtime) | Profesor | Recarga en desarrollo |
| `spring-boot-starter-data-jpa-test`, `spring-boot-starter-webmvc-test` (test) | Profesor | Pruebas |
| `spring-boot-starter-validation` | `sales` | `@Valid`, `@NotBlank`, `@Size` en resources |
| `org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.1` | Equipo (D-06) | Swagger UI; la 3.1.1 está compilada contra Boot 4.1 |
| `com.h2database:h2` (test) | Profesor (D-14) | Base en memoria para las pruebas de persistencia |
| `spring-security-crypto` | Equipo | Solo `BCryptPasswordEncoder` para `password_hash`. **Se agrega cuando André implemente `Account`**; no activa Spring Security |

El `maven-compiler-plugin` lleva `lombok` en `annotationProcessorPaths`, igual que el pom del
profesor. **No se agregan ni actualizan dependencias** sin avisar a André y registrarlo.

`src/main/resources/application.properties` (mismo esquema que el profesor):

```properties
spring.application.name=quesale-api
spring.datasource.url=jdbc:mysql://localhost:${DB_PORT:3306}/quesale?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:root}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.jdbc.time_zone=UTC
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs
```

**Usuario y contraseña de MySQL:** por defecto `root`/`root`, como el ejemplo del profesor. Si
tu MySQL tiene otra contraseña, **no edites el archivo**: pásala por variable de entorno al
arrancar. En PowerShell:

```powershell
$env:DB_PASSWORD = "tu-contraseña"; ./mvnw spring-boot:run
```

---

## 3. Estructura de paquetes

```text
com.utp.quesale
├── QueSaleApplication.java
├── shared/
│   ├── domain/model/aggregates/AbstractDomainAggregateRoot.java
│   ├── domain/exceptions/        ResourceNotFoundException, BusinessRuleViolationException,
│   │                             ForbiddenOperationException
│   ├── interfaces/rest/          GlobalExceptionHandler, ErrorResource, RequestHeaders
│   └── infrastructure/documentation/OpenApiConfiguration.java
└── <contexto>/                   iam · profiles · connections · meetups · messaging ·
    │                             communities · moderation · subscriptions
    ├── domain/
    │   ├── aggregates/           raíces de agregado
    │   ├── entities/             entidades hijas
    │   ├── valueobjects/         records y enums (el profesor escribe "valueobjets"; nosotros no)
    │   ├── events/               records de eventos
    │   ├── exceptions/           una por caso
    │   ├── repositories/         interfaces (contratos)
    │   └── services/             servicios de dominio (reglas que no pertenecen a una entidad)
    ├── application/              CQRS del profesor (ver sección 4); el modelo es `iam`
    │   ├── commands/             un record por operación que modifica
    │   ├── queries/              un record por consulta
    │   ├── dtos/                 resultado que devuelven los handlers
    │   ├── assemblers/           del agregado al DTO, sin depender de HTTP
    │   ├── handlers/
    │   │   ├── commands/         un @Service por agregado, con un handle(...) por command
    │   │   └── queries/          un @Service por query
    │   ├── eventhandlers/        reaccionan a eventos de otros contextos (@EventListener)
    │   └── acl/                  implementación de la fachada del contexto
    ├── infrastructure/persistence/jpa/{entities,repositories,mappers,adapters}
    └── interfaces/
        ├── controllers/
        ├── resources/
        ├── assemblers/
        └── acl/                        interfaz de la fachada (contrato público del contexto)
```

Cada carpeta se crea **cuando se implementa** algo que va en ella; no se crean carpetas vacías.

---

## 4. Reglas por capa

### `domain`

- **Cero Lombok, cero JPA, cero Spring Web** (D-02). La única dependencia de framework es la
  herencia de `AbstractDomainAggregateRoot` del molde del profesor; se reconoce como
  acoplamiento docente y no se agregan otros.
- Dos constructores por agregado: **creación** y **rehidratación** (este último sin eventos).
- Las reglas viven en métodos con nombre de negocio (`confirm()`, `cancel(requesterId)`), nunca
  en setters. No hay setters públicos.
- Validaciones: `IllegalArgumentException` para datos inválidos, `IllegalStateException` para
  transiciones no permitidas, o una excepción de dominio que extienda las de `shared`.
- IDs técnicos `Long`. Entre agregados se referencian **por `Long`**, nunca por objeto.
- Colecciones expuestas con `Collections.unmodifiableList/Set`.

### `infrastructure`

- Lombok permitido solo en `persistence/jpa/entities`.
- Depende del dominio, nunca al revés. Implementa los contratos de `domain/repositories`.
- Puede referenciar entidades JPA de **otro contexto** con `@ManyToOne(fetch = FetchType.LAZY)`
  para tener FKs reales (D-05). Es el único lugar donde un contexto importa algo de otro, además
  de fachadas y eventos.
- Restricciones de la base de datos declaradas en la entidad: `@UniqueConstraint`, `nullable`,
  `length` y, para los `CHECK`, `@Table(check = @CheckConstraint(name = "...", constraint = "..."))`
  (ver `StudentJpaEntity` de la demo o la sección 8).
- **Cada adapter tiene su prueba `@DataJpaTest` sobre H2** (D-14): guarda un objeto de dominio,
  lo vuelve a leer y verifica que no se perdió nada. Modelo: el test de `iam` del repo.

### `application` — CQRS del profesor (clase del 15-oct)

> **`iam` es el modelo** (B5): copia su estructura en `src/main/java/com/utp/quesale/iam/application/`
> y sus pruebas, sin inventar otra. Primero termina el dominio y la persistencia con test H2 de tu
> módulo; después escribes su capa de aplicación. Esta sección reemplaza la idea anterior de
> `XCommandService` y `application/internal`, que ya **no se usa**.

Estructura, tomada del código `learning-platform-semana9-corregido` del profesor:

```text
application/
├── commands/        un record por operación que modifica (CreateStudentCommand)
├── queries/         un record por consulta (GetStudentQuery)
├── dtos/            el resultado que devuelve el handler (StudentDto)
├── assemblers/      pasa del agregado al DTO, sin depender de HTTP
└── handlers/
    ├── commands/    una clase @Service por agregado, con un handle(...) por command
    └── queries/     una clase @Service por query, con @Transactional(readOnly = true)
```

- El **handler de command** carga el agregado con el repositorio del dominio, ejecuta la regla,
  guarda y despacha los eventos con `DomainEventDispatcher`. Lleva `@Transactional`.
- Las **reglas del negocio** siguen en el agregado; el handler solo coordina.
- Los eventos se publican **después del commit**; el despachador está en `shared`.
- El **controller** arma el command desde el resource, llama al handler y arma el resource de
  respuesta **a partir del DTO**. El `@Transactional` del controller desaparece.
- Errores: se mantienen nuestras excepciones propias (`XNotFoundException` → 404), no
  `IllegalArgumentException`.
- Inyección por constructor. Sin Lombok (D-02).

### `interfaces`

- Rutas `/api/v1/...`, en plural y en inglés.
- **Nunca se devuelve una entidad de dominio ni una entidad JPA**: siempre un `resource`
  (`record`) construido por un assembler.
- Resources de entrada con `jakarta.validation` y `@Valid` en el controller.
- Respuestas: `201 Created` con `Location` al crear, `200` al consultar o modificar, `204` sin
  cuerpo. Cada controller lleva `@Tag` y cada endpoint `@Operation(summary = "...")`.
- La identidad provisional llega por `@RequestHeader(RequestHeaders.STUDENT_ID) Long studentId`
  (D-04). El controller no decide permisos: le pasa el `studentId` al método del agregado
  (`meetup.cancel(studentId)`) y **el agregado** decide.
- Mientras no exista la capa de aplicación, los endpoints que **modifican** un agregado ya
  guardado llevan `@Transactional` en el método del controller (concesión temporal; pasa al
  servicio de aplicación cuando el profesor la enseñe).

### Entre contextos

Un contexto **nunca importa el dominio de otro**. Solo puede usar:

1. La **fachada** del otro contexto (`interfaces/acl`), con tipos primitivos (sección 6).
2. Sus **eventos** (`domain/events`), que son su lenguaje publicado, solo en
   `application/eventhandlers`.
3. Sus **entidades JPA**, solo desde `infrastructure` y solo para FKs (D-05).

---

## 5. Correcciones deliberadas al molde

El ejemplo del profesor tiene dos detalles que **no** se copian. Si el profesor pregunta, esta es
la explicación:

1. **Eventos de creación con `id` nulo.** En el molde, el constructor de creación registra
   `StudentCreatedEvent(id, ...)` cuando el `id` todavía es `null`, porque lo asigna MySQL al
   guardar. → Nuestros constructores de creación **no** registran eventos. Tras guardar, el
   servicio de aplicación llama a `saved.markAsCreated()`, que registra el evento con el `id`
   real.
2. **Los eventos nunca se publican.** Spring Data publica los eventos del objeto que recibe
   `save()`, y ese objeto es la entidad JPA, no el agregado. → Se agrega `pullDomainEvents()` a
   `AbstractDomainAggregateRoot` y el servicio de aplicación los publica con
   `ApplicationEventPublisher`.

```java
public abstract class AbstractDomainAggregateRoot<T extends AbstractDomainAggregateRoot<T>>
        extends AbstractAggregateRoot<T> {

    protected void registerDomainEvent(Object event) {
        super.registerEvent(event);
    }

    @Override
    protected Collection<Object> domainEvents() {
        return super.domainEvents();
    }

    @Override
    protected void clearDomainEvents() {
        super.clearDomainEvents();
    }

    // Agregado por el equipo: el servicio de aplicación publica los eventos tras guardar
    public List<Object> pullDomainEvents() {
        List<Object> events = List.copyOf(domainEvents());
        clearDomainEvents();
        return events;
    }
}
```

Uso en un servicio de aplicación (cuando exista esa capa; hasta entonces los eventos solo se
declaran y se prueban en el dominio, nadie los escucha todavía):

```java
@Override
@Transactional
public Long handle(CreateMeetupCommand command) {
    Long organizerId = profiles.studentIdOfAccount(command.accountId())
            .orElseThrow(() -> new ForbiddenOperationException("Account has no student profile"));
    Meetup saved = meetupRepository.save(new Meetup(organizerId, /* ... */));
    saved.markAsCreated();
    saved.pullDomainEvents().forEach(eventPublisher::publishEvent);
    return saved.getId();
}
```

---

## 6. `shared` y contratos entre contextos

### 6.1 Errores

| Excepción | HTTP |
|---|---|
| `ResourceNotFoundException` (base de todos los `XNotFoundException`) | 404 |
| `IllegalArgumentException`, `MethodArgumentNotValidException`, `HttpMessageNotReadableException`, `MissingRequestHeaderException` | 400 |
| `ForbiddenOperationException` (p. ej. no eres el organizador) | 403 |
| `BusinessRuleViolationException`, `IllegalStateException`, `DataIntegrityViolationException`, `PessimisticLockingFailureException` | 409 |

`GlobalExceptionHandler` (`@RestControllerAdvice`) responde siempre con:

```java
public record ErrorResource(int status, String error, String message, String path, Instant timestamp) {}
```

### 6.2 Identidad provisional (D-04)

`RequestHeaders.STUDENT_ID = "X-Student-Id"`: en Swagger o Postman se envía el `id` del
estudiante que «hace» la petición. Es lo más simple mientras no exista login. Cuando el profesor
enseñe Spring Security + JWT, el `studentId` saldrá del token y este header desaparece; el
dominio no cambia. Las operaciones de administrador se agregan con `iam` completo
(`IamContextFacade.isAdmin`).

**Endpoints de administrador (por ejemplo, revisar o descartar un reporte).** Hasta tener `Account`
y JWT, el valor de `X-Student-Id` se usa como `adminAccountId` y **no se valida el rol**. Dilo en
Swagger y en Postman como provisional. Con JWT, el id sale del token y se valida el rol.

### 6.3 Fachadas — firmas fijas (contrato primero)

Cada dueño publica la **interfaz** apenas termina su dominio (B3), aunque la implementación
llegue después. Cambiar una firma exige avisar a quienes la usan y registrarlo.

```java
// iam/interfaces/acl — André
public interface IamContextFacade {
    // Publicadas (21-oct), las usa profiles para crear estudiantes
    boolean isActiveUniversity(Long universityId);
    boolean universityAcceptsEmail(Long universityId, String email);
    // Se agregan cuando exista Account
    // boolean existsAccount(Long accountId);
    // boolean isAdmin(Long accountId);
    // boolean isActiveAndVerified(Long accountId);
    // Optional<Long> universityOfAccount(Long accountId);
}

// profiles/interfaces/acl — Fernando
public interface ProfilesContextFacade {
    boolean existsStudent(Long studentId);
    Optional<Long> studentIdOfAccount(Long accountId);
    Optional<Long> universityOfStudent(Long studentId);
}

// connections/interfaces/acl — Ubes
public interface ConnectionsContextFacade {
    boolean areConnected(Long studentId, Long otherStudentId);
}

// moderation/interfaces/acl — Ubes
public interface ModerationContextFacade {
    boolean isBlocked(Long studentId, Long otherStudentId);   // en cualquier sentido
}

// subscriptions/interfaces/acl — André. -1 significa "sin límite" (D-13)
public interface SubscriptionsContextFacade {
    int dailyConnectionRequestLimit(Long studentId);
    int dailyMeetupRequestLimit(Long studentId);
    int activeOrganizedMeetupLimit(Long studentId);
}
```

Cada contexto **cuenta sus propios datos** y compara contra el límite. Ejemplo: `connections`
cuenta las solicitudes que el estudiante envió hoy y las compara con
`dailyConnectionRequestLimit`.

---

## 7. Modelo de dominio oficial

Viene del diagrama de clases y del modelo relacional del *Avance de Proyecto Final 2* de
Integrador I. Equivalencias con el código:

| Diagrama | Código | Diagrama | Código |
|---|---|---|---|
| Cuenta | `Account` | Rol (ESTUDIANTE, ADMINISTRADOR) | `Role` (STUDENT, ADMIN) |
| Universidad | `University` | Visibilidad (PUBLICO, PRIVADO) | `Visibility` (PUBLIC, PRIVATE) |
| Estudiante | `Student` | TipoMeetup (ACADEMICO, SOCIAL) | `MeetupType` (ACADEMIC, SOCIAL) |
| Perfil | `Profile` | EstadoMeetup (PROGRAMADO, CANCELADO, REALIZADO) | `MeetupStatus` (SCHEDULED, CANCELLED, FINISHED) |
| Interés | `Interest` | EstadoParticipacion (SOLICITADA, CONFIRMADA, RETIRADA) | `ParticipationStatus` (REQUESTED, CONFIRMED, WITHDRAWN) |
| Habilidad | `Skill` | EstadoConexion (PENDIENTE, ACEPTADA, RECHAZADA) | `ConnectionStatus` (PENDING, ACCEPTED, REJECTED, CANCELLED) |
| Conexión | `Connection` | Participación | `Participation` |
| Meetup | `Meetup` | Mensaje, Notificación, Reporte | `Message`, `Notification`, `Report` |

Restricciones de la Fig. 5 que se respetan: `UNIQUE (student_id, meetup_id)` en
`participations`, `CHECK (max_capacity >= 2)`, `CHECK (requester_id <> recipient_id)`,
`password_hash` guarda el resultado de BCrypt y nunca la contraseña. El diagrama marca como
decisión a validar que el organizador cuente como participante confirmado: **se adopta** (el
organizador ocupa un cupo).

---

## 8. Contextos

Para cada contexto: agregados, reglas que **deben tener test** y endpoints (todos bajo
`/api/v1`). Las tablas van en inglés, plural y `snake_case`.

> **Alcance para el avance del 15-oct y la entrega de la semana 11.** El profesor pidió *todos
> los agregados con su persistencia* y endpoints expuestos. Prioridad por agregado:
> 1) dominio + test; 2) persistencia + test H2; 3) **crear, obtener por id y listar**; 4) una o
> dos acciones clave (p. ej. `confirm`, `cancel`, `accept`). El resto de endpoints de las tablas
> se agrega después, cuando exista la capa de aplicación. Lo marcado *(admin)* se expone sin
> verificar rol hasta tener `Account`.

### 8.1 `iam` — André

- **`University`**: `name` (único, ≤120), `emailDomain` (único, minúsculas, p. ej. `utp.edu.pe`),
  `active`. `accepts(String email)` indica si el correo pertenece a su dominio.
- **`Account`**: `email` (VO `InstitutionalEmail`, formato válido y en minúsculas),
  `passwordHash`, `role`, `universityId`, `verified`, `active`, `createdAt`. Métodos `verify()`,
  `activate()`, `deactivate()`, `hasRole(Role)`, `markAsCreated()`.
- **Servicio de dominio** `PasswordHasher` (interfaz en `domain/services`), implementado en
  `infrastructure/security/BCryptPasswordHasher` con `spring-security-crypto`.
- **Reglas con test:** el correo debe ser del dominio de su universidad; un correo no se
  registra dos veces; no se verifica dos veces; una cuenta inactiva no opera.
- **Datos iniciales:** `BootstrapDataInitializer` (`CommandLineRunner`) crea la universidad UTP
  y una cuenta administradora si la base está vacía, con valores de `quesale.bootstrap.*`.

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/universities` | Registrar universidad *(admin)* |
| GET | `/universities` · `/universities/{id}` | Listar · obtener |
| PUT | `/universities/{id}` | Actualizar *(admin)* |
| POST | `/accounts` | Registrarse: correo institucional y contraseña |
| GET | `/accounts/{id}` | Obtener (sin `passwordHash`) |
| PATCH | `/accounts/{id}/verify` | Verificar correo *(simulado hasta tener envío de correos)* |
| PATCH | `/accounts/{id}/activate` · `/deactivate` | *(admin)* |

### 8.2 `profiles` — Fernando

- **`Interest`** y **`Skill`**: catálogos con `name` único (≤40).
- **`Student`** (raíz): `recordId` (VO `StudentRecordId`, UUID), `accountId` (único),
  `universityId`, `fullName` (VO `FullName`: nombres y apellidos ≤60), `createdAt` y su entidad
  hija **`Profile`** (0..1): `career` (VO `Career` ≤80), `cycle` (VO `StudyCycle` 1–10),
  `description` (≤500), `visibility`, `interestIds` y `skillIds` (máximo 15 cada uno, sin
  repetidos).
- **Mientras `Account` no exista** (lo hace André después de `University`), `Student` se crea con
  `universityId` y su correo institucional, como en la demo; `accountId` se agrega cuando llegue
  `Account`. Lo único que bloquea a los demás es **B2: `StudentJpaEntity` con FK a
  `universities`**.
- Métodos: `createProfile(...)`, `updateProfile(...)`, `addInterest`, `removeInterest`,
  `addSkill`, `removeSkill`, `isVisibleTo(viewerStudentId, connected)`.
- **Servicio de dominio `AffinityCalculator`**: compara dos perfiles y devuelve el VO
  `AffinityScore` (0–100) con los criterios que coincidieron: intereses comunes 50 %,
  habilidades comunes 20 %, misma carrera 15 %, ciclo a ±1 15 %. Nunca muestra un porcentaje sin
  explicar qué coincide.
- **Reglas con test:** un estudiante por cuenta; ciclo 1–10; un perfil privado solo es visible
  completo para su dueño o sus conexiones; topes de intereses y habilidades.
- **B2:** `StudentJpaEntity` se entrega primero; las demás entidades JPA la referencian.

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/students` | Crear estudiante para la cuenta actual |
| GET | `/students/{id}` | Obtener; si es privado y no hay conexión, versión reducida (nombre, carrera, ciclo) |
| GET | `/students?career&cycle&interestId&skillId` | Buscar en la misma universidad, sin bloqueados (JPQL) |
| PUT | `/students/{id}/profile` | Crear o actualizar el perfil *(solo el dueño)* |
| POST · DELETE | `/students/{id}/profile/interests/{interestId}` | Agregar · quitar interés |
| POST · DELETE | `/students/{id}/profile/skills/{skillId}` | Agregar · quitar habilidad |
| GET | `/students/{id}/recommendations` | Estudiantes afines con su afinidad y lo que comparten |
| POST · GET | `/interests` · `/skills` | Catálogos (crear: admin) |

### 8.3 `connections` — Ubes

- **`Connection`**: `requesterId`, `recipientId`, `status`, `createdAt`, `respondedAt`.
  - `accept(byStudentId)`: solo el destinatario y solo si está PENDING → ACCEPTED; registra
    `ConnectionAcceptedEvent` (el *match*, «¡Hay química!»).
  - `reject(byStudentId)`: solo el destinatario, PENDING → REJECTED.
  - `cancel(byStudentId)`: solo el solicitante, PENDING → CANCELLED.
  - `markAsCreated()` registra `ConnectionRequestedEvent`.
- **Reglas con test:** nadie se conecta consigo mismo; solo el destinatario acepta o rechaza;
  solo se responde una solicitud pendiente.
- **Reglas en el servicio de aplicación:** misma universidad; no bloqueados; sin otra conexión
  PENDING o ACCEPTED entre el mismo par en cualquier sentido; límite diario del plan.

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/connections` | Enviar solicitud |
| PATCH | `/connections/{id}/accept` · `/reject` · `/cancel` | Responder o cancelar |
| GET | `/connections?status=` | Mis conexiones |
| GET | `/connections/received` · `/connections/sent` | Pendientes recibidas · enviadas |

### 8.4 `meetups` — Jesús

- **`Meetup`** (raíz): `organizerId`, `universityId`, `title` (≤100), `description` (≤500),
  `type`, `visibility`, `scheduledAt` (`LocalDateTime`), `place` (VO `Place`: nombre ≤120,
  dirección ≤200, distrito ≤60), `capacity` (VO `Capacity`: máximo 2–50), `status` y su entidad
  hija **`Participation`**: `studentId`, `status`, `registeredAt`.
  - Al crearse, el organizador queda como participación **CONFIRMED** (ocupa un cupo).
  - `requestParticipation(studentId)`, `confirmParticipation(studentId, requesterId)`,
    `invite(studentId, requesterId)`, `acceptInvitation(studentId)`,
    `declineInvitation(studentId)`, `withdraw(studentId)`, `modify(..., requesterId)`,
    `cancel(requesterId)`, `finish(requesterId, now)`, `availableSpots()`.
  - Especificación `MeetupIsJoinable`: programado, con cupo y fecha futura.
- **Visibilidad** (enum `MeetupVisibility`). Regla de sentido común: **nadie recibe un aviso que
  el organizador no decidió mandar**. Una cita es un meetup `PRIVATE` con cupo 2.

  | Valor | Quién lo ve | Cómo se entra |
  |---|---|---|
  | `PUBLIC` | Todos los de su universidad | Solicitar y que el organizador confirme, o por invitación |
  | `PRIVATE` | Solo el organizador y sus invitados | **Solo por invitación**; solicitar está prohibido |

  Para esta entrega solo existen `PUBLIC` y `PRIVATE`. La visibilidad `CONNECTIONS` (solo para las
  conexiones del organizador) se agrega **después**, porque necesita `connections`.
- **Estados de participación** (`ParticipationStatus`): `REQUESTED`, `INVITED`, `CONFIRMED`,
  `DECLINED`, `WITHDRAWN`. Solo `CONFIRMED` ocupa cupo; aceptar una invitación también respeta el cupo.
- **Eventos** (records en `domain/events`), con el **estilo del profesor**: solo ids y
  `Instant occurredAt`, como `EnrollmentConfirmedEvent(enrollmentId, studentId, courseId, occurredAt)`.
  Cada evento se registra **dentro del método que lo provoca**. `MeetupCreatedEvent` se registra en
  el constructor de creación, igual que el profesor: no notifica a nadie, así que no hace falta
  `markAsCreated()`. El `meetupId` es el `referenceId` de la notificación en `messaging`:

  | Evento | Campos | Lo registra | Se notifica a |
  |---|---|---|---|
  | `MeetupCreatedEvent` | `meetupId`, `organizerId`, `occurredAt` | Constructor de creación | Nadie |
  | `ParticipationRequestedEvent` | `meetupId`, `organizerId`, `studentId`, `occurredAt` | `requestParticipation` | `organizerId` |
  | `ParticipationConfirmedEvent` | `meetupId`, `organizerId`, `studentId`, `occurredAt` | `confirmParticipation` | `studentId` |
  | `ParticipationInvitedEvent` | `meetupId`, `organizerId`, `studentId`, `occurredAt` | `invite` | `studentId` |
  | `InvitationAcceptedEvent` | `meetupId`, `organizerId`, `studentId`, `occurredAt` | `acceptInvitation` | `organizerId` |
  | `InvitationDeclinedEvent` | `meetupId`, `organizerId`, `studentId`, `occurredAt` | `declineInvitation` | `organizerId` (a nadie más) |
  | `MeetupCancelledEvent` | `meetupId`, `organizerId`, `participantIds`, `occurredAt` | `cancel` | Cada id de `participantIds` |

  `participantIds` son quienes tienen participación `REQUESTED`, `INVITED` o `CONFIRMED`, **sin el
  organizador** (`List.copyOf`).
- **Excepciones**, con el **estilo del profesor**: pocas y propias solo para «no encontrado»; las
  reglas usan `IllegalStateException` (el manejador devuelve 409) y los datos inválidos
  `IllegalArgumentException` (400).

  | Excepción | Hereda de | Cuándo |
  |---|---|---|
  | `MeetupNotFoundException` | `ResourceNotFoundException` (404) | El meetup no existe |
  | `ParticipationNotFoundException` | `ResourceNotFoundException` (404) | Confirmar, aceptar, rechazar o retirar sin participación previa |
  | `MeetupAccessDeniedException` | `ForbiddenOperationException` (403) | Quien no es el organizador intenta modificar, cancelar, confirmar, invitar o finalizar; un estudiante de otra universidad solicita, recibe o acepta una invitación |
  | `IllegalStateException` | — (409) | Cupo lleno, meetup no disponible, ya participa, solicitar en un meetup `PRIVATE`, el organizador se retira, nuevo cupo menor que los confirmados |

- **Reglas con test:** nunca se supera el cupo; cancelado, finalizado o pasado no acepta
  participantes; solo el organizador modifica, cancela, confirma, invita o finaliza; en `PRIVATE`
  nadie puede solicitar; solo el invitado acepta o rechaza su invitación; fecha futura al crear y
  al reprogramar (no al rehidratar); el nuevo cupo no puede ser menor que los confirmados; nadie
  participa dos veces; el organizador no se retira de su propio meetup.
- **Concurrencia por el último cupo:** las operaciones que confirman o aceptan cargan el meetup con
  `findByIdForUpdate`, implementado en `MeetupJpaRepository` con
  `@Lock(LockModeType.PESSIMISTIC_WRITE)` y `@Query("select m from MeetupJpaEntity m where m.id = :id")`.
  Además, `UNIQUE (meetup_id, student_id)` en `participations`.
- El participante debe pertenecer a la misma universidad del meetup; el handler valida esta regla
  con `ProfilesContextFacade` al solicitar participación, invitar y aceptar una invitación.
- **Después de la entrega** (necesitan otros módulos): solo se invita a **conexiones** y nunca a alguien bloqueado; la visibilidad `CONNECTIONS`; y los
  límites del plan, que se revisan en la capa de aplicación.

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/meetups` | Crear, con `visibility` (una cita: `PRIVATE` con cupo 2) |
| GET | `/meetups?type&status&from&to` | Listar los **públicos** de mi universidad |
| GET | `/meetups/mine` | Los que organizo, en los que participo o a los que me invitaron |
| GET · PUT | `/meetups/{id}` | Obtener · modificar *(organizador)*; un `PRIVATE` solo lo ven organizador e invitados |
| PATCH | `/meetups/{id}/cancel` · `/finish` | *(organizador)* |
| POST | `/meetups/{id}/participations` | Solicitar participar (solo `PUBLIC`) |
| PATCH | `/meetups/{id}/participations/{studentId}/confirm` | Confirmar *(organizador)* |
| POST | `/meetups/{id}/invitations` | Invitar a un estudiante *(organizador)* |
| PATCH | `/meetups/{id}/invitations/me/accept` · `/decline` | Aceptar o rechazar mi invitación |
| PATCH | `/meetups/{id}/participations/me/withdraw` | Retirarme |
| GET | `/meetups/{id}/participations` | Ver participantes |

### 8.5 `communities` — Mafer

- **`Community`** (raíz): `universityId`, `name` (≤80, único por universidad), `description`
  (≤500), `creatorId`, `createdAt` y su entidad hija **`Membership`**: `studentId`, `role`
  (`MemberRole`: ADMIN, MEMBER), `joinedAt`. El creador entra como ADMIN.
  Métodos `join`, `leave`, `update(..., requesterId)`, `isMember`.
- **`CommunityPost`**: `communityId`, `authorId`, `content` (1–1000), `createdAt`.
- **Reglas con test:** nadie se une dos veces; el último ADMIN no puede abandonar; solo un ADMIN
  modifica la comunidad; solo los miembros publican.

| Método | Ruta | Descripción |
|---|---|---|
| POST · GET | `/communities` | Crear · listar las de mi universidad |
| GET · PUT | `/communities/{id}` | Obtener · modificar *(admin de la comunidad)* |
| POST | `/communities/{id}/members` | Unirme |
| DELETE | `/communities/{id}/members/me` | Salir |
| GET | `/communities/{id}/members` | Miembros |
| POST · GET | `/communities/{id}/posts` | Publicar · ver publicaciones *(miembros)* |

### 8.6 `messaging` — Mafer (después de `communities`)

- **`Message`**: `senderId`, `recipientId`, `content` (1–1000), `sentAt`, `readAt`.
  `markAsRead(byStudentId)` solo el destinatario.
- **`Notification`**: `recipientStudentId`, `type` (`NotificationType`: CONNECTION_REQUESTED,
  CONNECTION_ACCEPTED, PARTICIPATION_REQUESTED, PARTICIPATION_CONFIRMED, PARTICIPATION_INVITED,
  INVITATION_ACCEPTED, INVITATION_DECLINED, MEETUP_CANCELLED, MESSAGE_RECEIVED), `title`,
  `referenceId`, `createdAt`, `readAt`.
- **Manejadores de eventos** (`@EventListener`): crean notificaciones a partir de los eventos
  de `connections` y `meetups`. Los campos de los eventos de `meetups` y a quién se notifica cada
  uno están en 8.4 (tabla de eventos); `meetupId` es el `referenceId`. Crear un meetup **no**
  notifica a nadie: solo se avisa a quien se invita, a quien se acepta y al organizador.
- **Reglas:** solo se escriben mensajes entre estudiantes **conectados y no bloqueados**; solo
  el destinatario marca como leído.

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/messages` | Enviar mensaje |
| GET | `/messages/conversations/{otherStudentId}` | Conversación con otro estudiante (historial) |
| PATCH | `/messages/{id}/read` | Marcar como leído |
| GET | `/notifications` | Mis notificaciones |
| PATCH | `/notifications/{id}/read` | Marcar como leída |

**Tiempo real (D-17).** El chat entrega los mensajes al instante por WebSocket con STOMP; REST
sigue siendo la vía para enviar, guardar y consultar el historial.

- **Flujo:** `POST /messages` guarda el mensaje (regla del agregado `Message`), el handler
  despacha `MessageSentEvent` y un manejador de ese evento lo **empuja** al destinatario con
  `SimpMessagingTemplate`. El socket no contiene reglas de negocio.
- **Conexión:** `/ws` (con `setAllowedOrigins` para el frontend). Cada estudiante se suscribe a
  `/user/queue/messages`; las notificaciones llegan por `/user/queue/notifications`.
- **Identidad provisional:** `studentId` como parámetro de la conexión, igual que el header
  `X-Student-Id` de REST (D-16); con JWT sale del token.
- **Dependencia:** `spring-boot-starter-websocket`; la agrega André en `pom.xml`, no cada
  integrante.
- **Orden de trabajo:** dominio, persistencia y REST de `messaging` primero (el evento
  `MessageSentEvent` se declara desde el dominio); el push por WebSocket se monta sobre ese
  evento y no cambia el dominio ni la persistencia.
- **Pruebas:** el manejador del evento se prueba con JUnit y un `SimpMessagingTemplate` simulado.
  Swagger no documenta WebSockets; el contrato queda en esta sección.

### 8.7 `moderation` — Ubes

- **`Report`**: `reporterId`, `targetType` (`ReportTargetType`: STUDENT, MEETUP, MESSAGE,
  COMMUNITY, COMMUNITY_POST), `targetId`, `reason` (`ReportReason`: INAPPROPRIATE_CONTENT,
  FAKE_PROFILE, HARASSMENT, SPAM, OTHER), `detail` (≤500), `status` (`ReportStatus`: OPEN,
  REVIEWED, DISMISSED), `createdAt`, `reviewedByAccountId`, `reviewedAt`, `resolution` (≤500).
  `review(adminAccountId, resolution)` y `dismiss(adminAccountId, resolution)`, solo si está OPEN.
- **`Block`**: `blockerId`, `blockedId`, `createdAt`. Nadie se bloquea a sí mismo; sin
  duplicados.
- Reportar y bloquear están disponibles en **todos los planes**.

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/reports` | Reportar |
| GET | `/reports?status=` · `/reports/{id}` | Bandeja de reportes *(admin)* |
| PATCH | `/reports/{id}/review` · `/dismiss` | Resolver *(admin)* |
| POST · GET | `/blocks` | Bloquear · mis bloqueos |
| DELETE | `/blocks/{blockedStudentId}` | Desbloquear |

### 8.8 `subscriptions` — André

- **`Plan`** (enum con sus límites; `-1` = sin límite):

  | Plan | Precio | Solicitudes de conexión/día | Solicitudes a meetups/día | Meetups organizados activos |
  |---|---|---|---|---|
  | FREE | S/ 0 | 5 | 3 | 1 |
  | CAMPUS | S/ 9.90 | -1 | -1 | -1 |
  | CAMPUS_PRO | S/ 19.90 | -1 | -1 | -1 |

  Los precios son hipótesis del modelo de negocio, no ventas reales.
- **`Subscription`**: `studentId`, `plan`, `status` (ACTIVE, CANCELLED), `startedAt`, `endsAt`.
  `cancel()`. Sin suscripción activa, el estudiante está en FREE. **Sin pagos reales.**

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/plans` | Planes con precios y límites |
| GET | `/subscriptions/me` | Mi plan vigente y sus límites |
| POST | `/subscriptions` | Suscribirse *(simulado, 30 días)* |
| PATCH | `/subscriptions/me/cancel` | Cancelar y volver a FREE |

---

## 9. Secuencia de commits

Regla general y punto de control: sección 6 del `AGENTS.md` raíz. Aquí está **el orden**.

### 9.1 Andamiaje — André (B0)

1. `build: agregar pom y Maven Wrapper`
2. `build: agregar clase principal y application.properties`
3. `chore: agregar compose.yaml con MySQL`
4. `feat(shared): agregar AbstractDomainAggregateRoot con pullDomainEvents`
5. `feat(shared): agregar excepciones base del dominio`
6. `feat(shared): agregar GlobalExceptionHandler y ErrorResource`
7. `feat(shared): agregar header provisional X-Student-Id`
8. `feat(shared): configurar Swagger con springdoc`

B1 (André, `iam` → `University`) sigue el mismo orden de la sección 9.2 y queda como **ejemplo
completo en el repo**.

### 9.2 Cada contexto — de abajo hacia arriba

Cada línea es **uno o más commits pequeños**, nunca una capa entera de golpe:

1. Enums y value objects → su test.
2. Entidades hijas → su test.
3. Agregado con sus reglas → tests de invariantes.
4. Eventos y excepciones.
5. Contrato de repositorio.
6. Interfaz de la fachada (`interfaces/acl`) — **B3, publicar temprano**.
7. Entidades JPA → `JpaRepository` → mapper → adapter → **test `@DataJpaTest` con H2**.
8. Resources → assemblers → controller (un commit por grupo de endpoints).
9. Colección Postman del contexto.
10. **Después del 15-oct:** capa de aplicación según la versión del profesor (el controller pasa
    a llamar al servicio) e implementación de la fachada.

Ejemplo `meetups`:

```text
feat(meetups): agregar enums MeetupType, MeetupStatus y ParticipationStatus
feat(meetups): agregar value objects Place y Capacity con validaciones
test(meetups): cubrir validaciones de Place y Capacity
feat(meetups): agregar entidad Participation con confirmar y retirar
feat(meetups): agregar agregado Meetup con reglas de cupo y organizador
test(meetups): cubrir invariantes de cupo, cancelacion y fecha futura
feat(meetups): agregar eventos y excepciones de dominio
feat(meetups): declarar MeetupRepository en el dominio
feat(meetups): agregar entidades JPA de meetup y participacion
feat(meetups): agregar repositorio JPA con bloqueo pesimista
feat(meetups): agregar mapper y adapter de persistencia
test(meetups): probar el adapter de meetups con H2
feat(meetups): agregar resources y assemblers
feat(meetups): exponer endpoints de meetups
feat(meetups): exponer endpoints de participacion
test(meetups): agregar coleccion Postman de meetups
```

---

## 10. Pruebas

| Nivel | Cómo | Obligatorio |
|---|---|---|
| Dominio | JUnit 5 **sin Spring**, en `src/test/java/com/utp/quesale/<contexto>/domain/` | Cada regla marcada «con test» en la sección 8 |
| Persistencia | `@DataJpaTest` + `@Import(adapter, mapper)` sobre **H2 en memoria** (D-14), en `src/test/java/com/utp/quesale/<contexto>/infrastructure/` | Cada adapter: guardar, leer y una consulta propia |
| API | Swagger UI: http://localhost:8080/swagger-ui.html | Cada endpoint, caso de éxito y de error |
| API | Postman o Thunder Client con las colecciones de `docs/api/` | Cada endpoint |

- `./mvnw test` antes de cada PR. **No necesita MySQL**: los tests usan H2.
- `@DataJpaTest` se importa de `org.springframework.boot.data.jpa.test.autoconfigure` (Boot 4).
- Si tu entidad JPA tiene FK a otra (`@ManyToOne`), el `@Import` incluye también el adapter y el
  mapper de esa otra para crear el dato previo (ej.: para guardar un `Student` primero se guarda
  una `University`).
- Colecciones en `docs/api/`, **una por contexto** para evitar conflictos:
  `quesale.postman_environment.json` (variables `baseUrl`, `studentId`),
  `00-setup.postman_collection.json` (Ubes: crea UTP y UPC, cuatro estudiantes, intereses y
  habilidades en orden para la demo; `Account` queda fuera de esta entrega) y
  `<contexto>.postman_collection.json`. Formato Postman v2.1, que
  Thunder Client importa directamente.
- Cada request de error dice en su nombre qué espera, por ejemplo
  `POST meetup lleno → 409`.

---

## 11. Base de datos

### 11.1 MySQL local

Dos opciones, la que tengas:

- **MySQL instalado en Windows** (servicio `MySQL80`): no necesitas Docker. Si tu contraseña de
  `root` no es `root`, pásala con `DB_PASSWORD` (sección 2).
- **Docker**: `compose.yaml` en la raíz del repo:

```yaml
services:
  mysql:
    image: mysql:8.4
    container_name: quesale-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: quesale
    ports:
      - "${DB_PORT:-3306}:3306"
    volumes:
      - quesale-mysql-data:/var/lib/mysql

volumes:
  quesale-mysql-data:
```

- Si ya tienes MySQL instalado en el puerto 3306 y quieres además el de Docker, usa otro puerto:
  `$env:DB_PORT = "3307"; docker compose up -d` y arranca la API en la misma terminal.
- `ddl-auto=update` no borra columnas viejas. Si cambias una entidad y algo falla:
  `DROP DATABASE quesale;` y reinicia la API.

### 11.2 EER Diagram con MySQL Workbench — Fernando

1. Levantar MySQL y arrancar la API una vez para que Hibernate cree las tablas.
2. En Workbench: **Database → Reverse Engineer…**
3. Conexión `localhost:3306`, usuario `root` → seleccionar el esquema **`quesale`**.
4. Importar las tablas con **«Place imported objects on a diagram»** marcado.
5. Se genera el **EER Diagram**: ordenar las tablas por contexto.
6. **File → Export → Export as PNG** → sección 3.4.7 del informe.

Las relaciones aparecen porque las entidades JPA declaran FKs reales (D-05).

### 11.3 Diccionario de datos

La edición Community de Workbench no trae DBDoc. Se genera con:

```sql
SELECT c.TABLE_NAME     AS tabla,
       c.COLUMN_NAME    AS columna,
       c.COLUMN_TYPE    AS tipo,
       c.IS_NULLABLE    AS acepta_nulo,
       c.COLUMN_KEY     AS clave,
       c.COLUMN_DEFAULT AS valor_por_defecto,
       c.EXTRA          AS extra
FROM INFORMATION_SCHEMA.COLUMNS c
WHERE c.TABLE_SCHEMA = 'quesale'
ORDER BY c.TABLE_NAME, c.ORDINAL_POSITION;

SELECT k.TABLE_NAME, k.COLUMN_NAME, k.REFERENCED_TABLE_NAME, k.REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE k
WHERE k.TABLE_SCHEMA = 'quesale' AND k.REFERENCED_TABLE_NAME IS NOT NULL;
```

El resultado se exporta y se completa con el significado de cada campo (sección 3.4.8).

---

## 12. Guion de exposición

Cada integrante muestra su contexto en Swagger: primero el caso de éxito, luego la regla
defendida, y explica el recorrido por las cuatro capas.

| Contexto | Demostración | Resultado esperado | Dónde vive la regla |
|---|---|---|---|
| `iam` | Registrar una cuenta con correo personal | 400: dominio no permitido | `University.accepts` + servicio de cuentas |
| `profiles` | Ver el perfil privado de alguien que no es tu conexión | Versión reducida | `Student.isVisibleTo` |
| `connections` | Que el solicitante intente aceptar su propia solicitud | 403 | `Connection.accept` |
| `meetups` | Meetup con cupo 2: confirmar a un tercero | 409: cupo lleno | `Meetup.confirmParticipation` + bloqueo pesimista |
| `communities` | Que el único admin intente salir | 409 | `Community.leave` |
| `messaging` | Escribir a alguien que no es tu conexión | 409 | Servicio de mensajes + fachada de conexiones |
| `moderation` | Un bloqueado envía solicitud de conexión | 409 | Fachada de moderación |
| `subscriptions` | Sexta solicitud de conexión del día en plan FREE | 409 | Límite del plan + conteo en `connections` |

---

## 13. Despliegue (D-09)

| Entorno | Rol |
|---|---|
| **AWS (Ubuntu)** | Producción final: `.jar` con Tomcat embebido y MySQL |
| Contabo o DigitalOcean (VPS, verificar precio) | Staging opcional |

En cualquier VPS Ubuntu: Java 21, MySQL 8, `./mvnw clean package`, el `.jar` como servicio
`systemd`, firewall (`ufw` o security groups en AWS) y credenciales en variables de entorno. Se
ejecuta cuando el curso llegue a despliegue o el profesor lo pida. Responsable: Fernando.

---

## 14. Registro de cambios del backend

| Fecha | Quién (persona · agente) | Cambio | Motivo |
|---|---|---|---|
| 2026-10-08 | André · Claude Code | Versión inicial: molde, capas, contextos, endpoints, base de datos | Inicio del desarrollo para el APF2 |
| 2026-10-11 | André · Claude Code | Pasa de `backend/AGENTS.md` a `docs/guia-backend.md` con rutas en la raíz; header `X-Student-Id`; controller con repositorio mientras no exista la capa de aplicación; tests H2 por adapter; alcance por agregado; `iam` del repo como ejemplo | Repos separados (D-15), indicaciones del profesor del 8 y 11-oct, límite de 32 KB de Codex |
| 2026-10-19 | André · Claude Code | Árbol de la sección 3 y regla «Entre contextos» alineados con la capa de aplicación CQRS (`application/commands`, `queries`, `dtos`, `assemblers`, `handlers`); se quitó `application/internal` y `domain/commands`/`queries` | La sección 4 ya describía CQRS pero el árbol conservaba la estructura anterior; lo detectó el agente de Fernando |
| 2026-10-21 | Jesús · Codex | La regla de universidad de `meetups` se valida en la capa de aplicación al solicitar, invitar y aceptar invitaciones | Feedback de André en el PR #32; `ProfilesContextFacade` ya permite aplicar la regla |
| 2026-10-22 | Ubes · Copilot | `00-setup` prepara UTP, UPC, tres estudiantes UTP, uno UPC, intereses y habilidades; no crea cuentas | Account se recortó de esta entrega; indicación de André |
