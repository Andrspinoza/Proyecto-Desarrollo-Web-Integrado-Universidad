package com.utp.quesale.communities.application;

import com.utp.quesale.communities.application.commands.CreateCommunityCommand;
import com.utp.quesale.communities.application.commands.JoinCommunityCommand;
import com.utp.quesale.communities.application.commands.LeaveCommunityCommand;
import com.utp.quesale.communities.application.commands.PromoteMemberCommand;
import com.utp.quesale.communities.application.commands.PublishPostCommand;
import com.utp.quesale.communities.application.commands.UpdateCommunityCommand;
import com.utp.quesale.communities.application.dtos.CommunityDto;
import com.utp.quesale.communities.application.dtos.CommunityPostDto;
import com.utp.quesale.communities.application.handlers.commands.CommunityCommandHandler;
import com.utp.quesale.communities.domain.exceptions.AlreadyMemberException;
import com.utp.quesale.communities.domain.exceptions.CommunityNameAlreadyExistsException;
import com.utp.quesale.communities.domain.exceptions.CommunityNotFoundException;
import com.utp.quesale.communities.domain.exceptions.DifferentUniversityException;
import com.utp.quesale.communities.domain.exceptions.LastAdminCannotLeaveException;
import com.utp.quesale.communities.domain.exceptions.NotCommunityAdminException;
import com.utp.quesale.communities.domain.exceptions.NotMemberException;
import com.utp.quesale.communities.domain.exceptions.StudentProfileRequiredException;
import com.utp.quesale.shared.domain.events.DomainEventDispatcher;
import com.utp.quesale.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Pruebas del handler: sin Spring ni base de datos
class CommunityCommandHandlerTest {

    private static final Long UTP = 1L;
    private static final Long UPC = 2L;
    private static final Long CREATOR = 10L;
    private static final Long MEMBER = 11L;
    private static final Long OTHER_UNIVERSITY_STUDENT = 20L;

    private final InMemoryCommunityRepository repository = new InMemoryCommunityRepository();
    private final InMemoryCommunityPostRepository postRepository = new InMemoryCommunityPostRepository();
    private final FakeProfilesContextFacade profiles = new FakeProfilesContextFacade();
    private final List<AbstractDomainAggregateRoot<?>> dispatched = new ArrayList<>();
    private final DomainEventDispatcher events = dispatched::add;
    private final CommunityCommandHandler handler =
            new CommunityCommandHandler(repository, postRepository, profiles, events);

    @BeforeEach
    void registerStudents() {
        profiles.register(CREATOR, UTP);
        profiles.register(MEMBER, UTP);
        profiles.register(OTHER_UNIVERSITY_STUDENT, UPC);
    }

    private CommunityDto createChess() {
        return handler.handle(new CreateCommunityCommand(CREATOR, "Ajedrez", "Torneos semanales"));
    }

    @Test
    @DisplayName("Crea la comunidad en la universidad del creador y lo deja como único miembro")
    void createsCommunity() {
        CommunityDto dto = createChess();

        assertEquals(1L, dto.id());
        assertEquals(UTP, dto.universityId());
        assertEquals(CREATOR, dto.creatorId());
        assertEquals(1, dto.memberCount());
        assertTrue(repository.findById(1L).orElseThrow().isAdmin(CREATOR));
    }

    @Test
    @DisplayName("No crea una comunidad si el estudiante no tiene perfil")
    void rejectsCreatorWithoutProfile() {
        assertThrows(StudentProfileRequiredException.class,
                () -> handler.handle(new CreateCommunityCommand(99L, "Ajedrez", null)));
        assertFalse(repository.existsById(1L));
    }

    @Test
    @DisplayName("No repite el nombre dentro de la misma universidad, pero sí en otra")
    void rejectsDuplicatedNameOnlyInSameUniversity() {
        createChess();

        assertThrows(CommunityNameAlreadyExistsException.class,
                () -> handler.handle(new CreateCommunityCommand(MEMBER, "ajedrez", null)));
        assertDoesNotThrow(() -> handler.handle(
                new CreateCommunityCommand(OTHER_UNIVERSITY_STUDENT, "Ajedrez", null)));
    }

    @Test
    @DisplayName("Despacha el agregado guardado para publicar sus eventos")
    void dispatchesSavedAggregate() {
        createChess();

        assertEquals(1, dispatched.size());
    }

    @Test
    @DisplayName("El administrador edita nombre y descripción")
    void adminUpdatesCommunity() {
        createChess();

        CommunityDto dto = handler.handle(new UpdateCommunityCommand(1L, CREATOR, "Ajedrez UTP", "Nueva"));

        assertEquals("Ajedrez UTP", dto.name());
        assertEquals("Nueva", dto.description());
    }

    @Test
    @DisplayName("Quien no es administrador no edita la comunidad")
    void nonAdminCannotUpdate() {
        createChess();
        handler.handle(new JoinCommunityCommand(1L, MEMBER));

        assertThrows(NotCommunityAdminException.class,
                () -> handler.handle(new UpdateCommunityCommand(1L, MEMBER, "Otro", null)));
    }

