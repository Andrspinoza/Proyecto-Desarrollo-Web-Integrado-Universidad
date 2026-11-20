package com.utp.quesale.profiles.application.dtos;

import com.utp.quesale.profiles.domain.valueobjects.Gender;

import java.util.List;

public record RecommendationDto(Long id, String firstName, String lastName, Integer age, Gender gender,
                                String career, int cycle, String campus, String description,
                                int affinityPercent, List<String> sharedInterests, List<String> sharedSkills,
                                boolean sameCareer, boolean sameCampus, int cycleDifference,
                                List<String> interests) {
}
