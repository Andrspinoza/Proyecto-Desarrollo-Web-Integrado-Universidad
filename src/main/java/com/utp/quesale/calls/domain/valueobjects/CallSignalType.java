package com.utp.quesale.calls.domain.valueobjects;

import java.util.Locale;

public enum CallSignalType {
    OFFER,
    ANSWER,
    ICE_CANDIDATE,
    HANGUP,
    REJECT;

    public static CallSignalType fromWire(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Call signal type is required");
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown call signal type: " + value.trim());
        }
    }

    public String wire() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    public boolean isTerminal() {
        return this == HANGUP || this == REJECT;
    }
}
