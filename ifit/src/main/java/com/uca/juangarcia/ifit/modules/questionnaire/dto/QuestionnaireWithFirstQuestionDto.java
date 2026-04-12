package com.uca.juangarcia.ifit.modules.questionnaire.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO especial que incluye la primera pregunta completa con sus opciones.
 * Usado cuando el usuario inicia un cuestionario.
 * Combina información del cuestionario + primera pregunta para reducir llamadas a la API.
 * 
 * @param id ID del cuestionario
 * @param name Nombre del cuestionario
 * @param description Descripción
 * @param coachModelTypeName Nombre del coach
 * @param coachModelTypeEmoji Emoji del coach
 * @param experienceLevelName Nivel de experiencia
 * @param isEnabled Estado
 * @param createdAt Fecha de creación
 * @param updatedAt Fecha de actualización
 * @param firstQuestion Primera pregunta del árbol con todas sus opciones
 * 
 * @author Juan Garcia
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Cuestionario con su primera pregunta incluida (para iniciar)")
public record QuestionnaireWithFirstQuestionDto(
    
    @Schema(description = "ID del cuestionario", example = "1")
    Long id,
    
    @Schema(description = "Nombre del cuestionario", example = "Ronnie - Fuerza para Principiantes")
    String name,
    
    @Schema(description = "Descripción del cuestionario")
    String description,
    
    @Schema(description = "Coach asociado", example = "Ronnie", nullable = true)
    String coachModelTypeName,
    
    @Schema(description = "Emoji del coach", example = "💪", nullable = true)
    String coachModelTypeEmoji,
    
    @Schema(description = "Nivel recomendado", example = "Principiante", nullable = true)
    String experienceLevelName,
    
    @Schema(description = "Estado del cuestionario", example = "true")
    Boolean isEnabled,
    
    @Schema(description = "Fecha de creación")
    LocalDateTime createdAt,
    
    @Schema(description = "Fecha de actualización")
    LocalDateTime updatedAt,
    
    @Schema(description = "Primera pregunta del cuestionario con todas sus opciones")
    QuestionDto firstQuestion
) {
    
    public QuestionnaireWithFirstQuestionDto {
        if (id == null) {
            throw new IllegalArgumentException("Questionnaire ID cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Questionnaire name cannot be null or blank");
        }
        if (firstQuestion == null) {
            throw new IllegalArgumentException("First question cannot be null when starting a questionnaire");
        }
    }
}
