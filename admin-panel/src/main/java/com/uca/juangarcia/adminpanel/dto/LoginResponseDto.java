package com.uca.juangarcia.adminpanel.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Respuesta de login/refresh de iFit: tokens de Keycloak + perfil del usuario.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LoginResponseDto(
        String accessToken,
        String refreshToken,
        Integer expiresIn,
        String tokenType,
        AppUserResponseDto appUser
) implements Serializable {
}
