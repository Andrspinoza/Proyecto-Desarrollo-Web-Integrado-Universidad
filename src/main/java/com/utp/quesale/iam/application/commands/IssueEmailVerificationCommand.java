package com.utp.quesale.iam.application.commands;

public record IssueEmailVerificationCommand(Long studentId, String email, String recipientName) {
}
