package com.ifit.ronnie.configuration;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Configuración de OpenAPI/Swagger para el servicio de coaches de IA.
 * Documenta los endpoints de interacción con los coaches virtuales.
 * 
 * @author Juan García
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ronnieOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("iFit AI Coaches API")
                        .description("""
                                API REST para interactuar con los coaches de IA de iFit.
                                
                                Coaches disponibles:
                                - **Ronnie**: Especialista en musculación y fuerza
                                - **Serena**: Especialista en yoga y flexibilidad
                                - **Eliud**: Especialista en running y resistencia
                                - **Kael**: Especialista en entrenamiento funcional y HIIT
                                
                                Cada coach mantiene el contexto de la conversación mediante un `memoryId`.
                                """)
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Juan García")
                                .email("adminifit96@gmail.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8082")
                                .description("Servicio directo")
                ));
    }
}