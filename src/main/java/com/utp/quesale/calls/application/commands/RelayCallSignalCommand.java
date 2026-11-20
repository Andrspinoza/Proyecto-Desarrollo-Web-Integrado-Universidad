package com.utp.quesale.calls.application.commands;

public record RelayCallSignalCommand(Long fromStudentId, Long toStudentId, String type, String payload) {
}
