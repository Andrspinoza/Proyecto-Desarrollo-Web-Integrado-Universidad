package com.utp.quesale.connections.application.commands;

public record RejectConnectionCommand(Long connectionId, Long recipientId) {
}
