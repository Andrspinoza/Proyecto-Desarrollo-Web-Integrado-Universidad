package com.utp.quesale.shared.infrastructure;

import com.utp.quesale.shared.infrastructure.web.WebCorsConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WebCorsConfigurationTest {

    @RestController
    static class PingController {
        @GetMapping("/api/v1/ping")
        ResponseEntity<String> ping() {
            return ResponseEntity.ok("pong");
        }
    }

    static class ReadableCorsRegistry extends CorsRegistry {
        Map<String, CorsConfiguration> configurations() {
            return getCorsConfigurations();
        }
    }

    private MockMvc mockMvc(String... origins) {
        ReadableCorsRegistry registry = new ReadableCorsRegistry();
        new WebCorsConfiguration(origins).addCorsMappings(registry);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.setCorsConfigurations(registry.configurations());
        return MockMvcBuilders.standaloneSetup(new PingController())
                .addFilters(new CorsFilter(source))
                .build();
    }

    @Test
    @DisplayName("El origen del front recibe permiso en la consulta previa del navegador")
    void allowedOriginPassesPreflight() throws Exception {
        mockMvc("http://localhost:4200").perform(options("/api/v1/ping")
                        .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.POST.name())
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "X-Student-Id"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"));
    }

    @Test
    @DisplayName("Un origen ajeno es rechazado")
    void foreignOriginIsRejected() throws Exception {
        mockMvc("http://localhost:4200").perform(get("/api/v1/ping")
                        .header(HttpHeaders.ORIGIN, "https://otro.example"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admite varios orígenes, como local y producción")
    void severalOrigins() throws Exception {
        mockMvc("http://localhost:4200", "https://quesale.example").perform(get("/api/v1/ping")
                        .header(HttpHeaders.ORIGIN, "https://quesale.example"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://quesale.example"));
    }
}
