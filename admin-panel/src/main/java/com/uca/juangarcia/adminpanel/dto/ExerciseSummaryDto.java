package com.uca.juangarcia.adminpanel.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Resumen de un ejercicio del catálogo (lectura).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExerciseSummaryDto(
        Long id,
        String name,
        String level,
        String category,
        String equipment,
        String mechanic,
        List<String> primaryMuscles,
        List<String> secondaryMuscles,
        List<String> imageUrls
) implements Serializable {
}
