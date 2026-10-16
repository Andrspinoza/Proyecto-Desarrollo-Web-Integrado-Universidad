package com.utp.quesale.meetups.domain.valueobjects;

public record Capacity(int value) {

    private static final int MIN_CAPACITY = 2;
    private static final int MAX_CAPACITY = 50;

    public Capacity {
        if (value < MIN_CAPACITY || value > MAX_CAPACITY)
            throw new IllegalArgumentException(
                    "Capacity must be between %d and %d".formatted(MIN_CAPACITY, MAX_CAPACITY));
    }
}
