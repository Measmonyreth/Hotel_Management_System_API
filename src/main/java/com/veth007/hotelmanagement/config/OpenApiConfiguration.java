package com.veth007.hotelmanagement.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components().addSecuritySchemes(
                "Bearer Authentication",
                new SecurityScheme()
                .name("Authorization")
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
            ));
    }

    @Bean
    public GroupedOpenApi backendGroup() {
        return GroupedOpenApi.builder()
                .group("backend-api")
                .addOpenApiCustomizer(openApi -> openApi.info(getBackendApiInfo()))
                .packagesToScan("com.veth007.hotelmanagement.controller")
                .build();
    }

    @Bean
    public GroupedOpenApi frontendGroup() {
        return GroupedOpenApi.builder()
                .group("frontend-api")
                .addOpenApiCustomizer(openApi -> openApi.info(getFrontendApiInfo()))
                .packagesToScan("com.veth007.hotelmanagement.controller")
                .build();
    }

    private Info getBackendApiInfo() {
        Contact contact = new Contact();
        contact.setName("VETH 007");
        contact.setEmail("example34@gamil.com");
        contact.setUrl("https://gitlab.com/rreth538");

        return new Info()
                .title("CRUD API Documentation")
                .description("API documentation for CRUD operations")
                .version("1.0.0")
                .contact(contact);
    }

    private Info getFrontendApiInfo() {
        Contact contact = new Contact();
        contact.setName("VETH 007");
        contact.setEmail("example34@gamil.com");
        contact.setUrl("https://gitlab.com/rreth538");

        return new Info()
                .title("Frontend API Documentation")
                .description("API documentation for frontend operations")
                .version("1.0.0")
                .contact(contact);
    }
}
