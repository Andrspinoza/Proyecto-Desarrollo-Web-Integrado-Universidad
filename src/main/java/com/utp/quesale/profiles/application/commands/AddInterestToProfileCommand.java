package com.utp.quesale.profiles.application.commands;

public record AddInterestToProfileCommand(Long studentId, Long requesterStudentId, Long interestId) {
}
