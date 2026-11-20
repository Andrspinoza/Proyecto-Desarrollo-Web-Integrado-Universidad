package com.utp.quesale.profiles.domain.valueobjects;

import java.util.UUID;

public record StudentPhoto(Long studentId, String key, String contentType, byte[] data) {
    public static final int MAX_BYTES = 1_048_576;

    public StudentPhoto {
        if (studentId == null || studentId <= 0) throw new IllegalArgumentException("Student id must be positive");
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Photo key is required");
        if (contentType == null || contentType.isBlank()) throw new IllegalArgumentException("Photo content type is required");
        if (data == null || data.length == 0) throw new IllegalArgumentException("Photo is empty");
        if (data.length > MAX_BYTES) throw new IllegalArgumentException("Photo must be at most 1 MB");
    }

    public static StudentPhoto create(Long studentId, byte[] data) {
        if (data == null || data.length == 0) throw new IllegalArgumentException("Photo is empty");
        if (data.length > MAX_BYTES) throw new IllegalArgumentException("Photo must be at most 1 MB");
        return new StudentPhoto(studentId, UUID.randomUUID().toString(), detectContentType(data), data);
    }

    static String detectContentType(byte[] data) {
        if (data.length >= 3 && (data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8 && (data[2] & 0xFF) == 0xFF)
            return "image/jpeg";
        if (data.length >= 8 && (data[0] & 0xFF) == 0x89 && data[1] == 'P' && data[2] == 'N' && data[3] == 'G'
                && data[4] == 0x0D && data[5] == 0x0A && data[6] == 0x1A && data[7] == 0x0A)
            return "image/png";
        if (data.length >= 12 && data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F'
                && data[8] == 'W' && data[9] == 'E' && data[10] == 'B' && data[11] == 'P')
            return "image/webp";
        throw new IllegalArgumentException("Photo must be a JPEG, PNG or WebP image");
    }
}
