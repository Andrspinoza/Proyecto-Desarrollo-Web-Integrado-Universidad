package com.utp.quesale.communities.domain;

import com.utp.quesale.communities.domain.aggregates.Community;
import com.utp.quesale.communities.domain.entities.CommunityPost;
import com.utp.quesale.communities.domain.entities.Membership;
import com.utp.quesale.communities.domain.exceptions.AlreadyMemberException;
import com.utp.quesale.communities.domain.exceptions.LastAdminCannotLeaveException;
import com.utp.quesale.communities.domain.exceptions.NotCommunityAdminException;
import com.utp.quesale.communities.domain.exceptions.NotMemberException;
import com.utp.quesale.communities.domain.valueobjects.MemberRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
    @DisplayName("Un ADMIN promueve a un miembro y el primer ADMIN ya puede salir")
    void adminCanPromoteMember() {
        Community community = community();
        community.join(STUDENT_ID);

        community.promote(STUDENT_ID, CREATOR_ID);

        assertTrue(community.isAdmin(STUDENT_ID));
        assertDoesNotThrow(() -> community.leave(CREATOR_ID));
        assertFalse(community.isMember(CREATOR_ID));
    }

    @Test
    @DisplayName("Un MEMBER no puede promover a nadie")
    void memberCannotPromote() {
        Community community = community();
        community.join(STUDENT_ID);
        community.join(30L);

        assertThrows(NotCommunityAdminException.class, () -> community.promote(30L, STUDENT_ID));
        assertFalse(community.isAdmin(30L));
    }

    @Test
    @DisplayName("Solo se promueve a quien ya es miembro")
    void cannotPromoteNonMember() {
        assertThrows(NotMemberException.class, () -> community().promote(STUDENT_ID, CREATOR_ID));
    }

    @Test
    @DisplayName("No se promueve a quien ya es ADMIN")
    void cannotPromoteAdminTwice() {
        assertThrows(IllegalStateException.class, () -> community().promote(CREATOR_ID, CREATOR_ID));
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

    private Community savedCommunity() {
        return new Community(1L, UNIVERSITY_ID, "Club de ajedrez", null, CREATOR_ID, Instant.now(),
                List.of(new Membership(CREATOR_ID, MemberRole.ADMIN)));
    }

    @Test
    @DisplayName("Un miembro publica en la comunidad")
    void memberCanPublish() {
        Community community = savedCommunity();
        community.join(STUDENT_ID);

        CommunityPost post = community.publish(STUDENT_ID, "Hola a todos");

        assertEquals(1L, post.getCommunityId());
        assertEquals(STUDENT_ID, post.getAuthorId());
        assertEquals("Hola a todos", post.getContent());
    }

    @Test
    @DisplayName("Quien no es miembro no puede publicar")
    void nonMemberCannotPublish() {
        assertThrows(NotMemberException.class, () -> savedCommunity().publish(STUDENT_ID, "Hola"));
    }

    @Test
    @DisplayName("No se publica en una comunidad que todavía no se guardó")
    void cannotPublishBeforeSaving() {
        assertThrows(IllegalStateException.class, () -> community().publish(CREATOR_ID, "Hola"));
    }
}
