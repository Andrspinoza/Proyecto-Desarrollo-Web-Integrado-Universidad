package com.utp.quesale.profiles.infrastructure;

import com.utp.quesale.iam.domain.aggregates.University;
import com.utp.quesale.iam.domain.valueobjects.EmailDomain;
import com.utp.quesale.iam.infrastructure.persistence.jpa.adapters.UniversityRepositoryAdapter;
import com.utp.quesale.iam.infrastructure.persistence.jpa.mappers.UniversityPersistenceMapper;
import com.utp.quesale.profiles.domain.aggregates.Interest;
import com.utp.quesale.profiles.domain.aggregates.Skill;
import com.utp.quesale.profiles.domain.aggregates.Student;
import com.utp.quesale.profiles.domain.valueobjects.Career;
import com.utp.quesale.profiles.domain.valueobjects.FullName;
import com.utp.quesale.profiles.domain.valueobjects.StudyCycle;
import com.utp.quesale.profiles.domain.valueobjects.Visibility;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.adapters.InterestRepositoryAdapter;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.adapters.SkillRepositoryAdapter;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.adapters.StudentRepositoryAdapter;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.mappers.InterestPersistenceMapper;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.mappers.SkillPersistenceMapper;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.mappers.StudentPersistenceMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({StudentRepositoryAdapter.class, StudentPersistenceMapper.class,
        UniversityRepositoryAdapter.class, UniversityPersistenceMapper.class,
        InterestRepositoryAdapter.class, InterestPersistenceMapper.class,
        SkillRepositoryAdapter.class, SkillPersistenceMapper.class})
class StudentProfilePersistenceTest {
    @Autowired
    private StudentRepositoryAdapter students;
    @Autowired
    private UniversityRepositoryAdapter universities;
    @Autowired
    private InterestRepositoryAdapter interests;
    @Autowired
    private SkillRepositoryAdapter skills;
    @Autowired
    private EntityManager entityManager;

    private Student newStudent() {
        Long universityId = universities.save(new University("UTP", new EmailDomain("utp.edu.pe"))).getId();
        return new Student(universityId, "estudiante@utp.edu.pe", new FullName("Estudiante", "Prueba"));
    }

    private void reloadFromDatabase() {
        // Evita que las comprobaciones se limiten a la caché de JPA.
        entityManager.flush();
        entityManager.clear();
    }

    private Long profileId(Long studentId) {
        return entityManager.find(StudentJpaEntity.class, studentId).getProfile().getId();
    }

    @Test
    void savesAndReadsCompleteProfile() {
        Long interestId = interests.save(new Interest("Musica")).getId();
        Long skillId = skills.save(new Skill("Programacion")).getId();
        Student original = newStudent();
        original.createProfile(new Career("Ingenieria"), new StudyCycle(6), "Mi descripcion", Visibility.PRIVATE);
        original.addInterest(interestId);
        original.addSkill(skillId);

        Student saved = students.save(original);
        reloadFromDatabase();
        Student found = students.findById(saved.getId()).orElseThrow();

        assertNotNull(profileId(found.getId()));
        assertEquals(new Career("Ingenieria"), found.getProfile().getCareer());
        assertEquals(new StudyCycle(6), found.getProfile().getCycle());
        assertEquals("Mi descripcion", found.getProfile().getDescription());
        assertEquals(Visibility.PRIVATE, found.getProfile().getVisibility());
        assertEquals(Set.of(interestId), found.getProfile().getInterestIds());
        assertEquals(Set.of(skillId), found.getProfile().getSkillIds());
        assertEquals(Set.of(interestId), students.findAll().getFirst().getProfile().getInterestIds());
        assertTrue(found.pullDomainEvents().isEmpty());
    }

