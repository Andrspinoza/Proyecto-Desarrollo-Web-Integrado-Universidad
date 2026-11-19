package com.utp.quesale.iam.domain.valueobjects;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

public final class VerificationToken {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;

    private final String raw;
    private final String hash;

    private VerificationToken(String raw) {
        this.raw = raw;
        this.hash = hashOf(raw);
    }

    public static VerificationToken generate() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return new VerificationToken(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
    }

    public static String hashOf(String raw) {
        if (raw == null || raw.isBlank()) throw new IllegalArgumentException("Verification token is required");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.trim().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    public String raw() {
        return raw;
    }

    public String hash() {
        return hash;
    }
}
