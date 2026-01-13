package com.uca.juangarcia.ifit.modules.coach.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * DTO de request para actualizar un tipo de modelo de coach existente.
 * Utiliza un record de Java para inmutabilidad y concisión.
 * 
 * <p>Todos los campos son opcionales, permitiendo actualizaciones parciales.
 * Solo se actualizarán los campos que no sean nulos. El sistema actualizará
 * automáticamente la fecha de última modificación.
 * 
 * @param name Nuevo nombre del modelo (opcional)
 * @param description Nueva descripción del modelo (opcional)
 * @param emojiCharacter Nuevo emoji representativo (opcional)
 * @param enabled Nuevo estado de habilitación (opcional)
 * 
 * @author Juan Garcia
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Datos para actualizar un tipo de modelo de coach existente")
public record UpdateCoachModelTypeRequestDto(
    
    @Schema(description = "Nombre del modelo de coach", 
            example = "GPT-4 Turbo")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    String name,
    
    @Schema(description = "Descripción detallada del modelo",
            example = "Versión mejorada con mayor velocidad y capacidad de contexto")
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    String description,
    
    @Schema(description = "Emoji representativo del modelo", 
            example = "⚡")
    @Size(max = 10, message = "El emoji no puede exceder 10 caracteres")
    String emojiCharacter,
    
    @Schema(description = "Indica si el modelo debe estar habilitado",
            example = "false")
    Boolean enabled
) {
    /**
     * Constructor compacto que normaliza los datos de entrada.
     * 
     * @throws IllegalArgumentException si los datos son inválidos
     */
    public UpdateCoachModelTypeRequestDto {
        // Normalizar el nombre si no es null (trim)
        if (name != null) {
            name = name.trim();
            if (name.isBlank()) {
                throw new IllegalArgumentException("El nombre no puede estar vacío");
            }
        }
    }
    
    /**
     * Verifica si la actualización contiene algún cambio.
     * 
     * @return true si al menos un campo no es nulo, false si todos son nulos
     */
    public boolean hasChanges() {
        return name != null || description != null || 
               emojiCharacter != null || enabled != null;
    }
}
