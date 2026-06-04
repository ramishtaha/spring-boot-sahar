package com.ramishtaha.sahar.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Describes the API for OpenAPI / Swagger UI (step 25).
 *
 * <p>springdoc already generates the endpoint list and JSON schemas from the controllers and records - this
 * bean just adds the human metadata (title, version, licence) and declares the {@code basic} security scheme
 * so Swagger UI shows an <b>Authorize</b> button. After authorizing once, the "Try it out" calls send the
 * {@code Authorization: Basic} header, which is exactly what the write endpoints need.
 *
 * <p>Note the lock icon will appear on every operation because we declare the requirement globally; in
 * reality the {@code GET}s are public (see {@link SecurityConfig}) and simply ignore the header. Annotating
 * each write with {@code @SecurityRequirement} instead would be more precise, at the cost of more clutter.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI saharOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sahar API")
                        .version("v1")
                        .description("""
                                The REST API behind Sahar - a personal prayer / training / nutrition routine app.
                                Reads (GET) are public; writes (POST/PUT/DELETE) need the admin credentials.""")
                        .license(new License().name("MIT")))
                .components(new Components().addSecuritySchemes("basic",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")))
                .addSecurityItem(new SecurityRequirement().addList("basic"));
    }
}
