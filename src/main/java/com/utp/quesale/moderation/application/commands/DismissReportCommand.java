package com.utp.quesale.moderation.application.commands;

public record DismissReportCommand(Long reportId, Long adminAccountId, String resolution) {
}
