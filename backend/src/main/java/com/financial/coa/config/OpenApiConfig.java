package com.financial.coa.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI configuration for Chart of Accounts Management API.
 * Provides Swagger UI documentation at /swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI coaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Chart of Accounts Management API")
                        .description("""
                                RESTful API for managing chart of accounts structures, Formance Ledger mappings,
                                and batch imports for the Financial Journal Maker platform.
                                
                                This API provides the foundational data layer for the AI-assisted accounting rule design system.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Financial Journal Maker Team"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort + "/api/v1")
                                .description("Local development server"),
                        new Server()
                                .url("https://api.financial-journal-maker.com/api/v1")
                                .description("Production server")
                ));
    }
}
