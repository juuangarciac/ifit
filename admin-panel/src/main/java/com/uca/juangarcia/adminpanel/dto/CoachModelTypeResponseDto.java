package com.uca.juangarcia.adminpanel.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Réplica (lectura) del {@code CoachModelTypeResponseDto} de iFit.
 * Versión leniente sin la validación del record original (las fechas se ignoran).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CoachModelTypeResponseDto(
        Long id,
        String name,
        String description,
        String emojiCharacter,
        Boolean enabled
) implements Serializable {
}
