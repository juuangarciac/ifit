package com.uca.juangarcia.adminpanel.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Réplica (lectura) del {@code QuestionnaireResponseSummaryDto} de iFit.
 * Vista completa de una sesión de cuestionario de un usuario, con sus respuestas.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record QuestionnaireResponseSummaryDto(
    Long responseId,
    Long userId,
    String userName,
    Long questionnaireId,
    String questionnaireName,
    String questionnaireDescription,
    List<AnswerDto> answers,
    LocalDateTime startedAt,
    LocalDateTime completedAt,
    Boolean isCompleted
) {
}
