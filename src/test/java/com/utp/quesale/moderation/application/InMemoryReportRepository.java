package com.utp.quesale.moderation.application;

import com.utp.quesale.moderation.domain.aggregates.Report;
import com.utp.quesale.moderation.domain.repositories.ReportRepository;
import com.utp.quesale.moderation.domain.valueobjects.ReportStatus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class InMemoryReportRepository implements ReportRepository {

    private final Map<Long, Report> reports = new LinkedHashMap<>();
    private long nextId = 1;

    @Override
    public Report save(Report report) {
        Report saved = copy(report, nextId++);
        reports.put(saved.getId(), saved);
        return saved;
    }

    @Override
    public Report update(Report report) {
        if (!reports.containsKey(report.getId())) {
            throw new IllegalStateException("Report does not exist");
        }
        Report updated = copy(report, report.getId());
        reports.put(updated.getId(), updated);
        return updated;
    }

    @Override
    public Optional<Report> findById(Long id) {
        return Optional.ofNullable(reports.get(id));
    }

    @Override
    public List<Report> findAll() {
        return new ArrayList<>(reports.values());
    }

    @Override
    public List<Report> findAllByStatus(ReportStatus status) {
        return reports.values().stream()
                .filter(report -> report.getStatus() == status)
                .toList();
    }

    private static Report copy(Report report, Long id) {
        return new Report(
                id,
                report.getReporterId(),
                report.getTargetType(),
                report.getTargetId(),
                report.getReason(),
                report.getDetail(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getReviewedByAccountId(),
                report.getReviewedAt(),
                report.getResolution());
    }
}
