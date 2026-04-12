package com.uca.juangarcia.ifit.modules.auth.controllers.dto;

import com.uca.juangarcia.ifit.modules.user.dto.AppUserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para operaciones de login y registro.
 * 
 * Contiene tanto los tokens de autenticación de Keycloak como
 * el perfil completo del usuario de la aplicación.
 * 
 * Estructura de respuesta:
 * - accessToken: JWT para autenticar requests
 * - refreshToken: Token para renovar el accessToken
 * - expiresIn: Tiempo de expiración del accessToken en segundos
 * - tokenType: Tipo de token (normalmente "Bearer")
 * - appUser: Perfil completo del usuario
 * 
 * @author Juan Garcia
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    
    /**
     * Token JWT de acceso para autenticar requests a la API.
     */
    private String accessToken;
    
    /**
     * Token para renovar el accessToken cuando expire.
     */
    private String refreshToken;
    
    /**
     * Tiempo de expiración del accessToken en segundos.
     */
    private Integer expiresIn;
    
    /**
     * Tipo de token (normalmente "Bearer").
     */
    private String tokenType;
    
    /**
     * Perfil completo del usuario autenticado.
     */
    private AppUserResponseDto appUser;
}
