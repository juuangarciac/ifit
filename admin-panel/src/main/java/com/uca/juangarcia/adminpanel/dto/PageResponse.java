package com.uca.juangarcia.adminpanel.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Envoltorio genérico para respuestas paginadas de Spring Data ({@code Page<T>}).
 * Solo mapeamos los campos que el panel necesita.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PageResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int number,
        boolean last
) implements Serializable {
}
