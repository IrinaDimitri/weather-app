package com.example.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration 
@OpenAPIDefinition(  
    info = @Info(
        title = "Weather API",
        version = "3.0.2",
        description = "API для управления данными о погоде",
        contact = @Contact(
            name = "Irina Dimitrieva",
            email = "irina@example.com"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(
            url = "http://localhost:8080",
            description = "Локальный сервер"
        )
    }
)
public class SwaggerConfig { 
}
