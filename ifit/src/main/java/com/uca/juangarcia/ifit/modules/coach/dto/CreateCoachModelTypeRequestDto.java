package com.uca.juangarcia.ifit.modules.coach.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de request para crear un nuevo tipo de modelo de coach.
 * Utiliza un record de Java para inmutabilidad y concisión.
 * 
 * <p>Este DTO contiene solo los campos necesarios para crear un nuevo
 * tipo de modelo de coach. Los campos como ID, fechas de creación y
 * actualización son gestionados automáticamente por el sistema.
 * 
 * @param name Nombre del modelo (requerido, único)
 * @param description Descripción del modelo (opcional)
 * @param emojiCharacter Emoji representativo (opcional)
 * @param enabled Estado inicial del modelo (requerido, por defecto true)
 * 
 * @author Juan Garcia
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Datos para crear un nuevo tipo de modelo de coach")
public record CreateCoachModelTypeRequestDto(
    
    @Schema(description = "Nombre del modelo de coach", 
            example = "GPT-4",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El nombre del modelo no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    String name,
    
    @Schema(description = "Descripción detallada del modelo",
            example = "Modelo de lenguaje avanzado con capacidades de razonamiento profundo")
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    String description,
    
    @Schema(description = "Emoji representativo del modelo", 
            example = "🤖")
    @Size(max = 10, message = "El emoji no puede exceder 10 caracteres")
    String emojiCharacter,
    
    @Schema(description = "Indica si el modelo debe estar habilitado inicialmente",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED,
            defaultValue = "true")
    @NotNull(message = "El estado de habilitación es requerido")
    Boolean enabled
) {
    /**
     * Constructor compacto que valida los datos de entrada.
     * Aplica valores por defecto y normalizaciones.
     * 
     * @throws IllegalArgumentException si los datos son inválidos
     */
    public CreateCoachModelTypeRequestDto {
        // Normalizar el nombre (trim)
        if (name != null) {
            name = name.trim();
        }
        
        // Si enabled es null, establecer valor por defecto
        if (enabled == null) {
            enabled = true;
        }
    }
}
