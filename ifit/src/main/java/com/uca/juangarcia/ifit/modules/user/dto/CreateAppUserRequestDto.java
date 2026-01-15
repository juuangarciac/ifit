package com.uca.juangarcia.ifit.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de request para crear un nuevo usuario.
 * Contiene validaciones de Bean Validation para garantizar datos correctos.
 * 
 * @param name Nombre completo del usuario (3-50 caracteres)
 * @param password Contraseña del usuario (8-100 caracteres)
 * @param email Email válido del usuario
 * 
 * @author Juan Garcia
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Datos requeridos para crear un nuevo usuario")
public record CreateAppUserRequestDto(
    
    @Schema(description = "Nombre completo del usuario", example = "Juan García", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    String name,
    
    @Schema(description = "Contraseña del usuario", example = "SecurePass123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    String password,
    
    @Schema(description = "Email del usuario", example = "juan@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El email debe ser válido")
    String email,

    @Schema(description = "Keycloak ID del usuario", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String keycloakId
) {
}
