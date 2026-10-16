package com.utp.quesale.profiles.domain.valueobjects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StudentRecordIdTest {
    @Test
    void generatesValidUuid() {
        StudentRecordId recordId = new StudentRecordId();

        assertDoesNotThrow(() -> UUID.fromString(recordId.studentRecordId()));
    }

    @Test
    void preservesExistingUuid() {
        String value = "e94a824b-43ad-4d59-a750-9bd4046c11a9";

        assertEquals(value, new StudentRecordId(value).studentRecordId());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "not-a-uuid", "e94a824b-43ad-4d59-a750-9bd4046c11az"})
    void rejectsMissingOrInvalidUuid(String value) {
        assertThrows(IllegalArgumentException.class, () -> new StudentRecordId(value));
    }
}
