package com.uca.juangarcia.adminpanel.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Nivel de experiencia (lectura) — {@code GET /ifit/api/v1/experience-levels}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExperienceLevelDto(
        Long id,
        String name,
        String description
) implements Serializable {
}
