package com.utp.quesale.moderation.domain.repositories;

import com.utp.quesale.moderation.domain.aggregates.Report;
import com.utp.quesale.moderation.domain.valueobjects.ReportStatus;

import java.util.List;
import java.util.Optional;

public interface ReportRepository {
    Report save(Report report);

    Report update(Report report);

    Optional<Report> findById(Long id);

    List<Report> findAll();

    List<Report> findAllByStatus(ReportStatus status);
}
