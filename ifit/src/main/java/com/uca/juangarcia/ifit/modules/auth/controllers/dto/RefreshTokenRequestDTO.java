package com.uca.juangarcia.ifit.modules.auth.controllers.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de request para refrescar tokens.
 * 
 * <p>Utilizado en el endpoint {@code /auth/refresh} para obtener
 * nuevos access y refresh tokens usando un refresh token válido.
 * 
 * <p><strong>Diferencias con LoginRequestDTO:</strong>
 * <ul>
 *   <li>LoginRequestDTO: Requiere email + password</li>
 *   <li>RefreshTokenRequestDTO: Requiere SOLO el refreshToken</li>
 * </ul>
 * 
 * @author Juan Garcia
 * @version 1.0
 * @since 2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequestDTO {
    
    /**
     * Refresh token actual que será usado para obtener nuevos tokens.
     * 
     * <p>Este token fue devuelto por Keycloak en un login o refresh previo.
     */
    @NotBlank(message = "El refresh token es requerido")
    private String refreshToken;
}
