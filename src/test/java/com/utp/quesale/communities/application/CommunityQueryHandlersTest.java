package com.utp.quesale.communities.application;

import com.utp.quesale.communities.application.dtos.CommunityDto;
import com.utp.quesale.communities.application.dtos.CommunityPostDto;
import com.utp.quesale.communities.application.dtos.MembershipDto;
import com.utp.quesale.communities.application.handlers.queries.GetCommunityQueryHandler;
import com.utp.quesale.communities.application.handlers.queries.ListCommunitiesQueryHandler;
import com.utp.quesale.communities.application.handlers.queries.ListMembersQueryHandler;
import com.utp.quesale.communities.application.handlers.queries.ListPostsQueryHandler;
import com.utp.quesale.communities.application.queries.GetCommunityQuery;
import com.utp.quesale.communities.application.queries.ListCommunitiesQuery;
import com.utp.quesale.communities.application.queries.ListMembersQuery;
import com.utp.quesale.communities.application.queries.ListPostsQuery;
import com.utp.quesale.communities.domain.aggregates.Community;
import com.utp.quesale.communities.domain.entities.CommunityPost;
import com.utp.quesale.communities.domain.exceptions.CommunityNotFoundException;
import com.utp.quesale.communities.domain.exceptions.DifferentUniversityException;
import com.utp.quesale.communities.domain.exceptions.NotMemberException;
import com.utp.quesale.communities.domain.exceptions.StudentProfileRequiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommunityQueryHandlersTest {

    private static final Long UTP = 1L;
    private static final Long UPC = 2L;
    private static final Long CREATOR = 10L;
    private static final Long CLASSMATE = 11L;
    private static final Long OTHER_UNIVERSITY_STUDENT = 20L;

    private final InMemoryCommunityRepository repository = new InMemoryCommunityRepository();
    private final InMemoryCommunityPostRepository postRepository = new InMemoryCommunityPostRepository();
    private final FakeProfilesContextFacade profiles = new FakeProfilesContextFacade();
    private final GetCommunityQueryHandler getHandler = new GetCommunityQueryHandler(repository, profiles);
    private final ListCommunitiesQueryHandler listHandler = new ListCommunitiesQueryHandler(repository, profiles);
    private final ListMembersQueryHandler membersHandler = new ListMembersQueryHandler(repository, profiles);
    private final ListPostsQueryHandler postsHandler = new ListPostsQueryHandler(repository, postRepository);

    @BeforeEach
    void registerStudents() {
        profiles.register(CREATOR, UTP);
        profiles.register(CLASSMATE, UTP);
        profiles.register(OTHER_UNIVERSITY_STUDENT, UPC);
    }

    private Community saveCommunity(Long universityId, String name, Long creatorId) {
        return repository.save(new Community(universityId, name, null, creatorId));
    }

    @Test
    @DisplayName("Devuelve la comunidad pedida a un estudiante de su universidad")
    void getsCommunity() {
        saveCommunity(UTP, "Ajedrez", CREATOR);

        CommunityDto dto = getHandler.handle(new GetCommunityQuery(1L, CLASSMATE));

        assertEquals("Ajedrez", dto.name());
        assertEquals(1, dto.memberCount());
    }

    @Test
    @DisplayName("No muestra la comunidad a otra universidad ni a quien no tiene perfil")
    void rejectsOutsiders() {
        saveCommunity(UTP, "Ajedrez", CREATOR);

        assertThrows(DifferentUniversityException.class,
                () -> getHandler.handle(new GetCommunityQuery(1L, OTHER_UNIVERSITY_STUDENT)));
        assertThrows(StudentProfileRequiredException.class,
                () -> getHandler.handle(new GetCommunityQuery(1L, 99L)));
    }

    @Test
    @DisplayName("Una comunidad inexistente da no encontrada")
    void unknownCommunity() {
        assertThrows(CommunityNotFoundException.class, () -> getHandler.handle(new GetCommunityQuery(99L, CREATOR)));
        assertThrows(CommunityNotFoundException.class,
                () -> membersHandler.handle(new ListMembersQuery(99L, CREATOR)));
        assertThrows(CommunityNotFoundException.class, () -> postsHandler.handle(new ListPostsQuery(99L, CREATOR)));
    }

    @Test
    @DisplayName("Lista solo las comunidades de la universidad del estudiante")
    void listsOnlyOwnUniversity() {
        saveCommunity(UTP, "Ajedrez", CREATOR);
        saveCommunity(UTP, "Futbol", CREATOR);
        saveCommunity(UPC, "Teatro", OTHER_UNIVERSITY_STUDENT);

        List<CommunityDto> communities = listHandler.handle(new ListCommunitiesQuery(CLASSMATE));

        assertEquals(2, communities.size());
        assertTrue(communities.stream().allMatch(c -> c.universityId().equals(UTP)));
    }

    @Test
    @DisplayName("No lista comunidades a un estudiante sin perfil")
    void listRequiresProfile() {
        assertThrows(StudentProfileRequiredException.class, () -> listHandler.handle(new ListCommunitiesQuery(99L)));
    }

    @Test
    @DisplayName("Lista los miembros con su rol")
    void listsMembers() {
        Community community = saveCommunity(UTP, "Ajedrez", CREATOR);
        community.join(CLASSMATE);
        repository.update(community);

        List<MembershipDto> members = membersHandler.handle(new ListMembersQuery(1L, CLASSMATE));

        assertEquals(2, members.size());
        assertEquals("ADMIN", members.get(0).role());
        assertEquals("MEMBER", members.get(1).role());
    }

    @Test
    @DisplayName("No lista los miembros a otra universidad")
    void membersRejectOtherUniversity() {
        saveCommunity(UTP, "Ajedrez", CREATOR);

        assertThrows(DifferentUniversityException.class,
                () -> membersHandler.handle(new ListMembersQuery(1L, OTHER_UNIVERSITY_STUDENT)));
    }

    @Test
    @DisplayName("Un miembro ve las publicaciones, las más recientes primero")
    void memberListsPosts() {
        saveCommunity(UTP, "Ajedrez", CREATOR);
        postRepository.save(new CommunityPost(null, 1L, CREATOR, "Primera",
                Instant.parse("2026-10-01T10:00:00Z")));
        postRepository.save(new CommunityPost(null, 1L, CREATOR, "Segunda",
                Instant.parse("2026-10-02T10:00:00Z")));

        List<CommunityPostDto> posts = postsHandler.handle(new ListPostsQuery(1L, CREATOR));

        assertEquals(List.of("Segunda", "Primera"), posts.stream().map(CommunityPostDto::content).toList());
    }

    @Test
    @DisplayName("Quien no es miembro no ve las publicaciones")
    void nonMemberCannotListPosts() {
        saveCommunity(UTP, "Ajedrez", CREATOR);

        assertThrows(NotMemberException.class, () -> postsHandler.handle(new ListPostsQuery(1L, CLASSMATE)));
    }
}