    @Test
    void addsProfileToExistingStudent() {
        Student saved = students.save(newStudent());
        reloadFromDatabase();
        Student existing = students.findById(saved.getId()).orElseThrow();
        assertNull(existing.getProfile());

        existing.createProfile(new Career("Derecho"), new StudyCycle(2), null, Visibility.PUBLIC);
        students.update(existing);
        reloadFromDatabase();

        Student found = students.findById(saved.getId()).orElseThrow();
        assertEquals(Visibility.PUBLIC, found.getProfile().getVisibility());
        assertNull(found.getProfile().getDescription());
        assertTrue(found.getProfile().getInterestIds().isEmpty());
        assertTrue(found.getProfile().getSkillIds().isEmpty());
        assertEquals(1, students.findAll().size());
    }

    @Test
    void updatesSameProfileAndReplacesCatalogLinks() {
        Long oldInterest = interests.save(new Interest("Musica")).getId();
        Long newInterest = interests.save(new Interest("Lectura")).getId();
        Long oldSkill = skills.save(new Skill("Programacion")).getId();
        Long newSkill = skills.save(new Skill("Oratoria")).getId();
        Student original = newStudent();
        original.createProfile(new Career("Ingenieria"), new StudyCycle(6), "Antes", Visibility.PUBLIC);
        original.addInterest(oldInterest);
        original.addSkill(oldSkill);
        Student saved = students.save(original);
        reloadFromDatabase();
        Long originalProfileId = profileId(saved.getId());
        Student changed = students.findById(saved.getId()).orElseThrow();
        changed.updateProfile(new Career("Derecho"), new StudyCycle(7), "Despues", Visibility.PRIVATE);
        changed.removeInterest(oldInterest);
        changed.addInterest(newInterest);
        changed.removeSkill(oldSkill);
        changed.addSkill(newSkill);

        students.update(changed);
        reloadFromDatabase();
        Student found = students.findById(saved.getId()).orElseThrow();

        assertEquals(originalProfileId, profileId(saved.getId()));
        assertEquals(new Career("Derecho"), found.getProfile().getCareer());
        assertEquals(new StudyCycle(7), found.getProfile().getCycle());
        assertEquals("Despues", found.getProfile().getDescription());
        assertEquals(Visibility.PRIVATE, found.getProfile().getVisibility());
        assertEquals(Set.of(newInterest), found.getProfile().getInterestIds());
        assertEquals(Set.of(newSkill), found.getProfile().getSkillIds());
        assertEquals(1L, entityManager.createQuery("select count(p) from ProfileJpaEntity p", Long.class)
                .getSingleResult());
        assertTrue(interests.existsById(oldInterest));
        assertTrue(skills.existsById(oldSkill));
    }

    @Test
    void rejectsUnknownInterest() {
        Student student = newStudent();
        student.createProfile(new Career("Ingenieria"), new StudyCycle(6), null, Visibility.PUBLIC);
        student.addInterest(Long.MAX_VALUE);

        assertThrows(IllegalArgumentException.class, () -> students.save(student));
        assertTrue(students.findAll().isEmpty());
    }

    @Test
    void rejectsUnknownSkill() {
        Student student = newStudent();
        student.createProfile(new Career("Ingenieria"), new StudyCycle(6), null, Visibility.PUBLIC);
        student.addSkill(Long.MAX_VALUE);

        assertThrows(IllegalArgumentException.class, () -> students.save(student));
        assertTrue(students.findAll().isEmpty());
    }

    @Test
    void deletesProfileWithStudentButPreservesCatalogs() {
        Long interestId = interests.save(new Interest("Musica")).getId();
        Long skillId = skills.save(new Skill("Programacion")).getId();
        Student original = newStudent();
        original.createProfile(new Career("Ingenieria"), new StudyCycle(6), null, Visibility.PUBLIC);
        original.addInterest(interestId);
        original.addSkill(skillId);
        Student saved = students.save(original);
        reloadFromDatabase();

        entityManager.remove(entityManager.find(StudentJpaEntity.class, saved.getId()));
        reloadFromDatabase();

        assertTrue(students.findById(saved.getId()).isEmpty());
        assertEquals(0L, entityManager.createQuery("select count(p) from ProfileJpaEntity p", Long.class)
                .getSingleResult());
        assertTrue(interests.existsById(interestId));
        assertTrue(skills.existsById(skillId));
    }
}
