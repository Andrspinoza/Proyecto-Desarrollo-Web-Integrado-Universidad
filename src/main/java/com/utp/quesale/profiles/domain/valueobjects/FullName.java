package com.utp.quesale.profiles.domain.valueobjects;

public record FullName(String firstName, String lastName) {
    private static final int MAX_LENGTH = 60;

    public FullName {
        firstName = validPart(firstName, "First name");
        lastName = validPart(lastName, "Last name");
    }

    private static String validPart(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        String trimmed = value.trim();
        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(field + " must be at most " + MAX_LENGTH + " characters");
        }
        return trimmed;
    }
}
