package com.utp.quesale.communities.infrastructure;

import com.utp.quesale.communities.domain.aggregates.Community;
import com.utp.quesale.communities.domain.entities.Membership;
import com.utp.quesale.communities.domain.valueobjects.MemberRole;
import com.utp.quesale.communities.infrastructure.persistence.jpa.adapters.CommunityRepositoryAdapter;
import com.utp.quesale.communities.infrastructure.persistence.jpa.mappers.CommunityPersistenceMapper;
import com.utp.quesale.iam.infrastructure.persistence.jpa.entities.UniversityJpaEntity;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

// Prueba el adapter sobre H2, incluyendo las FKs a universidades y estudiantes.
@DataJpaTest
@Import({CommunityRepositoryAdapter.class, CommunityPersistenceMapper.class})
class CommunityRepositoryAdapterTest {

    @Autowired
    private CommunityRepositoryAdapter communityRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Guarda y recupera la comunidad con su creador como ADMIN")
    void savesAndReadsCommunityWithCreatorAsAdmin() {
        UniversityJpaEntity university = persistUniversity("utp.edu.pe");
        StudentJpaEntity creator = persistStudent(university, "mafer@utp.edu.pe", "20261001");

        Community saved = communityRepository.save(
                new Community(university.getId(), "Club de ajedrez", "Partidas y torneos", creator.getId()));
        entityManager.flush();
        entityManager.clear();

        Community found = communityRepository.findById(saved.getId()).orElseThrow();
        assertEquals(saved.getId(), found.getId());
        assertEquals(university.getId(), found.getUniversityId());
        assertEquals("Club de ajedrez", found.getName());
        assertEquals("Partidas y torneos", found.getDescription());
        assertEquals(creator.getId(), found.getCreatorId());
        assertEquals(1, found.getMemberships().size());
        assertTrue(found.isAdmin(creator.getId()));
        assertNotNull(found.getMemberships().get(0).getId());
    }

    @Test
    @DisplayName("Guarda a los miembros que se unen")
    void savesJoinedMembers() {
        UniversityJpaEntity university = persistUniversity("utp.edu.pe");
        StudentJpaEntity creator = persistStudent(university, "mafer@utp.edu.pe", "20261001");
        StudentJpaEntity member = persistStudent(university, "ana@utp.edu.pe", "20261002");
        Community saved = communityRepository.save(new Community(university.getId(), "Club", null, creator.getId()));
        entityManager.flush();
        entityManager.clear();

        Community loaded = communityRepository.findById(saved.getId()).orElseThrow();
        loaded.join(member.getId());
        communityRepository.update(loaded);
        entityManager.flush();
        entityManager.clear();

        Community found = communityRepository.findById(saved.getId()).orElseThrow();
        assertEquals(2, found.getMemberships().size());
        assertTrue(found.isMember(member.getId()));
        assertFalse(found.isAdmin(member.getId()));
    }

    @Test
    @DisplayName("Promover a un miembro conserva su registro y solo cambia el rol")
    void promoteKeepsMembershipId() {
        UniversityJpaEntity university = persistUniversity("utp.edu.pe");
        StudentJpaEntity creator = persistStudent(university, "mafer@utp.edu.pe", "20261001");
        StudentJpaEntity member = persistStudent(university, "ana@utp.edu.pe", "20261002");
        Community community = new Community(university.getId(), "Club", null, creator.getId());
        community.join(member.getId());
        Community saved = communityRepository.save(community);
        entityManager.flush();
        entityManager.clear();

        Community loaded = communityRepository.findById(saved.getId()).orElseThrow();
        Long membershipId = membershipOf(loaded, member.getId()).getId();
        loaded.promote(member.getId(), creator.getId());
        communityRepository.update(loaded);
        entityManager.flush();
        entityManager.clear();

        Community found = communityRepository.findById(saved.getId()).orElseThrow();
        assertEquals(2, found.getMemberships().size());
        assertEquals(MemberRole.ADMIN, membershipOf(found, member.getId()).getRole());
        assertEquals(membershipId, membershipOf(found, member.getId()).getId());
    }

    @Test
    @DisplayName("Quien sale de la comunidad se borra de la base")
    void leavingRemovesMembership() {
        UniversityJpaEntity university = persistUniversity("utp.edu.pe");
        StudentJpaEntity creator = persistStudent(university, "mafer@utp.edu.pe", "20261001");
        StudentJpaEntity member = persistStudent(university, "ana@utp.edu.pe", "20261002");
        Community community = new Community(university.getId(), "Club", null, creator.getId());
        community.join(member.getId());
        Community saved = communityRepository.save(community);
        entityManager.flush();
        entityManager.clear();

        Community loaded = communityRepository.findById(saved.getId()).orElseThrow();
        loaded.leave(member.getId());
        communityRepository.update(loaded);
        entityManager.flush();
        entityManager.clear();

        Community found = communityRepository.findById(saved.getId()).orElseThrow();
        assertEquals(1, found.getMemberships().size());
        assertFalse(found.isMember(member.getId()));
    }

