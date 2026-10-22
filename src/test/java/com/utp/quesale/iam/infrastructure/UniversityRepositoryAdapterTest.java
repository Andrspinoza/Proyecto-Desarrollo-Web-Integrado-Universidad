package com.utp.quesale.iam.infrastructure;

import com.utp.quesale.iam.domain.aggregates.University;
import com.utp.quesale.iam.domain.valueobjects.EmailDomain;
import com.utp.quesale.iam.infrastructure.persistence.jpa.adapters.UniversityRepositoryAdapter;
import com.utp.quesale.iam.infrastructure.persistence.jpa.mappers.UniversityPersistenceMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({UniversityRepositoryAdapter.class, UniversityPersistenceMapper.class})
class UniversityRepositoryAdapterTest {

    @Autowired
    private UniversityRepositoryAdapter universityRepository;

    @Test
    @DisplayName("Se guarda y se recupera con su dominio de correo")
    void savesAndReadsUniversity() {
        University saved = universityRepository.save(
                new University("Universidad Tecnológica del Perú", new EmailDomain("utp.edu.pe")));

        University found = universityRepository.findById(saved.getId()).orElseThrow();
        assertEquals("Universidad Tecnológica del Perú", found.getName());
        assertEquals("utp.edu.pe", found.getEmailDomain().value());
        assertTrue(found.isActive());
    }

    @Test
    @DisplayName("Encuentra el dominio de correo sin importar mayúsculas")
    void existsByEmailDomainIgnoresCase() {
        universityRepository.save(new University("Universidad Tecnológica del Perú", new EmailDomain("utp.edu.pe")));

        assertTrue(universityRepository.existsByEmailDomain("UTP.EDU.PE"));
        assertFalse(universityRepository.existsByEmailDomain("upc.edu.pe"));
    }

    @Test
    @DisplayName("Lista todas las universidades guardadas")
    void findsAll() {
        universityRepository.save(new University("Universidad Tecnológica del Perú", new EmailDomain("utp.edu.pe")));
        universityRepository.save(new University("Universidad Peruana de Ciencias Aplicadas", new EmailDomain("upc.edu.pe")));

        assertEquals(2, universityRepository.findAll().size());
    }
}
