package com.utp.quesale.profiles.domain.entities;

import com.utp.quesale.profiles.domain.valueobjects.Career;
import com.utp.quesale.profiles.domain.valueobjects.Gender;
import com.utp.quesale.profiles.domain.valueobjects.StudyCycle;
import com.utp.quesale.profiles.domain.valueobjects.Visibility;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Profile {
    private static final int MAX_ITEMS = 15;
    private Career career;
    private StudyCycle cycle;
    private String description;
    private Visibility visibility;
    private Gender gender = Gender.UNDISCLOSED;
    private LocalDate birthDate;
    private String campus;
    private final Set<Long> interestIds;
    private final Set<Long> skillIds;

    public Profile(Career career, StudyCycle cycle, String description, Visibility visibility) {
        this(career, cycle, description, visibility, List.of(), List.of());
    }

    public Profile(Career career, StudyCycle cycle, String description, Visibility visibility,
                   Collection<Long> interestIds, Collection<Long> skillIds) {
        update(career, cycle, description, visibility);
        this.interestIds = validIds(interestIds);
        this.skillIds = validIds(skillIds);
    }

    public void update(Career career, StudyCycle cycle, String description, Visibility visibility) {
        if (career == null) throw new IllegalArgumentException("Career is required");
        if (cycle == null) throw new IllegalArgumentException("Study cycle is required");
        if (visibility == null) throw new IllegalArgumentException("Visibility is required");
        if (description != null && description.length() > 500)
            throw new IllegalArgumentException("Description must be at most 500 characters");
        this.career = career;
        this.cycle = cycle;
        this.description = description;
        this.visibility = visibility;
    }

    public void updateDetails(Gender gender, LocalDate birthDate, String campus, LocalDate today) {
        if (birthDate != null) {
            if (!birthDate.isBefore(today)) throw new IllegalArgumentException("Birth date must be in the past");
            int age = Period.between(birthDate, today).getYears();
            if (age < 16 || age > 100) throw new IllegalArgumentException("Age must be between 16 and 100");
        }
        if (campus != null && campus.strip().length() > 60)
            throw new IllegalArgumentException("Campus must be at most 60 characters");
        this.gender = gender == null ? Gender.UNDISCLOSED : gender;
        this.birthDate = birthDate;
        this.campus = campus == null || campus.isBlank() ? null : campus.strip();
    }

    public void restoreDetails(Gender gender, LocalDate birthDate, String campus) {
        this.gender = gender == null ? Gender.UNDISCLOSED : gender;
        this.birthDate = birthDate;
        this.campus = campus;
    }

    public Integer ageOn(LocalDate today) {
        return birthDate == null ? null : Period.between(birthDate, today).getYears();
    }

    public void replaceInterests(Collection<Long> ids) {
        Set<Long> valid = validIds(ids);
        interestIds.clear();
        interestIds.addAll(valid);
    }

    public void replaceSkills(Collection<Long> ids) {
        Set<Long> valid = validIds(ids);
        skillIds.clear();
        skillIds.addAll(valid);
    }

    public void addInterest(Long interestId) { addId(interestIds, interestId); }
    public void removeInterest(Long interestId) { interestIds.remove(validId(interestId)); }
    public void addSkill(Long skillId) { addId(skillIds, skillId); }
    public void removeSkill(Long skillId) { skillIds.remove(validId(skillId)); }

    private static void addId(Set<Long> ids, Long id) {
        validId(id);
        if (ids.contains(id)) throw new IllegalStateException("Item is already in the profile");
        if (ids.size() >= MAX_ITEMS) throw new IllegalStateException("Profile allows at most 15 items per catalog");
        ids.add(id);
    }

    private static Long validId(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Catalog id must be positive");
        return id;
    }

    private static Set<Long> validIds(Collection<Long> values) {
        if (values == null) throw new IllegalArgumentException("Catalog ids are required");
        if (values.size() > MAX_ITEMS) throw new IllegalArgumentException("Profile allows at most 15 items per catalog");
        Set<Long> ids = new LinkedHashSet<>();
        for (Long id : values) {
            if (!ids.add(validId(id))) throw new IllegalArgumentException("Catalog ids must not repeat");
        }
        return ids;
    }

    public Career getCareer() { return career; }
    public StudyCycle getCycle() { return cycle; }
    public String getDescription() { return description; }
    public Visibility getVisibility() { return visibility; }
    public Gender getGender() { return gender; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getCampus() { return campus; }
    public Set<Long> getInterestIds() { return Collections.unmodifiableSet(interestIds); }
    public Set<Long> getSkillIds() { return Collections.unmodifiableSet(skillIds); }
}
