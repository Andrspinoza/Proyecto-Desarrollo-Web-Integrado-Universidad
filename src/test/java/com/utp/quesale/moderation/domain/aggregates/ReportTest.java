package com.utp.quesale.moderation.domain.aggregates;

import com.utp.quesale.moderation.domain.valueobjects.ReportReason;
import com.utp.quesale.moderation.domain.valueobjects.ReportStatus;
import com.utp.quesale.moderation.domain.valueobjects.ReportTargetType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReportTest {

    @Test
    void createsAnOpenReport() {
        Report report = new Report(1L, ReportTargetType.STUDENT, 2L, ReportReason.HARASSMENT, "  Repeated messages  ");

        assertEquals(1L, report.getReporterId());
        assertEquals(ReportTargetType.STUDENT, report.getTargetType());
        assertEquals(2L, report.getTargetId());
        assertEquals(ReportReason.HARASSMENT, report.getReason());
        assertEquals("Repeated messages", report.getDetail());
        assertEquals(ReportStatus.OPEN, report.getStatus());
        assertNotNull(report.getCreatedAt());
        assertNull(report.getReviewedAt());
    }

    @Test
    void rejectsDetailsLongerThan500Characters() {
        String detail = "x".repeat(501);

        assertThrows(IllegalArgumentException.class,
                () -> new Report(1L, ReportTargetType.STUDENT, 2L, ReportReason.OTHER, detail));
    }

    @Test
    void reviewsAnOpenReportAndStoresTheDecision() {
        Report report = new Report(1L, ReportTargetType.MESSAGE, 7L, ReportReason.SPAM, null);

        report.review(9L, "  Reviewed and actioned  ");

        assertEquals(ReportStatus.REVIEWED, report.getStatus());
        assertEquals(9L, report.getReviewedByAccountId());
        assertNotNull(report.getReviewedAt());
        assertEquals("Reviewed and actioned", report.getResolution());
    }

    @Test
    void dismissesAnOpenReport() {
        Report report = new Report(1L, ReportTargetType.COMMUNITY, 3L, ReportReason.OTHER, null);

        report.dismiss(9L, "No violation found");

        assertEquals(ReportStatus.DISMISSED, report.getStatus());
        assertEquals(9L, report.getReviewedByAccountId());
    }

    @Test
    void rejectsResolvingAReportMoreThanOnce() {
        Report report = new Report(1L, ReportTargetType.STUDENT, 2L, ReportReason.FAKE_PROFILE, null);
        report.review(9L, "Verified");

        assertThrows(IllegalStateException.class, () -> report.dismiss(9L, "Second decision"));
    }

    @Test
    void rejectsResolutionWithoutAdminOrText() {
        Report report = new Report(1L, ReportTargetType.STUDENT, 2L, ReportReason.OTHER, null);

        assertThrows(IllegalArgumentException.class, () -> report.review(null, "Reviewed"));
        assertThrows(IllegalArgumentException.class, () -> report.review(9L, " "));
    }

    @Test
    void rejectsOpenReportsWithResolutionData() {
        Instant createdAt = Instant.now();

        assertThrows(IllegalArgumentException.class, () -> new Report(
                1L, 1L, ReportTargetType.STUDENT, 2L, ReportReason.OTHER, null,
                ReportStatus.OPEN, createdAt, 9L, createdAt, "Reviewed"));
    }

    @Test
    void rejectsResolvedReportsWithoutCompleteResolutionData() {
        Instant createdAt = Instant.now();

        assertThrows(IllegalArgumentException.class, () -> new Report(
                1L, 1L, ReportTargetType.STUDENT, 2L, ReportReason.OTHER, null,
                ReportStatus.REVIEWED, createdAt, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new Report(
                1L, 1L, ReportTargetType.STUDENT, 2L, ReportReason.OTHER, null,
                ReportStatus.DISMISSED, createdAt, 9L, createdAt, " "));
    }

    @Test
    void rejectsReviewTimeBeforeCreation() {
        Instant createdAt = Instant.now();

        assertThrows(IllegalArgumentException.class, () -> new Report(
                1L, 1L, ReportTargetType.STUDENT, 2L, ReportReason.OTHER, null,
                ReportStatus.REVIEWED, createdAt, 9L, createdAt.minusSeconds(1), "Reviewed"));
    }
}
