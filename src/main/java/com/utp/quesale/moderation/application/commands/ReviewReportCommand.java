package com.utp.quesale.moderation.application.commands;

public record ReviewReportCommand(Long reportId, Long adminAccountId, String resolution) {
}
