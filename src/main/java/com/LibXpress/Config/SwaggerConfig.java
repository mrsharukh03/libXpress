package com.LibXpress.Config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Library Xpress API",
                version = "1.0",
                description = "API documentation for Library Xpress",
                contact = @Contact(name = "Library Support", email = "devloperindia03@gmail.com")
        )
)
@Configuration
public class SwaggerConfig {
}
