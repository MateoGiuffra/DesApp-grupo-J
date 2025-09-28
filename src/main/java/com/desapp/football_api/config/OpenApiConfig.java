package com.desapp.football_api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Football API — Grupo J",
                version = "1.0.0",
                description = "API for user authentication and football data queries (teams and players).\n" +
                        "Includes public authentication endpoints and protected endpoints using JWT in cookie.",
                contact = @Contact(name = "Grupo J", email = "contacto@grupo-j.local"),
                license = @License(name = "MIT")
        ),
        servers = {
                @Server(url = "/", description = "Servidor local")
        }
)
public class OpenApiConfig {
}
