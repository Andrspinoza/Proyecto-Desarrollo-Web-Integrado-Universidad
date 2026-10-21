package com.utp.quesale;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

// Arranca la aplicación completa con H2. Falla si falta un bean (por ejemplo, una fachada sin implementación)
// o si dos beans compiten. Los tests con repositorios falsos no detectan estos errores.
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:startup;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class ApplicationStartupTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void applicationContextStartsWithAllBeansWired() {
        assertNotNull(context);
    }
}
