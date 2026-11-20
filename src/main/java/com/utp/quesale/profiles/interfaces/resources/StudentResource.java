package com.utp.quesale.profiles.interfaces.resources;

import java.time.Instant;

public record StudentResource(Long id, String recordId, Long universityId, String email,
                              String firstName, String lastName, Instant createdAt,
                              ProfileResource profile, String photoUrl) {
    public StudentResource(Long id, String recordId, Long universityId, String email,
                           String firstName, String lastName, Instant createdAt, ProfileResource profile) {
        this(id, recordId, universityId, email, firstName, lastName, createdAt, profile, null);
    }

    public StudentResource withPhotoUrl(String photoUrl) {
        return new StudentResource(id, recordId, universityId, email, firstName, lastName, createdAt, profile, photoUrl);
    }
}
