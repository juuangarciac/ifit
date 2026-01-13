package com.uca.juangarcia.ifit.modules.coach.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para información de tipos de modelo de coach.
 * Utiliza un record de Java para inmutabilidad y concisión.
 * 
 * <p>Este DTO representa la información completa de un tipo de modelo de coach
 * que puede ser asignado a los usuarios de la aplicación. Incluye todos los
 * campos de la entidad para proporcionar una vista completa al cliente.
 * 
 * @param id ID único del tipo de modelo de coach
 * @param name Nombre del modelo (ej: "GPT-4", "Claude", "Gemini")
 * @param description Descripción detallada de las capacidades del modelo
 * @param emojiCharacter Emoji representativo del modelo para la UI
 * @param enabled Indica si el modelo está actualmente disponible
 * @param createdAt Fecha y hora de creación del registro
 * @param updatedAt Fecha y hora de última actualización
 * 
 * @author Juan Garcia
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Información completa de un tipo de modelo de coach")
public record CoachModelTypeResponseDto(
    
    @Schema(description = "ID único del tipo de modelo", example = "1")
    Long id,
    
    @Schema(description = "Nombre del modelo de coach", example = "GPT-4")
    String name,
    
    @Schema(description = "Descripción detallada del modelo", 
            example = "Modelo de lenguaje avanzado con capacidades de razonamiento profundo")
    String description,
    
    @Schema(description = "Emoji representativo del modelo", example = "🤖")
    String emojiCharacter,
    
    @Schema(description = "Indica si el modelo está habilitado", example = "true")
    Boolean enabled,
    
    @Schema(description = "Fecha de creación del registro")
    LocalDateTime createdAt,
    
    @Schema(description = "Fecha de última actualización", nullable = true)
    LocalDateTime updatedAt
) {
    /**
     * Constructor compacto que valida que los campos requeridos no sean nulos.
     * 
     * @throws IllegalArgumentException si algún campo requerido es nulo o inválido
     */
    public CoachModelTypeResponseDto {
        if (id == null) {
            throw new IllegalArgumentException("Coach model type ID cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Coach model type name cannot be null or blank");
        }
        if (enabled == null) {
            throw new IllegalArgumentException("Enabled status cannot be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("Created date cannot be null");
        }
    }
}
