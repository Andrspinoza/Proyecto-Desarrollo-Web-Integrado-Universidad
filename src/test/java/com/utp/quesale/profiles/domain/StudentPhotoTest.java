package com.utp.quesale.profiles.domain;

import com.utp.quesale.profiles.domain.valueobjects.StudentPhoto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudentPhotoTest {
    private static byte[] bytes(int... values) {
        byte[] data = new byte[Math.max(values.length, 64)];
        for (int i = 0; i < values.length; i++) data[i] = (byte) values[i];
        return data;
    }

    @Test void detectsJpegPngAndWebpByTheirSignatureNotByName() {
        assertEquals("image/jpeg", StudentPhoto.create(1L, bytes(0xFF, 0xD8, 0xFF, 0xE0)).contentType());
        assertEquals("image/png", StudentPhoto.create(1L, bytes(0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A)).contentType());
        assertEquals("image/webp", StudentPhoto.create(1L,
                bytes('R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P')).contentType());
    }

    @Test void rejectsEverythingThatIsNotAnImage() {
        assertThrows(IllegalArgumentException.class, () -> StudentPhoto.create(1L, "<script>alert(1)</script>".getBytes()));
        assertThrows(IllegalArgumentException.class, () -> StudentPhoto.create(1L, "GIF89a............".getBytes()));
        assertThrows(IllegalArgumentException.class, () -> StudentPhoto.create(1L, new byte[0]));
        assertThrows(IllegalArgumentException.class, () -> StudentPhoto.create(1L, null));
    }

    @Test void rejectsPhotosLargerThanOneMegabyte() {
        byte[] big = new byte[StudentPhoto.MAX_BYTES + 1];
        big[0] = (byte) 0xFF; big[1] = (byte) 0xD8; big[2] = (byte) 0xFF;
        assertThrows(IllegalArgumentException.class, () -> StudentPhoto.create(1L, big));
        byte[] limit = new byte[StudentPhoto.MAX_BYTES];
        limit[0] = (byte) 0xFF; limit[1] = (byte) 0xD8; limit[2] = (byte) 0xFF;
        assertDoesNotThrow(() -> StudentPhoto.create(1L, limit));
    }

    @Test void everyPhotoGetsAnUnguessableUniqueKey() {
        var first = StudentPhoto.create(1L, bytes(0xFF, 0xD8, 0xFF));
        var second = StudentPhoto.create(1L, bytes(0xFF, 0xD8, 0xFF));
        assertNotEquals(first.key(), second.key());
        assertEquals(36, first.key().length());
    }

    @Test void rejectsAnInvalidStudentId() {
        assertThrows(IllegalArgumentException.class, () -> StudentPhoto.create(0L, bytes(0xFF, 0xD8, 0xFF)));
        assertThrows(IllegalArgumentException.class, () -> StudentPhoto.create(null, bytes(0xFF, 0xD8, 0xFF)));
    }
}
