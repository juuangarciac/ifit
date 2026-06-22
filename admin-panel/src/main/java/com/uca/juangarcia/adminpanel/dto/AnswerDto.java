package com.uca.juangarcia.adminpanel.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Réplica (lectura) del {@code AnswerDto} de iFit.
 * Una respuesta concreta dentro de una sesión de cuestionario.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AnswerDto(
    Long answerId,
    String questionText,
    String selectedOption,
    String additionalText,
    String aiDescription,
    LocalDateTime answeredAt
) {
}