    @Test
    @DisplayName("No renombra a un nombre que ya usa otra comunidad de la universidad")
    void rejectsRenameToExistingName() {
        createChess();
        handler.handle(new CreateCommunityCommand(CREATOR, "Futbol", null));

        assertThrows(CommunityNameAlreadyExistsException.class,
                () -> handler.handle(new UpdateCommunityCommand(2L, CREATOR, "AJEDREZ", null)));
    }

    @Test
    @DisplayName("Puede conservar su nombre actual al editar solo la descripción")
    void keepsOwnNameWhenUpdatingDescription() {
        createChess();

        assertDoesNotThrow(() -> handler.handle(new UpdateCommunityCommand(1L, CREATOR, "Ajedrez", "Otra")));
    }

    @Test
    @DisplayName("Editar una comunidad inexistente da no encontrada")
    void updateUnknownCommunity() {
        assertThrows(CommunityNotFoundException.class,
                () -> handler.handle(new UpdateCommunityCommand(99L, CREATOR, "X", null)));
    }

    @Test
    @DisplayName("Un estudiante de la misma universidad se une como miembro")
    void joinsCommunity() {
        createChess();

        CommunityDto dto = handler.handle(new JoinCommunityCommand(1L, MEMBER));

        assertEquals(2, dto.memberCount());
        assertTrue(repository.findById(1L).orElseThrow().isMember(MEMBER));
    }

    @Test
    @DisplayName("No se une quien estudia en otra universidad")
    void rejectsJoinFromOtherUniversity() {
        createChess();

        assertThrows(DifferentUniversityException.class,
                () -> handler.handle(new JoinCommunityCommand(1L, OTHER_UNIVERSITY_STUDENT)));
    }

    @Test
    @DisplayName("No se une dos veces ni sin perfil ni a una comunidad inexistente")
    void rejectsInvalidJoins() {
        createChess();

        assertThrows(AlreadyMemberException.class, () -> handler.handle(new JoinCommunityCommand(1L, CREATOR)));
        assertThrows(StudentProfileRequiredException.class, () -> handler.handle(new JoinCommunityCommand(1L, 99L)));
        assertThrows(CommunityNotFoundException.class, () -> handler.handle(new JoinCommunityCommand(99L, MEMBER)));
    }

    @Test
    @DisplayName("Un miembro puede salir de la comunidad")
    void memberLeaves() {
        createChess();
        handler.handle(new JoinCommunityCommand(1L, MEMBER));

        handler.handle(new LeaveCommunityCommand(1L, MEMBER));

        assertFalse(repository.findById(1L).orElseThrow().isMember(MEMBER));
    }

    @Test
    @DisplayName("El único administrador no puede salir")
    void lastAdminCannotLeave() {
        createChess();
        handler.handle(new JoinCommunityCommand(1L, MEMBER));

        assertThrows(LastAdminCannotLeaveException.class,
                () -> handler.handle(new LeaveCommunityCommand(1L, CREATOR)));
        assertTrue(repository.findById(1L).orElseThrow().isMember(CREATOR));
    }

    @Test
    @DisplayName("Quien no es miembro no puede salir")
    void nonMemberCannotLeave() {
        createChess();

        assertThrows(NotMemberException.class, () -> handler.handle(new LeaveCommunityCommand(1L, MEMBER)));
    }

    @Test
    @DisplayName("El administrador promueve a un miembro y entonces el creador ya puede salir")
    void promotesMemberSoAdminCanLeave() {
        createChess();
        handler.handle(new JoinCommunityCommand(1L, MEMBER));

        handler.handle(new PromoteMemberCommand(1L, MEMBER, CREATOR));

        assertTrue(repository.findById(1L).orElseThrow().isAdmin(MEMBER));
        assertDoesNotThrow(() -> handler.handle(new LeaveCommunityCommand(1L, CREATOR)));
    }

    @Test
    @DisplayName("Quien no es administrador no promueve a nadie")
    void nonAdminCannotPromote() {
        createChess();
        handler.handle(new JoinCommunityCommand(1L, MEMBER));

        assertThrows(NotCommunityAdminException.class,
                () -> handler.handle(new PromoteMemberCommand(1L, MEMBER, MEMBER)));
    }

    @Test
    @DisplayName("Un miembro publica y la publicación queda guardada con su id")
    void memberPublishes() {
        createChess();

        CommunityPostDto dto = handler.handle(new PublishPostCommand(1L, CREATOR, "Hola a todos"));

        assertEquals(1L, dto.id());
        assertEquals(1L, dto.communityId());
        assertEquals(CREATOR, dto.authorId());
        assertEquals(1, postRepository.findAllByCommunityId(1L).size());
    }

    @Test
    @DisplayName("Quien no es miembro no publica y no se guarda nada")
    void nonMemberCannotPublish() {
        createChess();

        assertThrows(NotMemberException.class,
                () -> handler.handle(new PublishPostCommand(1L, MEMBER, "Hola")));
        assertTrue(postRepository.findAllByCommunityId(1L).isEmpty());
    }

    @Test
    @DisplayName("No publica contenido vacío ni en una comunidad inexistente")
    void rejectsInvalidPosts() {
        createChess();

        assertThrows(IllegalArgumentException.class,
                () -> handler.handle(new PublishPostCommand(1L, CREATOR, " ")));
        assertThrows(CommunityNotFoundException.class,
                () -> handler.handle(new PublishPostCommand(99L, CREATOR, "Hola")));
    }
}
