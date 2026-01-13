package com.uca.juangarcia.ifit.modules.questionnaire.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO compacto para cuestionarios.
 * Usado en listados donde no se necesita toda la información detallada.
 * Ideal para selección de cuestionarios o dropdowns.
 * 
 * @param id ID único del cuestionario
 * @param name Nombre del cuestionario
 * @param description Descripción corta
 * @param coachModelTypeName Nombre del coach (puede ser null)
 * @param coachModelTypeEmoji Emoji del coach (puede ser null)
 * @param experienceLevelName Nivel de experiencia (puede ser null)
 * @param isEnabled Indica si está activo
 * 
 * @author Juan Garcia
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Resumen compacto de un cuestionario")
public record QuestionnaireSummaryDto(
    
    @Schema(description = "ID del cuestionario", example = "1")
    Long id,
    
    @Schema(description = "Nombre del cuestionario", example = "Ronnie - Fuerza para Principiantes")
    String name,
    
    @Schema(description = "Descripción breve", example = "Programa intenso de fuerza...")
    String description,
    
    @Schema(description = "Coach asignado", example = "Ronnie", nullable = true)
    String coachModelTypeName,
    
    @Schema(description = "Emoji del coach", example = "💪", nullable = true)
    String coachModelTypeEmoji,
    
    @Schema(description = "Nivel recomendado", example = "Principiante", nullable = true)
    String experienceLevelName,
    
    @Schema(description = "Estado del cuestionario", example = "true")
    Boolean isEnabled
) {
    
    public QuestionnaireSummaryDto {
        if (id == null) {
            throw new IllegalArgumentException("Questionnaire ID cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Questionnaire name cannot be null or blank");
        }
    }
}
