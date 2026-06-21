package com.uca.juangarcia.adminpanel.dto;

/**
 * DTO para crear un nuevo cuestionario.
 * Refleja {@code com.uca.juangarcia.ifit.modules.questionnaire.dto.CreateQuestionnaireRequestDto}
 *
 * @author Juan Garcia
 * @version 1.0
 */
public record CreateQuestionnaireRequestDto(
    String name,
    String description,
    Long coachModelTypeId,
    Long experienceLevelId,
    Long firstQuestionId
) {
}
