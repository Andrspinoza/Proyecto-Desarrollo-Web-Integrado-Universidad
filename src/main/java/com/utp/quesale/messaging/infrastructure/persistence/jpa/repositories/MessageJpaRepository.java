package com.utp.quesale.messaging.infrastructure.persistence.jpa.repositories;

import com.utp.quesale.messaging.infrastructure.persistence.jpa.entities.MessageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageJpaRepository extends JpaRepository<MessageJpaEntity, Long> {

    @Query("""
            select m from MessageJpaEntity m
            where (m.sender.id = :studentId and m.recipient.id = :otherStudentId)
               or (m.sender.id = :otherStudentId and m.recipient.id = :studentId)
            order by m.sentAt asc, m.id asc
            """)
    List<MessageJpaEntity> findConversation(@Param("studentId") Long studentId,
                                            @Param("otherStudentId") Long otherStudentId);
}
