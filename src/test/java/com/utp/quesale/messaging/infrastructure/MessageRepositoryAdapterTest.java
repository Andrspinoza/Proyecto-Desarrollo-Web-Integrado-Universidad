package com.utp.quesale.messaging.infrastructure;

import com.utp.quesale.iam.infrastructure.persistence.jpa.entities.UniversityJpaEntity;
import com.utp.quesale.messaging.domain.aggregates.Message;
import com.utp.quesale.messaging.infrastructure.persistence.jpa.adapters.MessageRepositoryAdapter;
import com.utp.quesale.messaging.infrastructure.persistence.jpa.entities.MessageJpaEntity;
import com.utp.quesale.messaging.infrastructure.persistence.jpa.mappers.MessagePersistenceMapper;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({MessageRepositoryAdapter.class, MessagePersistenceMapper.class})
class MessageRepositoryAdapterTest {

    @Autowired
    private MessageRepositoryAdapter messageRepository;

    @Autowired
    private EntityManager entityManager;

    private StudentJpaEntity ana;
    private StudentJpaEntity marco;
    private StudentJpaEntity alicia;

    @BeforeEach
    void persistStudents() {
        UniversityJpaEntity university = persistUniversity();
        ana = persistStudent(university, "ana@utp.edu.pe", "20261001");
        marco = persistStudent(university, "marco@utp.edu.pe", "20261002");
        alicia = persistStudent(university, "alicia@utp.edu.pe", "20261003");
    }

    @Test
    @DisplayName("Guarda y recupera un mensaje con su emisor y su destinatario")
    void savesAndReadsMessage() {
        Message saved = messageRepository.save(new Message(ana.getId(), marco.getId(), "Hola Marco"));
        entityManager.flush();
        entityManager.clear();

        Message found = messageRepository.findById(saved.getId()).orElseThrow();
        assertNotNull(saved.getId());
        assertEquals(ana.getId(), found.getSenderId());
        assertEquals(marco.getId(), found.getRecipientId());
        assertEquals("Hola Marco", found.getContent());
        assertNotNull(found.getSentAt());
    }

    @Test
    @DisplayName("Un mensaje inexistente no se encuentra")
    void missingMessageIsEmpty() {
        assertTrue(messageRepository.findById(9999L).isEmpty());
    }

    @Test
    @DisplayName("La conversación incluye los mensajes en ambos sentidos, del más antiguo al más reciente")
    void listsConversationInBothDirections() {
        Instant now = Instant.now();
        messageRepository.save(new Message(null, marco.getId(), ana.getId(), "Segundo", now.minusSeconds(30)));
        messageRepository.save(new Message(null, ana.getId(), marco.getId(), "Primero", now.minusSeconds(60)));
        messageRepository.save(new Message(null, ana.getId(), marco.getId(), "Tercero", now));
        entityManager.flush();
        entityManager.clear();

        List<Message> conversation = messageRepository.findConversation(ana.getId(), marco.getId());

        assertEquals(List.of("Primero", "Segundo", "Tercero"),
                conversation.stream().map(Message::getContent).toList());
        assertEquals(3, messageRepository.findConversation(marco.getId(), ana.getId()).size());
    }

    @Test
    @DisplayName("La conversación no incluye mensajes con otros estudiantes")
    void conversationIgnoresOtherChats() {
        messageRepository.save(new Message(ana.getId(), marco.getId(), "Para Marco"));
        messageRepository.save(new Message(ana.getId(), alicia.getId(), "Para Alicia"));
        messageRepository.save(new Message(alicia.getId(), marco.getId(), "Alicia a Marco"));
        entityManager.flush();
        entityManager.clear();

        List<Message> conversation = messageRepository.findConversation(ana.getId(), marco.getId());

        assertEquals(1, conversation.size());
        assertEquals("Para Marco", conversation.get(0).getContent());
    }

    @Test
    @DisplayName("Una conversación sin mensajes devuelve una lista vacía")
    void emptyConversation() {
        assertTrue(messageRepository.findConversation(ana.getId(), alicia.getId()).isEmpty());
    }

    @Test
    @DisplayName("La base rechaza un mensaje donde emisor y destinatario son el mismo estudiante")
    void databaseRejectsMessageToYourself() {
        MessageJpaEntity entity = new MessageJpaEntity();
        entity.setSender(ana);
        entity.setRecipient(ana);
        entity.setContent("Hola yo");
        entity.setSentAt(Instant.now());

        assertThrows(PersistenceException.class, () -> {
            entityManager.persist(entity);
            entityManager.flush();
        });
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
