package com.utp.quesale.moderation.application.handlers.commands;

import com.utp.quesale.moderation.application.assemblers.ModerationDtoAssembler;
import com.utp.quesale.moderation.application.commands.CreateReportCommand;
import com.utp.quesale.moderation.application.commands.DismissReportCommand;
import com.utp.quesale.moderation.application.commands.ReviewReportCommand;
import com.utp.quesale.moderation.application.dtos.ReportDto;
import com.utp.quesale.moderation.domain.aggregates.Report;
import com.utp.quesale.moderation.domain.exceptions.ReportNotFoundException;
import com.utp.quesale.moderation.domain.repositories.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportCommandHandler {

    private final ReportRepository reportRepository;

    public ReportCommandHandler(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Transactional
    public ReportDto handle(CreateReportCommand command) {
        Report saved = reportRepository.save(new Report(
                command.reporterId(),
                command.targetType(),
                command.targetId(),
                command.reason(),
                command.detail()));
        return ModerationDtoAssembler.toDto(saved);
    }

    @Transactional
    public ReportDto handle(ReviewReportCommand command) {
        Report report = findReport(command.reportId());
        report.review(command.adminAccountId(), command.resolution());
        return ModerationDtoAssembler.toDto(reportRepository.update(report));
    }

    @Transactional
    public ReportDto handle(DismissReportCommand command) {
        Report report = findReport(command.reportId());
        report.dismiss(command.adminAccountId(), command.resolution());
        return ModerationDtoAssembler.toDto(reportRepository.update(report));
    }

    private Report findReport(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException(reportId));
    }
}
