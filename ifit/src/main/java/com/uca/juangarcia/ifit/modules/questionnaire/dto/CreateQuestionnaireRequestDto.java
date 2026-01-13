package com.uca.juangarcia.ifit.modules.questionnaire.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para crear un nuevo cuestionario.
 * Solo incluye los campos que el usuario debe proporcionar al crear.
 * 
 * @param name Nombre del cuestionario
 * @param description Descripción del cuestionario
 * @param coachModelTypeId ID del coach asociado (opcional)
 * @param experienceLevelId ID del nivel de experiencia (opcional)
 * @param firstQuestionId ID de la primera pregunta (opcional al crear, se puede asignar después)
 * 
 * @author Juan Garcia
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Datos para crear un nuevo cuestionario")
public record CreateQuestionnaireRequestDto(
    
    @NotBlank(message = "El nombre del cuestionario no puede estar vacío")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    @Schema(description = "Nombre único del cuestionario", 
            example = "Ronnie - Fuerza para Principiantes")
    String name,
    
    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(min = 10, max = 1000, message = "La descripción debe tener entre 10 y 1000 caracteres")
    @Schema(description = "Descripción detallada del cuestionario", 
            example = "Programa intenso de fuerza diseñado por Ronnie. Ideal para quienes quieren construir músculo desde cero.")
    String description,
    
    @Schema(description = "ID del coach asociado (opcional)", 
            example = "2",
            nullable = true)
    Long coachModelTypeId,
    
    @Schema(description = "ID del nivel de experiencia recomendado (opcional)", 
            example = "1",
            nullable = true)
    Long experienceLevelId,
    
    @Schema(description = "ID de la primera pregunta del árbol (opcional, se puede asignar después)", 
            example = "1",
            nullable = true)
    Long firstQuestionId
) {
}
