package com.utp.quesale.moderation.interfaces.assemblers;

import com.utp.quesale.moderation.domain.aggregates.Report;
import com.utp.quesale.moderation.interfaces.resources.CreateReportResource;
import com.utp.quesale.moderation.interfaces.resources.ReportResource;

public final class ReportResourceAssembler {

    private ReportResourceAssembler() {
    }

    public static Report toDomainFromResource(Long reporterId, CreateReportResource resource) {
        return new Report(reporterId, resource.targetType(), resource.targetId(),
                resource.reason(), resource.detail());
    }

    public static ReportResource toResourceFromEntity(Report report) {
        return new ReportResource(report.getId(), report.getReporterId(), report.getTargetType(),
                report.getTargetId(), report.getReason(), report.getDetail(), report.getStatus(),
                report.getCreatedAt(), report.getReviewedByAccountId(), report.getReviewedAt(),
                report.getResolution());
    }
}
