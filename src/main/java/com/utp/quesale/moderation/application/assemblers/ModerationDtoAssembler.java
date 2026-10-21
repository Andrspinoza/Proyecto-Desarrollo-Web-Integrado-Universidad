package com.utp.quesale.moderation.application.assemblers;

import com.utp.quesale.moderation.application.dtos.BlockDto;
import com.utp.quesale.moderation.application.dtos.ReportDto;
import com.utp.quesale.moderation.domain.aggregates.Block;
import com.utp.quesale.moderation.domain.aggregates.Report;

public final class ModerationDtoAssembler {

    private ModerationDtoAssembler() {
    }

    public static ReportDto toDto(Report report) {
        return new ReportDto(
                report.getId(),
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

    public static BlockDto toDto(Block block) {
        return new BlockDto(
                block.getId(),
                block.getBlockerId(),
                block.getBlockedId(),
                block.getCreatedAt());
    }
}
