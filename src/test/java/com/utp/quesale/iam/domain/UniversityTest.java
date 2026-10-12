package com.utp.quesale.iam.domain;

import com.utp.quesale.iam.domain.aggregates.University;
import com.utp.quesale.iam.domain.valueobjects.EmailDomain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Pruebas de dominio puras: sin Spring ni base de datos
class UniversityTest {

    private University utp() {
        return new University("Universidad Tecnológica del Perú", new EmailDomain("utp.edu.pe"));
    }

    @Test
    @DisplayName("El dominio de correo se guarda en minúsculas y sin @")
    void normalizesEmailDomain() {
        assertEquals("utp.edu.pe", new EmailDomain("@UTP.edu.pe").value());
    }

    @Test
    @DisplayName("No se acepta un dominio de correo inválido")
    void rejectsInvalidEmailDomain() {
        assertThrows(IllegalArgumentException.class, () -> new EmailDomain("utp"));
    }

    @Test
    @DisplayName("Solo acepta correos de su propio dominio")
    void acceptsOnlyItsOwnDomain() {
        University utp = utp();

        assertTrue(utp.accepts("camila@utp.edu.pe"));
        assertFalse(utp.accepts("camila@gmail.com"));
        assertFalse(utp.accepts("sin-arroba"));
    }

    @Test
    @DisplayName("El nombre es obligatorio")
    void rejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new University(" ", new EmailDomain("utp.edu.pe")));
    }

    @Test
    @DisplayName("Una universidad nace activa y no se desactiva dos veces")
    void cannotDeactivateTwice() {
        University utp = utp();
        assertTrue(utp.isActive());

        utp.deactivate();

        assertFalse(utp.isActive());
        assertThrows(IllegalStateException.class, utp::deactivate);
    }
}
