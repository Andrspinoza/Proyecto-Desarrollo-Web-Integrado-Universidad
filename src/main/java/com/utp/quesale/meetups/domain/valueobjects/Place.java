package com.utp.quesale.meetups.domain.valueobjects;

public record Place(String name, String address, String district) {

    private static final int NAME_MAX_LENGTH = 120;
    private static final int ADDRESS_MAX_LENGTH = 200;
    private static final int DISTRICT_MAX_LENGTH = 60;

    public Place {
        name = validateRequired(name, "Place name", NAME_MAX_LENGTH);
        address = validateRequired(address, "Place address", ADDRESS_MAX_LENGTH);
        district = validateRequired(district, "Place district", DISTRICT_MAX_LENGTH);
    }

    private static String validateRequired(String value, String fieldName, int maxLength) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("%s is required".formatted(fieldName));

        String trimmed = value.trim();
        if (trimmed.length() > maxLength)
            throw new IllegalArgumentException(
                    "%s must be at most %d characters".formatted(fieldName, maxLength));

        return trimmed;
    }
}
