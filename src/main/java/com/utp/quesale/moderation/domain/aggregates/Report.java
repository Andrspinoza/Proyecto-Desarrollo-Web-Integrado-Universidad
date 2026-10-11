package com.utp.quesale.moderation.domain.aggregates;

import com.utp.quesale.moderation.domain.valueobjects.ReportReason;
import com.utp.quesale.moderation.domain.valueobjects.ReportStatus;
import com.utp.quesale.moderation.domain.valueobjects.ReportTargetType;
import com.utp.quesale.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Instant;

public class Report extends AbstractDomainAggregateRoot<Report> {

    private Long id;
    private final Long reporterId;
    private final ReportTargetType targetType;
    private final Long targetId;
    private final ReportReason reason;
    private final String detail;
    private ReportStatus status;
    private final Instant createdAt;
    private Long reviewedByAccountId;
    private Instant reviewedAt;
    private String resolution;

    public Report(Long reporterId, ReportTargetType targetType, Long targetId, ReportReason reason, String detail) {
        this(null, reporterId, targetType, targetId, reason, detail, ReportStatus.OPEN,
                Instant.now(), null, null, null);
    }

    public Report(Long id, Long reporterId, ReportTargetType targetType, Long targetId, ReportReason reason,
                  String detail, ReportStatus status, Instant createdAt, Long reviewedByAccountId,
                  Instant reviewedAt, String resolution) {
        if (reporterId == null) throw new IllegalArgumentException("Reporter ID is required");
        if (targetType == null) throw new IllegalArgumentException("Target type is required");
        if (targetId == null) throw new IllegalArgumentException("Target ID is required");
        if (reason == null) throw new IllegalArgumentException("Report reason is required");
        if (status == null) throw new IllegalArgumentException("Report status is required");
        if (createdAt == null) throw new IllegalArgumentException("Created at is required");

        String validatedResolution = status == ReportStatus.OPEN
                ? validateOptionalText(resolution, "Resolution")
                : validateRequiredText(resolution, "Resolution");
        if (status == ReportStatus.OPEN && (reviewedByAccountId != null || reviewedAt != null || resolution != null)) {
            throw new IllegalArgumentException("Open reports cannot contain resolution data");
        }
        if (status != ReportStatus.OPEN && (reviewedByAccountId == null || reviewedAt == null)) {
            throw new IllegalArgumentException("Resolved reports require reviewer and review time");
        }
        if (reviewedAt != null && reviewedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("Review time cannot be before report creation");
        }

        this.id = id;
        this.reporterId = reporterId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
        this.detail = validateOptionalText(detail, "Detail");
        this.status = status;
        this.createdAt = createdAt;
        this.reviewedByAccountId = reviewedByAccountId;
        this.reviewedAt = reviewedAt;
        this.resolution = validatedResolution;
    }

    public void review(Long adminAccountId, String resolution) {
        resolve(adminAccountId, resolution, ReportStatus.REVIEWED);
    }

    public void dismiss(Long adminAccountId, String resolution) {
        resolve(adminAccountId, resolution, ReportStatus.DISMISSED);
    }

    private void resolve(Long adminAccountId, String resolution, ReportStatus resolvedStatus) {
        if (status != ReportStatus.OPEN) throw new IllegalStateException("Only open reports can be resolved");
        if (adminAccountId == null) throw new IllegalArgumentException("Admin account ID is required");
        String validatedResolution = validateRequiredText(resolution, "Resolution");

        Instant now = Instant.now();
        this.status = resolvedStatus;
        this.reviewedByAccountId = adminAccountId;
        this.reviewedAt = now;
        this.resolution = validatedResolution;
    }

    private static String validateOptionalText(String value, String fieldName) {
        if (value == null) return null;
        String trimmedValue = value.trim();
        if (trimmedValue.length() > 500) {
            throw new IllegalArgumentException(fieldName + " cannot exceed 500 characters");
        }
        return trimmedValue;
    }

    private static String validateRequiredText(String value, String fieldName) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(fieldName + " is required");
        return validateOptionalText(value, fieldName);
    }

    public Long getId() {
        return id;
    }

    public Long getReporterId() {
        return reporterId;
    }

    public ReportTargetType getTargetType() {
        return targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public ReportReason getReason() {
        return reason;
    }

    public String getDetail() {
        return detail;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Long getReviewedByAccountId() {
        return reviewedByAccountId;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public String getResolution() {
        return resolution;
    }
}
