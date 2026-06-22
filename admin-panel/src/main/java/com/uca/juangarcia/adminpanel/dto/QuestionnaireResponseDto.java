package com.uca.juangarcia.adminpanel.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Réplica (lectura) del {@code QuestionnaireResponseDto} de iFit.
 * Representa una sesión de cuestionario en formato compacto.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record QuestionnaireResponseDto(
    Long responseId,
    Boolean isCompleted,
    Integer totalQuestionsAnswered
) {
}
