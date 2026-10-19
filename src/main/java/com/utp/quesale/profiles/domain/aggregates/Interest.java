package com.utp.quesale.profiles.domain.aggregates;

import com.utp.quesale.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

public class Interest extends AbstractDomainAggregateRoot<Interest> {
    private final Long id;
    private final String name;

    public Interest(String name) {
        this(null, name);
    }

    public Interest(Long id, String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Interest name is required");
        }
        String trimmed = name.trim();
        if (trimmed.length() > 40) {
            throw new IllegalArgumentException("Interest name must be at most 40 characters");
        }
        this.id = id;
        this.name = trimmed;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
}
