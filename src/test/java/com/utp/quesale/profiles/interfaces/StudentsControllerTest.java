package com.utp.quesale.profiles.interfaces;

import com.utp.quesale.iam.interfaces.acl.IamContextFacade;
import com.utp.quesale.profiles.application.handlers.commands.CreateStudentCommandHandler;
import com.utp.quesale.profiles.application.handlers.commands.UpsertProfileCommandHandler;
import com.utp.quesale.profiles.application.handlers.queries.GetStudentPhotoQueryHandler;
import com.utp.quesale.profiles.application.handlers.queries.GetStudentQueryHandler;
import com.utp.quesale.profiles.domain.aggregates.Student;
import com.utp.quesale.profiles.domain.repositories.StudentRepository;
import com.utp.quesale.profiles.domain.valueobjects.*;
import com.utp.quesale.profiles.interfaces.controllers.StudentsController;
import com.utp.quesale.shared.interfaces.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StudentsControllerTest {
    private MockMvc mvc;
    private MemoryStudents repository;
    private static final String VALID = """
            {"universityId":1,"email":"ANA@UTP.EDU.PE","firstName":"Ana","lastName":"Perez"}
            """;

    @BeforeEach
    void setUp() {
        repository = new MemoryStudents();
        IamContextFacade iam = new IamContextFacade() {
            public boolean isActiveUniversity(Long id) { return Long.valueOf(1L).equals(id); }
            public boolean universityAcceptsEmail(Long id, String email) {
                return isActiveUniversity(id) && email.endsWith("@utp.edu.pe");
            }
        };
        StudentsController controller = new StudentsController(
                new CreateStudentCommandHandler(repository, iam, aggregate -> { }),
                new GetStudentQueryHandler(repository, (viewer, student) -> false),
                new UpsertProfileCommandHandler(repository, Clock.fixed(Instant.parse("2026-11-20T12:00:00Z"), ZoneOffset.UTC)),
                new GetStudentPhotoQueryHandler(new com.utp.quesale.profiles.interfaces.MemoryPhotos()));
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void createsStudentAndReadsItAsOwner() throws Exception {
        mvc.perform(post("/api/v1/students").contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/students/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("ana@utp.edu.pe"))
                .andExpect(jsonPath("$.recordId").isNotEmpty());
        mvc.perform(get("/api/v1/students/1").header("X-Student-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ana"))
                .andExpect(jsonPath("$.email").value("ana@utp.edu.pe"));
    }

    @Test
    void duplicateEmailReturnsConflict() throws Exception {
        mvc.perform(post("/api/v1/students").contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/students").contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void missingUniversityReturnsNotFound() throws Exception {
        mvc.perform(post("/api/v1/students").contentType(MediaType.APPLICATION_JSON)
                        .content(VALID.replace("\"universityId\":1", "\"universityId\":99")))
                .andExpect(status().isNotFound());
    }

    @Test
    void personalEmailReturnsBadRequest() throws Exception {
        mvc.perform(post("/api/v1/students").contentType(MediaType.APPLICATION_JSON)
                        .content(VALID.replace("ANA@UTP.EDU.PE", "ana@gmail.com")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidInputReturnsBadRequestWithoutSaving() throws Exception {
        for (String body : List.of(VALID.replace("\"Ana\"", "\"\""),
                VALID.replace("ANA@UTP.EDU.PE", "invalid"),
                VALID.replace("\"universityId\":1", "\"universityId\":0"), "{}", "{")) {
            mvc.perform(post("/api/v1/students").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        org.junit.jupiter.api.Assertions.assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void missingStudentReturnsNotFound() throws Exception {
        mvc.perform(get("/api/v1/students/99").header("X-Student-Id", 1))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void missingOrInvalidViewerReturnsBadRequest() throws Exception {
        mvc.perform(get("/api/v1/students/1")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/students/1").header("X-Student-Id", 0))
                .andExpect(status().isBadRequest());
    }

    @Test
    void privateProfileIsReducedForStrangerAndCompleteForOwner() throws Exception {
        Student student = new Student(1L, "ana@utp.edu.pe", new FullName("Ana", "Perez"));
        student.createProfile(new Career("Sistemas"), new StudyCycle(6), "Privado", Visibility.PRIVATE);
        student.addInterest(9L);
        repository.save(student);

        mvc.perform(get("/api/v1/students/1").header("X-Student-Id", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ana"))
                .andExpect(jsonPath("$.profile.career").value("Sistemas"))
                .andExpect(jsonPath("$.profile.cycle").value(6))
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.recordId").doesNotExist())
                .andExpect(jsonPath("$.profile.description").doesNotExist())
                .andExpect(jsonPath("$.profile.interestIds").isEmpty());
        mvc.perform(get("/api/v1/students/1").header("X-Student-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.description").value("Privado"))
                .andExpect(jsonPath("$.profile.interestIds[0]").value(9));
    }

    private static int expectedAge() {
        return Period.between(LocalDate.of(2003, 5, 10), LocalDate.now(ZoneId.of("America/Lima"))).getYears();
    }

    private static final String PROFILE = """
            {"career":"Ingenieria de Sistemas","cycle":7,"description":"Cafe y voley","visibility":"PUBLIC",
             "gender":"FEMALE","birthDate":"2003-05-10","campus":"Lima Centro","interestIds":[],"skillIds":[]}
            """;

    @Test
    void savesTheProfileAndShowsAgeAndBirthDateOnlyToTheOwner() throws Exception {
        mvc.perform(post("/api/v1/students").contentType(MediaType.APPLICATION_JSON).content(VALID)).andExpect(status().isCreated());
        mvc.perform(put("/api/v1/students/me/profile").header("X-Student-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON).content(PROFILE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.career").value("Ingenieria de Sistemas"))
                .andExpect(jsonPath("$.profile.gender").value("FEMALE"))
                .andExpect(jsonPath("$.profile.age").value(expectedAge()))
                .andExpect(jsonPath("$.profile.campus").value("Lima Centro"))
                .andExpect(jsonPath("$.profile.birthDate").value("2003-05-10"));
        mvc.perform(get("/api/v1/students/me").header("X-Student-Id", 1))
                .andExpect(status().isOk()).andExpect(jsonPath("$.profile.birthDate").value("2003-05-10"));
        mvc.perform(get("/api/v1/students/1").header("X-Student-Id", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.age").value(expectedAge()))
                .andExpect(jsonPath("$.profile.campus").value("Lima Centro"))
                .andExpect(jsonPath("$.profile.birthDate").doesNotExist());
    }

    @Test
    void updatingTheProfileAgainReplacesTheValues() throws Exception {
        mvc.perform(post("/api/v1/students").contentType(MediaType.APPLICATION_JSON).content(VALID)).andExpect(status().isCreated());
        mvc.perform(put("/api/v1/students/me/profile").header("X-Student-Id", 1)
                .contentType(MediaType.APPLICATION_JSON).content(PROFILE)).andExpect(status().isOk());
        mvc.perform(put("/api/v1/students/me/profile").header("X-Student-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PROFILE.replace("Lima Centro", "Lima Norte").replace("FEMALE", "UNDISCLOSED")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.campus").value("Lima Norte"))
                .andExpect(jsonPath("$.profile.gender").value("UNDISCLOSED"));
    }

    @Test
    void invalidProfilesAreRejected() throws Exception {
        mvc.perform(post("/api/v1/students").contentType(MediaType.APPLICATION_JSON).content(VALID)).andExpect(status().isCreated());
        for (String body : List.of(
                PROFILE.replace("\"cycle\":7", "\"cycle\":11"),
                PROFILE.replace("2003-05-10", "2015-05-10"),
                PROFILE.replace("2003-05-10", "2999-01-01"),
                PROFILE.replace("\"career\":\"Ingenieria de Sistemas\"", "\"career\":\"\""),
                PROFILE.replace("\"PUBLIC\"", "null")))
            mvc.perform(put("/api/v1/students/me/profile").header("X-Student-Id", 1)
                    .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());
    }

    @Test
    void savingTheProfileOfAnUnknownStudentReturns404() throws Exception {
        mvc.perform(put("/api/v1/students/me/profile").header("X-Student-Id", 99)
                .contentType(MediaType.APPLICATION_JSON).content(PROFILE)).andExpect(status().isNotFound());
    }

    private static class MemoryStudents implements StudentRepository {
        private final Map<Long, Student> students = new LinkedHashMap<>();
        public Student save(Student student) {
            Long id = student.getId() == null ? (long) students.size() + 1 : student.getId();
            Student saved = new Student(id, student.getRecordId(), student.getUniversityId(), student.getEmail(),
                    student.getFullName(), student.getCreatedAt(), student.getProfile());
            students.put(id, saved);
            return saved;
        }
        public Student update(Student student) { return save(student); }
        public Optional<Student> findById(Long id) { return Optional.ofNullable(students.get(id)); }
        public List<Student> findAll() { return List.copyOf(students.values()); }
        public boolean existsById(Long id) { return students.containsKey(id); }
        public boolean existsByEmail(String email) {
            return students.values().stream().anyMatch(student -> student.getEmail().equalsIgnoreCase(email));
        }
    }
}
