package com.utp.quesale.profiles.domain.valueobjects;

public record Career(String value) {
    private static final int MAX_LENGTH = 80;

    public Career {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Career is required");
        }
        value = value.trim();
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Career must be at most " + MAX_LENGTH + " characters");
        }
    }
}
