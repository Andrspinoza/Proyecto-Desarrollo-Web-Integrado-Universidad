package com.utp.quesale.communities.application;

import com.utp.quesale.communities.application.assemblers.CommunitiesDtoAssembler;
import com.utp.quesale.communities.application.dtos.CommunityDto;
import com.utp.quesale.communities.application.dtos.CommunityPostDto;
import com.utp.quesale.communities.application.dtos.MembershipDto;
import com.utp.quesale.communities.domain.aggregates.Community;
import com.utp.quesale.communities.domain.entities.CommunityPost;
import com.utp.quesale.communities.domain.entities.Membership;
import com.utp.quesale.communities.domain.valueobjects.MemberRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Pruebas puras: sin Spring ni base de datos
class CommunitiesDtoAssemblerTest {

    private static final Instant NOW = Instant.parse("2026-10-01T12:00:00Z");

    @Test
    @DisplayName("Convierte la comunidad en su DTO con el número de miembros")
    void convertsCommunityToDto() {
        Community community = new Community(5L, 1L, "Ajedrez", "Torneos semanales", 10L, NOW,
                List.of(new Membership(10L, MemberRole.ADMIN), new Membership(11L, MemberRole.MEMBER)));

        CommunityDto dto = CommunitiesDtoAssembler.toDto(community);

        assertEquals(5L, dto.id());
        assertEquals(1L, dto.universityId());
        assertEquals("Ajedrez", dto.name());
        assertEquals("Torneos semanales", dto.description());
        assertEquals(10L, dto.creatorId());
        assertEquals(2, dto.memberCount());
        assertEquals(NOW, dto.createdAt());
    }

    @Test
    @DisplayName("Convierte la membresía en su DTO con el rol como texto")
    void convertsMembershipToDto() {
        Membership membership = new Membership(1L, 10L, MemberRole.ADMIN, NOW);

        MembershipDto dto = CommunitiesDtoAssembler.toDto(membership);

        assertEquals(10L, dto.studentId());
        assertEquals("ADMIN", dto.role());
        assertEquals(NOW, dto.joinedAt());
    }

    @Test
    @DisplayName("Convierte la publicación en su DTO")
    void convertsPostToDto() {
        CommunityPost post = new CommunityPost(3L, 5L, 10L, "Hola a todos", NOW);

        CommunityPostDto dto = CommunitiesDtoAssembler.toDto(post);

        assertEquals(3L, dto.id());
        assertEquals(5L, dto.communityId());
        assertEquals(10L, dto.authorId());
        assertEquals("Hola a todos", dto.content());
        assertEquals(NOW, dto.createdAt());
    }
}
