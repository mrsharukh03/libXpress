package com.LibXpress.Config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Library Xpress API",
                version = "3.1.0",
                description = "API documentation for Library Xpress",
                contact = @Contact(name = "Library Support", email = "devloperindia03@gmail.com")
        )
)
public class SwaggerConfig {

        @Bean
        public OpenAPI myCustomApi() {
                return new OpenAPI()
                        .components(new Components().addSecuritySchemes(
                                "bearerAuth", new io.swagger.v3.oas.models.security.SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")
                        ))
                        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
        }
}
