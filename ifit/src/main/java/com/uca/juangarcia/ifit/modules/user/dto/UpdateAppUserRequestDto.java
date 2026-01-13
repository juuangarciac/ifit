package com.uca.juangarcia.ifit.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO de request para actualizar un usuario existente.
 * Todos los campos son opcionales para permitir actualizaciones parciales.
 * 
 * @param name Nuevo nombre del usuario (opcional, 3-50 caracteres)
 * @param email Nuevo email del usuario (opcional, debe ser válido)
 * 
 * @author Juan Garcia
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Datos para actualizar un usuario existente")
public record UpdateAppUserRequestDto(
    
    @Schema(description = "Nuevo nombre del usuario", example = "Juan García Pérez", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    String name,
    
    @Schema(description = "Nuevo email del usuario", example = "nuevoemail@example.com", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Email(message = "El email debe ser válido")
    String email
) {
}
