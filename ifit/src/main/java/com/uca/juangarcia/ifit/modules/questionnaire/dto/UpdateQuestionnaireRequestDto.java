package com.uca.juangarcia.ifit.modules.questionnaire.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * DTO para actualizar un cuestionario existente.
 * Todos los campos son opcionales (actualizaciones parciales).
 * Solo los campos no nulos serán actualizados.
 * 
 * @param name Nuevo nombre (opcional)
 * @param description Nueva descripción (opcional)
 * @param coachModelTypeId Nuevo ID de coach (opcional, null para quitar asignación)
 * @param experienceLevelId Nuevo ID de nivel (opcional, null para quitar asignación)
 * @param firstQuestionId Nuevo ID de primera pregunta (opcional)
 * @param isEnabled Nuevo estado de habilitación (opcional)
 * 
 * @author Juan Garcia
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Datos para actualizar un cuestionario existente")
public record UpdateQuestionnaireRequestDto(
    
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    @Schema(description = "Nuevo nombre del cuestionario (opcional)", 
            example = "Ronnie - Fuerza Mejorado",
            nullable = true)
    String name,
    
    @Size(min = 10, max = 1000, message = "La descripción debe tener entre 10 y 1000 caracteres")
    @Schema(description = "Nueva descripción (opcional)", 
            example = "Descripción actualizada del programa...",
            nullable = true)
    String description,
    
    @Schema(description = "Nuevo ID de coach (opcional, null para quitar asignación)", 
            example = "3",
            nullable = true)
    Long coachModelTypeId,
    
    @Schema(description = "Nuevo ID de nivel de experiencia (opcional, null para quitar asignación)", 
            example = "2",
            nullable = true)
    Long experienceLevelId,
    
    @Schema(description = "Nuevo ID de primera pregunta (opcional)", 
            example = "5",
            nullable = true)
    Long firstQuestionId,
    
    @Schema(description = "Nuevo estado de habilitación (opcional)", 
            example = "false",
            nullable = true)
    Boolean isEnabled
) {
}
