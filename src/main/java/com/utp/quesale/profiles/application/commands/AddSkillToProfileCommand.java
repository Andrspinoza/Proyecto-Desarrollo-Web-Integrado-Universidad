package com.utp.quesale.profiles.application.commands;

public record AddSkillToProfileCommand(Long studentId, Long requesterStudentId, Long skillId) {
}
