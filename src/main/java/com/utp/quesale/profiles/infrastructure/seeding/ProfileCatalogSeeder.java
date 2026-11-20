package com.utp.quesale.profiles.infrastructure.seeding;

import com.utp.quesale.profiles.application.commands.CreateInterestCommand;
import com.utp.quesale.profiles.application.commands.CreateSkillCommand;
import com.utp.quesale.profiles.application.handlers.commands.InterestCommandHandler;
import com.utp.quesale.profiles.application.handlers.commands.SkillCommandHandler;
import com.utp.quesale.profiles.domain.repositories.InterestRepository;
import com.utp.quesale.profiles.domain.repositories.SkillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "app.universities.seed", havingValue = "true")
public class ProfileCatalogSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(ProfileCatalogSeeder.class);

    static final List<String> INTERESTS = List.of(
            "Café", "Vóley", "Fútbol", "Básquet", "Videojuegos", "Música indie", "Rock", "Reggaetón",
            "Cine", "Series", "Anime", "Lectura", "Fotografía", "Viajes", "Gimnasio", "Running",
            "Cocina", "Arte", "Tecnología", "Emprendimiento", "Idiomas", "Estudio en grupo", "Baile",
            "Mascotas", "Voluntariado");

    static final List<String> SKILLS = List.of(
            "Programación", "Diseño gráfico", "Matemática", "Redacción", "Oratoria", "Excel",
            "Liderazgo", "Edición de video", "Análisis de datos", "Trabajo en equipo");

    private final InterestRepository interests;
    private final SkillRepository skills;
    private final InterestCommandHandler interestHandler;
    private final SkillCommandHandler skillHandler;

    public ProfileCatalogSeeder(InterestRepository interests, SkillRepository skills,
                                InterestCommandHandler interestHandler, SkillCommandHandler skillHandler) {
        this.interests = interests;
        this.skills = skills;
        this.interestHandler = interestHandler;
        this.skillHandler = skillHandler;
    }

    @Override
    public void run(ApplicationArguments args) {
        int createdInterests = 0;
        for (String name : INTERESTS) {
            if (interests.existsByName(name)) continue;
            interestHandler.handle(new CreateInterestCommand(name));
            createdInterests++;
        }
        int createdSkills = 0;
        for (String name : SKILLS) {
            if (skills.existsByName(name)) continue;
            skillHandler.handle(new CreateSkillCommand(name));
            createdSkills++;
        }
        log.info("Catálogos del perfil: {} intereses y {} habilidades nuevos", createdInterests, createdSkills);
    }
}
