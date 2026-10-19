package com.utp.quesale.iam.application;

import com.utp.quesale.iam.application.assemblers.IamDtoAssembler;
import com.utp.quesale.iam.application.dtos.UniversityDto;
import com.utp.quesale.iam.domain.aggregates.University;
import com.utp.quesale.iam.domain.valueobjects.EmailDomain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

// Pruebas puras: sin Spring ni base de datos
class IamDtoAssemblerTest {

    @Test
    @DisplayName("Convierte la universidad en su DTO con todos sus datos")
    void convertsUniversityToDto() {
        Instant createdAt = Instant.parse("2026-10-01T12:00:00Z");
        University university = new University(7L, "Universidad Tecnológica del Perú",
                new EmailDomain("utp.edu.pe"), true, createdAt);

        UniversityDto dto = IamDtoAssembler.toDto(university);

        assertEquals(7L, dto.id());
        assertEquals("Universidad Tecnológica del Perú", dto.name());
        assertEquals("utp.edu.pe", dto.emailDomain());
        assertEquals(createdAt, dto.createdAt());
    }

    @Test
    @DisplayName("Refleja que una universidad desactivada ya no está activa")
    void reflectsInactiveUniversity() {
        University university = new University(8L, "Universidad Peruana de Ciencias Aplicadas",
                new EmailDomain("upc.edu.pe"), true, Instant.now());
        university.deactivate();

        assertFalse(IamDtoAssembler.toDto(university).active());
    }
}
