package com.utp.quesale.profiles.application.commands;

public record CreateStudentCommand(Long universityId, String email, String firstName, String lastName) {
}
