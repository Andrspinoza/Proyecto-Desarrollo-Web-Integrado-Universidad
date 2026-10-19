package com.utp.quesale.moderation.infrastructure.persistence.jpa.repositories;

import com.utp.quesale.moderation.infrastructure.persistence.jpa.entities.BlockJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BlockJpaRepository extends JpaRepository<BlockJpaEntity, Long> {
    Optional<BlockJpaEntity> findByBlocker_IdAndBlocked_Id(Long blockerId, Long blockedId);

    List<BlockJpaEntity> findAllByBlocker_Id(Long blockerId);

    @Query("""
            select case when count(block) > 0 then true else false end
            from BlockJpaEntity block
            where (block.blocker.id = :studentId and block.blocked.id = :otherStudentId)
               or (block.blocker.id = :otherStudentId and block.blocked.id = :studentId)
            """)
    boolean existsBetween(@Param("studentId") Long studentId,
                          @Param("otherStudentId") Long otherStudentId);

    long deleteByBlocker_IdAndBlocked_Id(Long blockerId, Long blockedId);
}
