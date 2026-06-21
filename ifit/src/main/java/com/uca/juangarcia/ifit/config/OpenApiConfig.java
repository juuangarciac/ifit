package com.uca.juangarcia.ifit.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI/Swagger para el servicio ifit.
 * Soporta acceso directo al servicio y a través del API Gateway.
 * 
 * @author Juan García
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ifitOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("iFit API")
                        .description("API REST para el sistema de gestión de fitness iFit")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Juan García")
                                .email("adminifit96@gmail.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Servicio directo")
                ));
    }
}