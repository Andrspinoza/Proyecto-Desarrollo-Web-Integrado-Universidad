package com.utp.quesale.shared.infrastructure.documentation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI queSaleOpenApi() {
        return new OpenAPI().info(new Info()
                .title("¿Qué sale? API")
                .version("v1")
                .description("Conexión y organización de meetups académicos y sociales para estudiantes "
                        + "universitarios. Identidad provisional: header X-Student-Id."));
    }
}
