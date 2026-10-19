package com.utp.quesale.communities.domain;

import com.utp.quesale.communities.domain.entities.CommunityPost;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommunityPostTest {

    @Test
    @DisplayName("El contenido es obligatorio")
    void rejectsBlankContent() {
        assertThrows(IllegalArgumentException.class, () -> new CommunityPost(1L, 10L, " "));
        assertThrows(IllegalArgumentException.class, () -> new CommunityPost(1L, 10L, null));
    }

    @Test
    @DisplayName("El contenido admite de 1 a 1000 caracteres")
    void validatesContentLength() {
        assertDoesNotThrow(() -> new CommunityPost(1L, 10L, "a"));
        assertDoesNotThrow(() -> new CommunityPost(1L, 10L, "a".repeat(1000)));
        assertThrows(IllegalArgumentException.class, () -> new CommunityPost(1L, 10L, "a".repeat(1001)));
    }

    @Test
    @DisplayName("Una publicación necesita comunidad y autor")
    void requiresCommunityAndAuthor() {
        assertThrows(IllegalArgumentException.class, () -> new CommunityPost(null, 10L, "Hola"));
        assertThrows(IllegalArgumentException.class, () -> new CommunityPost(1L, null, "Hola"));
    }
}
