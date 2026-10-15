package com.utp.quesale.iam.domain.valueobjects;

import java.util.Locale;

// Dominio de correo institucional, por ejemplo "utp.edu.pe"
public record EmailDomain(String value) {

    public EmailDomain {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Email domain is required");
        value = value.trim().toLowerCase(Locale.ROOT);
        if (value.startsWith("@")) value = value.substring(1);
        if (!value.matches("^[a-z0-9-]+(\\.[a-z0-9-]+)+$"))
            throw new IllegalArgumentException("Email domain format is invalid");
    }
}
