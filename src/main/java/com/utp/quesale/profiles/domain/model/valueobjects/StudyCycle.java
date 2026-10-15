package com.utp.quesale.profiles.domain.model.valueobjects;

public record StudyCycle(int value) {
    public StudyCycle {
        if (value < 1 || value > 10) {
            throw new IllegalArgumentException("Study cycle must be between 1 and 10");
        }
    }
}
