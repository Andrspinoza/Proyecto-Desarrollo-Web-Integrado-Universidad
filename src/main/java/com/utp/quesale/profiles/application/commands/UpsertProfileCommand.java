package com.utp.quesale.profiles.application.commands;

import com.utp.quesale.profiles.domain.valueobjects.Visibility;

public record UpsertProfileCommand(Long studentId, Long requesterStudentId, String career, Integer cycle,
                                   String description, Visibility visibility) {
}
