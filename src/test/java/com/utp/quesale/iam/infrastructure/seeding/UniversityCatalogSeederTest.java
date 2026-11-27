package com.utp.quesale.iam.infrastructure.seeding;

import com.utp.quesale.iam.application.commands.CreateUniversityCommand;
import com.utp.quesale.iam.application.handlers.commands.UniversityCommandHandler;
import com.utp.quesale.iam.domain.repositories.UniversityRepository;
import com.utp.quesale.iam.domain.valueobjects.EmailDomain;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UniversityCatalogSeederTest {
    private final UniversityRepository repository = mock(UniversityRepository.class);
    private final UniversityCommandHandler handler = mock(UniversityCommandHandler.class);
    private final UniversityCatalogSeeder seeder = new UniversityCatalogSeeder(repository, handler);

    @Test void catalogHasTenUniversitiesWithValidUniqueDomainsAndNames() {
        var catalog = UniversityCatalogSeeder.CATALOG;
        assertEquals(10, catalog.size());
        assertEquals(10, new HashSet<>(catalog.stream().map(CreateUniversityCommand::emailDomain).toList()).size());
        assertEquals(10, new HashSet<>(catalog.stream().map(CreateUniversityCommand::name).toList()).size());
        catalog.forEach(command -> assertDoesNotThrow(() -> new EmailDomain(command.emailDomain())));
    }

    @Test void createsEveryUniversityWhenTheCatalogIsEmpty() {
        when(repository.existsByEmailDomain(anyString())).thenReturn(false);
        seeder.run(null);
        verify(handler, times(10)).handle(any(CreateUniversityCommand.class));
    }

    @Test void skipsUniversitiesThatAlreadyExistByDomain() {
        when(repository.existsByEmailDomain(anyString())).thenReturn(false);
        when(repository.existsByEmailDomain("utp.edu.pe")).thenReturn(true);
        seeder.run(null);
        ArgumentCaptor<CreateUniversityCommand> captured = ArgumentCaptor.forClass(CreateUniversityCommand.class);
        verify(handler, times(9)).handle(captured.capture());
        List<String> domains = captured.getAllValues().stream().map(CreateUniversityCommand::emailDomain).toList();
        assertFalse(domains.contains("utp.edu.pe"));
        assertTrue(domains.contains("usil.pe"));
    }

    @Test void runningTwiceDoesNotDuplicate() {
        when(repository.existsByEmailDomain(anyString())).thenReturn(true);
        seeder.run(null);
        verifyNoInteractions(handler);
    }
}