    @Test
    @DisplayName("Modificar la comunidad actualiza nombre y descripción")
    void savesUpdatedCommunity() {
        UniversityJpaEntity university = persistUniversity("utp.edu.pe");
        StudentJpaEntity creator = persistStudent(university, "mafer@utp.edu.pe", "20261001");
        Community saved = communityRepository.save(new Community(university.getId(), "Club", null, creator.getId()));
        entityManager.flush();
        entityManager.clear();

        Community loaded = communityRepository.findById(saved.getId()).orElseThrow();
        loaded.update("Club de ajedrez UTP", "Nueva descripcion", creator.getId());
        communityRepository.update(loaded);
        entityManager.flush();
        entityManager.clear();

        Community found = communityRepository.findById(saved.getId()).orElseThrow();
        assertEquals("Club de ajedrez UTP", found.getName());
        assertEquals("Nueva descripcion", found.getDescription());
        assertEquals(1, found.getMemberships().size());
    }

    @Test
    @DisplayName("Lista solo las comunidades de la universidad pedida")
    void listsCommunitiesOfOneUniversity() {
        UniversityJpaEntity utp = persistUniversity("utp.edu.pe");
        UniversityJpaEntity other = persistUniversity("otra.edu.pe");
        StudentJpaEntity utpStudent = persistStudent(utp, "mafer@utp.edu.pe", "20261001");
        StudentJpaEntity otherStudent = persistStudent(other, "luis@otra.edu.pe", "20261002");
        communityRepository.save(new Community(utp.getId(), "Club de ajedrez", null, utpStudent.getId()));
        communityRepository.save(new Community(utp.getId(), "Club de lectura", null, utpStudent.getId()));
        communityRepository.save(new Community(other.getId(), "Club de cine", null, otherStudent.getId()));
        entityManager.flush();
        entityManager.clear();

        assertEquals(2, communityRepository.findAllByUniversityId(utp.getId()).size());
        assertEquals(1, communityRepository.findAllByUniversityId(other.getId()).size());
    }

    @Test
    @DisplayName("El nombre se busca por universidad sin distinguir mayúsculas")
    void checksNameUniquenessPerUniversity() {
        UniversityJpaEntity utp = persistUniversity("utp.edu.pe");
        UniversityJpaEntity other = persistUniversity("otra.edu.pe");
        StudentJpaEntity creator = persistStudent(utp, "mafer@utp.edu.pe", "20261001");
        communityRepository.save(new Community(utp.getId(), "Club de ajedrez", null, creator.getId()));
        entityManager.flush();

        assertTrue(communityRepository.existsByUniversityIdAndName(utp.getId(), "club de AJEDREZ"));
        assertFalse(communityRepository.existsByUniversityIdAndName(other.getId(), "Club de ajedrez"));
        assertFalse(communityRepository.existsByUniversityIdAndName(utp.getId(), "Club de lectura"));
    }

    @Test
    @DisplayName("La base rechaza dos comunidades con el mismo nombre en una universidad")
    void databaseRejectsDuplicateName() {
        UniversityJpaEntity utp = persistUniversity("utp.edu.pe");
        StudentJpaEntity creator = persistStudent(utp, "mafer@utp.edu.pe", "20261001");
        communityRepository.save(new Community(utp.getId(), "Club de ajedrez", null, creator.getId()));
        entityManager.flush();

        assertThrows(DataIntegrityViolationException.class, () -> {
            communityRepository.save(new Community(utp.getId(), "Club de ajedrez", null, creator.getId()));
            entityManager.flush();
        });
    }

    private Membership membershipOf(Community community, Long studentId) {
        return community.getMemberships().stream()
                .filter(membership -> membership.getStudentId().equals(studentId))
                .findFirst()
                .orElseThrow();
    }

    private UniversityJpaEntity persistUniversity(String emailDomain) {
        UniversityJpaEntity university = new UniversityJpaEntity();
        university.setName("Universidad " + emailDomain);
        university.setEmailDomain(emailDomain);
        university.setActive(true);
        university.setCreatedAt(Instant.now());
        entityManager.persist(university);
        return university;
    }

    private StudentJpaEntity persistStudent(UniversityJpaEntity university, String email, String recordId) {
        StudentJpaEntity student = new StudentJpaEntity();
        student.setRecordId(recordId);
        student.setUniversity(university);
        student.setEmail(email);
        student.setFirstName("Estudiante");
        student.setLastName("Prueba");
        student.setCreatedAt(Instant.now());
        entityManager.persist(student);
        return student;
    }
}
