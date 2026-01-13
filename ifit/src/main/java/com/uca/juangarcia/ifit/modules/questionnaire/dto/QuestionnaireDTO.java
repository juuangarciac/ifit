package com.uca.juangarcia.ifit.modules.questionnaire.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de respuesta completo para cuestionarios.
 * Incluye toda la información necesaria para mostrar un cuestionario en el frontend.
 * 
 * @param id ID único del cuestionario
 * @param name Nombre del cuestionario
 * @param description Descripción detallada
 * @param coachModelTypeName Nombre del coach asociado (puede ser null)
 * @param coachModelTypeEmoji Emoji del coach (puede ser null)
 * @param experienceLevelName Nombre del nivel de experiencia (puede ser null)
 * @param firstQuestionId ID de la primera pregunta del árbol (puede ser null)
 * @param isEnabled Indica si el cuestionario está activo
 * @param createdAt Fecha de creación
 * @param updatedAt Fecha de última actualización
 * 
 * @author Juan Garcia
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Información completa de un cuestionario")
public record QuestionnaireDTO(
    
    @Schema(description = "ID único del cuestionario", example = "1")
    Long id,
    
    @Schema(description = "Nombre del cuestionario", example = "Ronnie - Fuerza para Principiantes")
    String name,
    
    @Schema(description = "Descripción detallada del cuestionario", 
            example = "Programa intenso de fuerza diseñado por Ronnie...")
    String description,
    
    @Schema(description = "Nombre del coach asociado", example = "Ronnie", nullable = true)
    String coachModelTypeName,
    
    @Schema(description = "Emoji del coach", example = "💪", nullable = true)
    String coachModelTypeEmoji,
    
    @Schema(description = "Nivel de experiencia recomendado", example = "Principiante", nullable = true)
    String experienceLevelName,
    
    @Schema(description = "ID de la primera pregunta", example = "1", nullable = true)
    Long firstQuestionId,
    
    @Schema(description = "Indica si el cuestionario está habilitado", example = "true")
    Boolean isEnabled,
    
    @Schema(description = "Fecha de creación del cuestionario")
    LocalDateTime createdAt,
    
    @Schema(description = "Fecha de última actualización")
    LocalDateTime updatedAt
) {
    
    /**
     * Constructor compacto que valida que los campos requeridos no sean nulos.
     * 
     * @throws IllegalArgumentException si algún campo requerido es nulo
     */
    public QuestionnaireDTO {
        if (id == null) {
            throw new IllegalArgumentException("Questionnaire ID cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Questionnaire name cannot be null or blank");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Questionnaire description cannot be null or blank");
        }
        if (isEnabled == null) {
            throw new IllegalArgumentException("isEnabled cannot be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt cannot be null");
        }
        if (updatedAt == null) {
            throw new IllegalArgumentException("updatedAt cannot be null");
        }
    }
}