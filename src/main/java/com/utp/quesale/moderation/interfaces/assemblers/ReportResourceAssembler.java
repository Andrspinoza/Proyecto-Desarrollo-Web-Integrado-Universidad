package com.utp.quesale.moderation.interfaces.assemblers;

import com.utp.quesale.moderation.application.commands.CreateReportCommand;
import com.utp.quesale.moderation.application.commands.DismissReportCommand;
import com.utp.quesale.moderation.application.commands.ReviewReportCommand;
import com.utp.quesale.moderation.application.dtos.ReportDto;
import com.utp.quesale.moderation.domain.aggregates.Report;
import com.utp.quesale.moderation.interfaces.resources.CreateReportResource;
import com.utp.quesale.moderation.interfaces.resources.ResolveReportResource;
import com.utp.quesale.moderation.interfaces.resources.ReportResource;

public final class ReportResourceAssembler {

    private ReportResourceAssembler() {
    }

    public static Report toDomainFromResource(Long reporterId, CreateReportResource resource) {
        return new Report(reporterId, resource.targetType(), resource.targetId(),
                resource.reason(), resource.detail());
    }

    public static CreateReportCommand toCreateCommand(Long reporterId, CreateReportResource resource) {
        return new CreateReportCommand(reporterId, resource.targetType(), resource.targetId(),
                resource.reason(), resource.detail());
    }

    public static ReviewReportCommand toReviewCommand(
            Long reportId, Long adminAccountId, ResolveReportResource resource) {
        return new ReviewReportCommand(reportId, adminAccountId, resource.resolution());
    }

    public static DismissReportCommand toDismissCommand(
            Long reportId, Long adminAccountId, ResolveReportResource resource) {
        return new DismissReportCommand(reportId, adminAccountId, resource.resolution());
    }

    public static ReportResource toResourceFromDto(ReportDto dto) {
        return new ReportResource(dto.id(), dto.reporterId(), dto.targetType(), dto.targetId(),
                dto.reason(), dto.detail(), dto.status(), dto.createdAt(), dto.reviewedByAccountId(),
                dto.reviewedAt(), dto.resolution());
    }

    public static ReportResource toResourceFromEntity(Report report) {
        return new ReportResource(report.getId(), report.getReporterId(), report.getTargetType(),
                report.getTargetId(), report.getReason(), report.getDetail(), report.getStatus(),
                report.getCreatedAt(), report.getReviewedByAccountId(), report.getReviewedAt(),
                report.getResolution());
    }
}
