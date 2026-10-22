package com.utp.quesale.profiles.application.commands;

public record RemoveInterestFromProfileCommand(Long studentId, Long requesterStudentId, Long interestId) {
}
