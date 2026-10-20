package com.utp.quesale.moderation.infrastructure.persistence.jpa.mappers;

import com.utp.quesale.moderation.domain.aggregates.Report;
import com.utp.quesale.moderation.infrastructure.persistence.jpa.entities.ReportJpaEntity;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ReportPersistenceMapper {

    public Report toDomain(ReportJpaEntity entity) {
        return new Report(
                entity.getId(),
                entity.getReporter().getId(),
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getReason(),
                entity.getDetail(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getReviewedByAccountId(),
                entity.getReviewedAt(),
                entity.getResolution()
        );
    }

    public ReportJpaEntity toEntity(Report report, StudentJpaEntity reporter) {
        ReportJpaEntity entity = new ReportJpaEntity();
        entity.setId(report.getId());
        entity.setReporter(reporter);
        entity.setTargetType(report.getTargetType());
        entity.setTargetId(report.getTargetId());
        entity.setReason(report.getReason());
        entity.setDetail(report.getDetail());
        entity.setStatus(report.getStatus());
        entity.setCreatedAt(report.getCreatedAt());
        entity.setReviewedByAccountId(report.getReviewedByAccountId());
        entity.setReviewedAt(report.getReviewedAt());
        entity.setResolution(report.getResolution());
        return entity;
    }
}
