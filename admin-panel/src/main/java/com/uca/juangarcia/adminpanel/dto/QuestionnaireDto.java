package com.uca.juangarcia.adminpanel.dto;

import java.time.LocalDateTime;

/**
 * DTO de respuesta completo para cuestionarios.
 * Refleja {@code com.uca.juangarcia.ifit.modules.questionnaire.dto.QuestionnaireDto}
 * sin acoplar admin-panel a ifit.
 *
 * @author Juan Garcia
 * @version 1.0
 */
public record QuestionnaireDto(
    Long id,
    String name,
    String description,
    String coachModelTypeName,
    String coachModelTypeEmoji,
    String experienceLevelName,
    Long firstQuestionId,
    Boolean isEnabled,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
