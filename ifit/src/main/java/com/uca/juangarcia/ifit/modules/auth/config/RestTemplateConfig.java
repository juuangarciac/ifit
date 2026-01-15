package com.uca.juangarcia.ifit.modules.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración de beans para comunicación HTTP.
 * 
 * <p>Proporciona un bean de RestTemplate para realizar peticiones HTTP
 * a servicios externos como Keycloak.
 * 
 * @author Juan Garcia
 * @version 1.0
 */
@Configuration
public class RestTemplateConfig {
    
    /**
     * Crea y configura un bean de RestTemplate.
     * 
     * <p>RestTemplate se utiliza para:
     * <ul>
     *   <li>Obtener tokens de Keycloak</li>
     *   <li>Comunicación con servicios externos</li>
     * </ul>
     * 
     * @return instancia configurada de RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
