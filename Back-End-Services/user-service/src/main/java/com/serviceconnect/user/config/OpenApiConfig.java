package com.serviceconnect.user.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userServiceOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("ServiceConnect User Service API")
                        .description(
                                "REST API for managing ServiceConnect users"
                        )
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ServiceConnect")
                                .email("support@serviceconnect.com")
                        )
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "bearerAuth",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .description(
                                                        "Enter JWT token"
                                                )
                                )
                );
    }
}