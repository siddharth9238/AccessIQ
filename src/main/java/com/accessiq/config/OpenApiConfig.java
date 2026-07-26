package com.accessiq.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

/**
 * OpenAPI configuration for Swagger documentation.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates the OpenAPI configuration.
     *
     * @return the OpenAPI configuration
     */
    @Bean
    public io.swagger.v3.oas.models.OpenAPI customOpenAPI() {
        return new io.swagger.v3.oas.models.OpenAPI()
                .info(new Info()
                        .title("AccessIQ API")
                        .description(
                                "Enterprise-grade Workflow &amp; Approval System API. "
                                        + "AccessIQ provides secure authentication, "
                                        + "role-based access control, configurable "
                                        + "multi-step workflows, and comprehensive "
                                        + "audit logging.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("AccessIQ Team")
                                .email("support@accessiq.com")
                                .url("https://accessiq.com"))
                        .license(new License().name("Apache 2.0")
                                .url("http://springdoc.org")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token to authenticate")))
                .security(new ArrayList<>())
                .addTagsItem(new Tag()
                        .name("Authentication")
                        .description("Authentication endpoints"))
                .addTagsItem(new Tag()
                        .name("Requests")
                        .description("Request management endpoints"))
                .addTagsItem(new Tag()
                        .name("Workflows")
                        .description("Workflow management endpoints"))
                .addTagsItem(new Tag()
                        .name("Administration")
                        .description("Administration endpoints"))
                .addTagsItem(new Tag()
                        .name("Audit")
                        .description("Audit logging endpoints"));
    }
}