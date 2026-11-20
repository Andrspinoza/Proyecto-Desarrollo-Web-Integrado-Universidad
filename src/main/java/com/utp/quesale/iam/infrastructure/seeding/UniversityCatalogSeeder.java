package com.utp.quesale.iam.infrastructure.seeding;

import com.utp.quesale.iam.application.commands.CreateUniversityCommand;
import com.utp.quesale.iam.application.handlers.commands.UniversityCommandHandler;
import com.utp.quesale.iam.domain.repositories.UniversityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "app.universities.seed", havingValue = "true")
public class UniversityCatalogSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(UniversityCatalogSeeder.class);

    static final List<CreateUniversityCommand> CATALOG = List.of(
            new CreateUniversityCommand("Universidad Tecnológica del Perú", "utp.edu.pe"),
            new CreateUniversityCommand("Universidad Peruana de Ciencias Aplicadas", "upc.edu.pe"),
            new CreateUniversityCommand("Universidad de Ingeniería y Tecnología", "utec.edu.pe"),
            new CreateUniversityCommand("Universidad de Lima", "aloe.ulima.edu.pe"),
            new CreateUniversityCommand("Universidad San Ignacio de Loyola", "usil.pe"),
            new CreateUniversityCommand("Universidad Privada del Norte", "upn.edu.pe"),
            new CreateUniversityCommand("Pontificia Universidad Católica del Perú", "pucp.edu.pe")
    );

    private final UniversityRepository repository;
    private final UniversityCommandHandler handler;

    public UniversityCatalogSeeder(UniversityRepository repository, UniversityCommandHandler handler) {
        this.repository = repository;
        this.handler = handler;
    }

    @Override
    public void run(ApplicationArguments args) {
        int created = 0;
        for (CreateUniversityCommand command : CATALOG) {
            if (repository.existsByEmailDomain(command.emailDomain())) continue;
            handler.handle(command);
            created++;
        }
        log.info("Catálogo de universidades: {} nuevas de {}", created, CATALOG.size());
    }
}
