package com.utp.quesale.moderation.interfaces.assemblers;

import com.utp.quesale.moderation.domain.aggregates.Report;
import com.utp.quesale.moderation.domain.valueobjects.ReportReason;
import com.utp.quesale.moderation.domain.valueobjects.ReportStatus;
import com.utp.quesale.moderation.domain.valueobjects.ReportTargetType;
import com.utp.quesale.moderation.interfaces.resources.CreateReportResource;
import com.utp.quesale.moderation.interfaces.resources.ReportResource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReportResourceAssemblerTest {

    @Test
    void createsDomainReportFromResourceAndReporterId() {
        CreateReportResource resource = new CreateReportResource(
                ReportTargetType.STUDENT, 7L, ReportReason.HARASSMENT, "Repeated messages");

        Report report = ReportResourceAssembler.toDomainFromResource(3L, resource);

        assertEquals(3L, report.getReporterId());
        assertEquals(ReportTargetType.STUDENT, report.getTargetType());
        assertEquals(7L, report.getTargetId());
        assertEquals(ReportReason.HARASSMENT, report.getReason());
        assertEquals("Repeated messages", report.getDetail());
        assertEquals(ReportStatus.OPEN, report.getStatus());
    }

    @Test
    void createsResourceFromDomainReport() {
        Report report = new Report(1L, 3L, ReportTargetType.MESSAGE, 9L,
                ReportReason.SPAM, null, ReportStatus.OPEN, java.time.Instant.parse("2026-10-11T12:00:00Z"),
                null, null, null);

        ReportResource resource = ReportResourceAssembler.toResourceFromEntity(report);

        assertEquals(report.getId(), resource.id());
        assertEquals(report.getReporterId(), resource.reporterId());
        assertEquals(report.getTargetType(), resource.targetType());
        assertEquals(report.getTargetId(), resource.targetId());
        assertEquals(report.getReason(), resource.reason());
        assertEquals(report.getStatus(), resource.status());
        assertEquals(report.getCreatedAt(), resource.createdAt());
    }
}
