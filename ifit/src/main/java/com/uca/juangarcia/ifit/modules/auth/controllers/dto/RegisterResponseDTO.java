package com.uca.juangarcia.ifit.modules.auth.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para operaciones de registro de usuario.
 * 
 * <p>Este DTO se devuelve cuando un usuario se registra exitosamente
 * pero aún no ha verificado su email. NO contiene tokens de acceso.
 * 
 * <p>Uso típico:
 * <pre>
 * RegisterResponseDto response = RegisterResponseDto.builder()
 *     .success(true)
 *     .message("Usuario registrado exitosamente. Verifica tu email.")
 *     .email("user@example.com")
 *     .requiresEmailVerification(true)
 *     .build();
 * </pre>
 * 
 * @author Juan Garcia
 * @version 1.0
 * @since 2.1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponseDto {
    
    /**
     * Indica si el registro fue exitoso
     */
    private boolean success;
    
    /**
     * Mensaje descriptivo del resultado del registro
     */
    private String message;
    
    /**
     * Email del usuario registrado
     */
    private String email;
    
    /**
     * Indica si el usuario necesita verificar su email antes de hacer login
     */
    private boolean requiresEmailVerification;
}