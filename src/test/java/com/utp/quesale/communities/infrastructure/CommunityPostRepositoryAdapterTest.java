package com.utp.quesale.communities.infrastructure;

import com.utp.quesale.communities.domain.aggregates.Community;
import com.utp.quesale.communities.domain.entities.CommunityPost;
import com.utp.quesale.communities.infrastructure.persistence.jpa.adapters.CommunityPostRepositoryAdapter;
import com.utp.quesale.communities.infrastructure.persistence.jpa.adapters.CommunityRepositoryAdapter;
import com.utp.quesale.communities.infrastructure.persistence.jpa.mappers.CommunityPersistenceMapper;
import com.utp.quesale.communities.infrastructure.persistence.jpa.mappers.CommunityPostPersistenceMapper;
import com.utp.quesale.iam.infrastructure.persistence.jpa.entities.UniversityJpaEntity;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({CommunityPostRepositoryAdapter.class, CommunityPostPersistenceMapper.class,
        CommunityRepositoryAdapter.class, CommunityPersistenceMapper.class})
class CommunityPostRepositoryAdapterTest {

    @Autowired
    private CommunityPostRepositoryAdapter postRepository;

    @Autowired
    private CommunityRepositoryAdapter communityRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Guarda y recupera una publicación con su comunidad y su autor")
    void savesAndReadsPost() {
        UniversityJpaEntity university = persistUniversity();
        StudentJpaEntity author = persistStudent(university, "mafer@utp.edu.pe", "20261001");
        Community community = communityRepository.save(
                new Community(university.getId(), "Club de ajedrez", null, author.getId()));

        CommunityPost saved = postRepository.save(community.publish(author.getId(), "Hola a todos"));
        entityManager.flush();
        entityManager.clear();

        List<CommunityPost> found = postRepository.findAllByCommunityId(community.getId());
        assertEquals(1, found.size());
        assertEquals(saved.getId(), found.get(0).getId());
        assertEquals(community.getId(), found.get(0).getCommunityId());
        assertEquals(author.getId(), found.get(0).getAuthorId());
        assertEquals("Hola a todos", found.get(0).getContent());
    }

    @Test
    @DisplayName("Lista las publicaciones de más reciente a más antigua")
    void listsNewestFirst() {
        UniversityJpaEntity university = persistUniversity();
        StudentJpaEntity author = persistStudent(university, "mafer@utp.edu.pe", "20261001");
        Community community = communityRepository.save(
                new Community(university.getId(), "Club de ajedrez", null, author.getId()));
        Instant now = Instant.now();
        postRepository.save(new CommunityPost(null, community.getId(), author.getId(), "Primera", now.minusSeconds(60)));
        postRepository.save(new CommunityPost(null, community.getId(), author.getId(), "Segunda", now));
        entityManager.flush();
        entityManager.clear();

        List<CommunityPost> found = postRepository.findAllByCommunityId(community.getId());
        assertEquals(List.of("Segunda", "Primera"), found.stream().map(CommunityPost::getContent).toList());
    }

    @Test
    @DisplayName("Solo lista las publicaciones de la comunidad pedida")
    void listsPostsOfOneCommunity() {
        UniversityJpaEntity university = persistUniversity();
        StudentJpaEntity author = persistStudent(university, "mafer@utp.edu.pe", "20261001");
        Community chess = communityRepository.save(
                new Community(university.getId(), "Club de ajedrez", null, author.getId()));
        Community reading = communityRepository.save(
                new Community(university.getId(), "Club de lectura", null, author.getId()));
        postRepository.save(chess.publish(author.getId(), "Torneo el viernes"));
        postRepository.save(reading.publish(author.getId(), "Libro del mes"));
        entityManager.flush();
        entityManager.clear();

        List<CommunityPost> found = postRepository.findAllByCommunityId(chess.getId());
        assertEquals(1, found.size());
        assertEquals("Torneo el viernes", found.get(0).getContent());
    }

    private UniversityJpaEntity persistUniversity() {
        UniversityJpaEntity university = new UniversityJpaEntity();
        university.setName("Universidad Tecnológica del Perú");
        university.setEmailDomain("utp.edu.pe");
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
