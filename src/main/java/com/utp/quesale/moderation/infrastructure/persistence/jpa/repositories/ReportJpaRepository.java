package com.utp.quesale.moderation.infrastructure.persistence.jpa.repositories;

import com.utp.quesale.moderation.domain.valueobjects.ReportStatus;
import com.utp.quesale.moderation.infrastructure.persistence.jpa.entities.ReportJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportJpaRepository extends JpaRepository<ReportJpaEntity, Long> {
    List<ReportJpaEntity> findAllByStatus(ReportStatus status);
}
