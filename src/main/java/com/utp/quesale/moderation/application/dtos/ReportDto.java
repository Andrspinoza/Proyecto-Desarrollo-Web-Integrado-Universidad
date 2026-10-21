package com.utp.quesale.moderation.application.dtos;

import com.utp.quesale.moderation.domain.valueobjects.ReportReason;
import com.utp.quesale.moderation.domain.valueobjects.ReportStatus;
import com.utp.quesale.moderation.domain.valueobjects.ReportTargetType;

import java.time.Instant;

public record ReportDto(
        Long id,
        Long reporterId,
        ReportTargetType targetType,
        Long targetId,
        ReportReason reason,
        String detail,
        ReportStatus status,
        Instant createdAt,
        Long reviewedByAccountId,
        Instant reviewedAt,
        String resolution) {
}
