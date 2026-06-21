package com.uca.juangarcia.adminpanel.dto;

/**
 * DTO para actualizar un cuestionario existente.
 * Todos los campos son opcionales (actualización parcial).
 * Refleja {@code com.uca.juangarcia.ifit.modules.questionnaire.dto.UpdateQuestionnaireRequestDto}
 *
 * @author Juan Garcia
 * @version 1.0
 */
public record UpdateQuestionnaireRequestDto(
    String name,
    String description,
    Long coachModelTypeId,
    Long experienceLevelId,
    Long firstQuestionId,
    Boolean isEnabled
) {
}
