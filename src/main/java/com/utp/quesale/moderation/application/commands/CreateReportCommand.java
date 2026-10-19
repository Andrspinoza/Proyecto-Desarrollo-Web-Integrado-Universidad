package com.utp.quesale.moderation.application.commands;

import com.utp.quesale.moderation.domain.valueobjects.ReportReason;
import com.utp.quesale.moderation.domain.valueobjects.ReportTargetType;

public record CreateReportCommand(
        Long reporterId,
        ReportTargetType targetType,
        Long targetId,
        ReportReason reason,
        String detail) {
}
