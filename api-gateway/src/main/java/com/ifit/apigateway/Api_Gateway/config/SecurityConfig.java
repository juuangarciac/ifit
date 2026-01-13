package com.ifit.apigateway.Api_Gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        http
            .authorizeExchange(exchange -> exchange
                .pathMatchers("/ifit/keycloak/auth/**").permitAll()     // Login y registro
                .pathMatchers("/ifit/keycloak/user/create").permitAll() // Crear usuario
                .pathMatchers("/ifit/api/v1/**").authenticated()        // Servicio IFIT
                .pathMatchers("/ifit/aimodels/api/v1/**").authenticated() // Servicio RONNIE
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {
                })
            )
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}

