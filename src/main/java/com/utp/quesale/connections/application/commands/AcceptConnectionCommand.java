package com.utp.quesale.connections.application.commands;

public record AcceptConnectionCommand(Long connectionId, Long recipientId) {
}
