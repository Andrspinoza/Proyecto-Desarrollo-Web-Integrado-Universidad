package com.utp.quesale.messaging.application.commands;

public record SendMessageCommand(Long senderId, Long recipientId, String content) {
}
