package com.utp.quesale.profiles.domain.entities;

import com.utp.quesale.profiles.domain.valueobjects.Career;
import com.utp.quesale.profiles.domain.valueobjects.StudyCycle;
import com.utp.quesale.profiles.domain.valueobjects.Visibility;

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
    public Set<Long> getInterestIds() { return Collections.unmodifiableSet(interestIds); }
    public Set<Long> getSkillIds() { return Collections.unmodifiableSet(skillIds); }
}
