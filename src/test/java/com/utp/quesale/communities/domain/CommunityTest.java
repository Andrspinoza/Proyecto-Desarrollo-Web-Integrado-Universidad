package com.utp.quesale.communities.domain;

import com.utp.quesale.communities.domain.aggregates.Community;
import com.utp.quesale.communities.domain.exceptions.AlreadyMemberException;
import com.utp.quesale.communities.domain.exceptions.LastAdminCannotLeaveException;
import com.utp.quesale.communities.domain.exceptions.NotCommunityAdminException;
import com.utp.quesale.communities.domain.exceptions.NotMemberException;
import com.utp.quesale.communities.domain.valueobjects.MemberRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Pruebas de dominio puras: sin Spring ni base de datos
class CommunityTest {

    private static final Long UNIVERSITY_ID = 1L;
    private static final Long CREATOR_ID = 10L;
    private static final Long STUDENT_ID = 20L;

    private Community community() {
        return new Community(UNIVERSITY_ID, "Club de ajedrez", "Partidas y torneos", CREATOR_ID);
    }

    @Test
    @DisplayName("El creador entra como ADMIN")
    void creatorJoinsAsAdmin() {
        Community community = community();

        assertTrue(community.isMember(CREATOR_ID));
        assertTrue(community.isAdmin(CREATOR_ID));
        assertEquals(1, community.getMemberships().size());
        assertEquals(MemberRole.ADMIN, community.getMemberships().get(0).getRole());
    }

    @Test
    @DisplayName("Quien se une entra como MEMBER")
    void joinAddsMember() {
        Community community = community();

        community.join(STUDENT_ID);

        assertTrue(community.isMember(STUDENT_ID));
        assertFalse(community.isAdmin(STUDENT_ID));
    }

    @Test
    @DisplayName("Nadie se une dos veces")
    void cannotJoinTwice() {
        Community community = community();
        community.join(STUDENT_ID);

        assertThrows(AlreadyMemberException.class, () -> community.join(STUDENT_ID));
        assertThrows(AlreadyMemberException.class, () -> community.join(CREATOR_ID));
    }

    @Test
    @DisplayName("Un miembro puede salir")
    void memberCanLeave() {
        Community community = community();
        community.join(STUDENT_ID);

        community.leave(STUDENT_ID);

        assertFalse(community.isMember(STUDENT_ID));
    }

    @Test
    @DisplayName("Quien no es miembro no puede salir")
    void nonMemberCannotLeave() {
        assertThrows(NotMemberException.class, () -> community().leave(STUDENT_ID));
    }

    @Test
    @DisplayName("El único ADMIN no puede abandonar la comunidad")
    void lastAdminCannotLeave() {
        Community community = community();
        community.join(STUDENT_ID);

        assertThrows(LastAdminCannotLeaveException.class, () -> community.leave(CREATOR_ID));
        assertTrue(community.isAdmin(CREATOR_ID));
    }

    @Test
    @DisplayName("Un ADMIN modifica la comunidad")
    void adminCanUpdate() {
        Community community = community();

        community.update("Club de ajedrez UTP", "Nueva descripcion", CREATOR_ID);

        assertEquals("Club de ajedrez UTP", community.getName());
        assertEquals("Nueva descripcion", community.getDescription());
    }

    @Test
    @DisplayName("Un MEMBER no puede modificar la comunidad")
    void memberCannotUpdate() {
        Community community = community();
        community.join(STUDENT_ID);

        assertThrows(NotCommunityAdminException.class,
                () -> community.update("Otro nombre", null, STUDENT_ID));
        assertEquals("Club de ajedrez", community.getName());
    }

    @Test
    @DisplayName("Quien no es miembro tampoco puede modificar la comunidad")
    void outsiderCannotUpdate() {
        assertThrows(NotCommunityAdminException.class,
                () -> community().update("Otro nombre", null, STUDENT_ID));
    }

    @Test
    @DisplayName("El nombre es obligatorio y de máximo 80 caracteres")
    void validatesName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Community(UNIVERSITY_ID, " ", null, CREATOR_ID));
        assertThrows(IllegalArgumentException.class,
                () -> new Community(UNIVERSITY_ID, "a".repeat(81), null, CREATOR_ID));
        assertDoesNotThrow(() -> new Community(UNIVERSITY_ID, "a".repeat(80), null, CREATOR_ID));
    }

    @Test
    @DisplayName("La descripción admite máximo 500 caracteres")
    void validatesDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> new Community(UNIVERSITY_ID, "Club", "a".repeat(501), CREATOR_ID));
        assertDoesNotThrow(() -> new Community(UNIVERSITY_ID, "Club", "a".repeat(500), CREATOR_ID));
    }

    @Test
    @DisplayName("La lista de miembros no se puede modificar desde afuera")
    void membershipsAreUnmodifiable() {
        Community community = community();

        assertThrows(UnsupportedOperationException.class, () -> community.getMemberships().clear());
    }
}
