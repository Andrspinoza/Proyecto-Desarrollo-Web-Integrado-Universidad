package com.utp.quesale.connections.infrastructure.persistence.jpa.repositories;

import com.utp.quesale.connections.domain.valueobjects.ConnectionStatus;
import com.utp.quesale.connections.infrastructure.persistence.jpa.entities.ConnectionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ConnectionJpaRepository extends JpaRepository<ConnectionJpaEntity, Long> {

    @Query("""
            select connection from ConnectionJpaEntity connection
            where connection.requester.id = :studentId or connection.recipient.id = :studentId
            order by connection.createdAt desc
            """)
    List<ConnectionJpaEntity> findAllByStudentId(@Param("studentId") Long studentId);

    @Query("""
            select connection from ConnectionJpaEntity connection
            where (connection.requester.id = :studentId or connection.recipient.id = :studentId)
              and connection.status = :status
            order by connection.createdAt desc
            """)
    List<ConnectionJpaEntity> findAllByStudentIdAndStatus(@Param("studentId") Long studentId,
                                                          @Param("status") ConnectionStatus status);

    @Query("""
            select case when count(connection) > 0 then true else false end
            from ConnectionJpaEntity connection
            where ((connection.requester.id = :studentId and connection.recipient.id = :otherStudentId)
                or (connection.requester.id = :otherStudentId and connection.recipient.id = :studentId))
              and connection.status in :statuses
            """)
    boolean existsBetweenWithStatuses(@Param("studentId") Long studentId,
                                     @Param("otherStudentId") Long otherStudentId,
                                     @Param("statuses") Set<ConnectionStatus> statuses);
}
