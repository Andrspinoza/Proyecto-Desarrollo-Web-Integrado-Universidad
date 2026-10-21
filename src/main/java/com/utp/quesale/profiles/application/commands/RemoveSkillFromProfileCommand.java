package com.utp.quesale.profiles.application.commands;

public record RemoveSkillFromProfileCommand(Long studentId, Long requesterStudentId, Long skillId) {
}
