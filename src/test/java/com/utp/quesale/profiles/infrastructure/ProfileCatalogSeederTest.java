package com.utp.quesale.profiles.infrastructure;

import com.utp.quesale.profiles.application.commands.CreateInterestCommand;
import com.utp.quesale.profiles.application.commands.CreateSkillCommand;
import com.utp.quesale.profiles.application.handlers.commands.InterestCommandHandler;
import com.utp.quesale.profiles.application.handlers.commands.SkillCommandHandler;
import com.utp.quesale.profiles.domain.repositories.InterestRepository;
import com.utp.quesale.profiles.domain.repositories.SkillRepository;
import com.utp.quesale.profiles.infrastructure.seeding.ProfileCatalogSeeder;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ProfileCatalogSeederTest {
    private final InterestRepository interests = mock(InterestRepository.class);
    private final SkillRepository skills = mock(SkillRepository.class);
    private final InterestCommandHandler interestHandler = mock(InterestCommandHandler.class);
    private final SkillCommandHandler skillHandler = mock(SkillCommandHandler.class);
    private final ProfileCatalogSeeder seeder = new ProfileCatalogSeeder(interests, skills, interestHandler, skillHandler);

    @Test void createsTheWholeCatalogWhenItIsEmpty() {
        seeder.run(null);
        verify(interestHandler, times(25)).handle(any(CreateInterestCommand.class));
        verify(skillHandler, times(10)).handle(any(CreateSkillCommand.class));
    }

    @Test void skipsNamesThatAlreadyExistSoRunningTwiceNeverDuplicates() {
        when(interests.existsByName(anyString())).thenReturn(true);
        when(skills.existsByName(anyString())).thenReturn(true);
        seeder.run(null);
        verifyNoInteractions(interestHandler, skillHandler);
    }

    @Test void createsOnlyTheMissingOnes() {
        when(interests.existsByName(anyString())).thenReturn(true);
        when(interests.existsByName("Café")).thenReturn(false);
        when(skills.existsByName(anyString())).thenReturn(true);
        seeder.run(null);
        verify(interestHandler, times(1)).handle(new CreateInterestCommand("Café"));
        verifyNoInteractions(skillHandler);
    }
}
