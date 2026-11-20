package com.utp.quesale.profiles.interfaces.resources;

import com.utp.quesale.profiles.domain.valueobjects.Gender;
import com.utp.quesale.profiles.domain.valueobjects.Visibility;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

public record UpsertProfileResource(@NotBlank @Size(max = 80) String career,
                                    @NotNull @Min(1) @Max(10) Integer cycle,
                                    @Size(max = 500) String description,
                                    @NotNull Visibility visibility,
                                    Gender gender,
                                    @Past LocalDate birthDate,
                                    @Size(max = 60) String campus,
                                    @Size(max = 15) Set<Long> interestIds,
                                    @Size(max = 15) Set<Long> skillIds) {
}
