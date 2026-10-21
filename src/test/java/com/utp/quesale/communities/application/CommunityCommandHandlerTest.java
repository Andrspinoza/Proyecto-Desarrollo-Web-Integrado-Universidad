package com.utp.quesale.communities.application;

import com.utp.quesale.communities.application.commands.CreateCommunityCommand;
import com.utp.quesale.communities.application.commands.JoinCommunityCommand;
import com.utp.quesale.communities.application.commands.UpdateCommunityCommand;
import com.utp.quesale.communities.application.dtos.CommunityDto;
import com.utp.quesale.communities.application.handlers.commands.CommunityCommandHandler;
import com.utp.quesale.communities.domain.exceptions.AlreadyMemberException;
import com.utp.quesale.communities.domain.exceptions.CommunityNameAlreadyExistsException;
import com.utp.quesale.communities.domain.exceptions.CommunityNotFoundException;
import com.utp.quesale.communities.domain.exceptions.DifferentUniversityException;
import com.utp.quesale.communities.domain.exceptions.NotCommunityAdminException;
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
    private final FakeProfilesContextFacade profiles = new FakeProfilesContextFacade();
    private final List<AbstractDomainAggregateRoot<?>> dispatched = new ArrayList<>();
    private final DomainEventDispatcher events = dispatched::add;
    private final CommunityCommandHandler handler = new CommunityCommandHandler(repository, profiles, events);

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
}
