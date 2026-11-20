package com.utp.quesale.profiles.application.commands;

import com.utp.quesale.profiles.domain.valueobjects.Gender;
import com.utp.quesale.profiles.domain.valueobjects.Visibility;

import java.time.LocalDate;
import java.util.Set;

public record UpsertProfileCommand(Long studentId, String career, Integer cycle, String description,
                                   Visibility visibility, Gender gender, LocalDate birthDate, String campus,
                                   Set<Long> interestIds, Set<Long> skillIds) {
}
