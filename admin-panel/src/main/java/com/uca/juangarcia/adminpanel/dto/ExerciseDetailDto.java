package com.uca.juangarcia.adminpanel.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Detalle completo de un ejercicio del catálogo (lectura).
 *
 * <p>Espejo de {@code ExerciseDetailDto} del microservicio iFit. Incluye instrucciones
 * paso a paso e {@code imageUrls} relativas (p. ej. {@code /exercise-images/Adductor/0.jpg}),
 * que la vista prefija con la base del gateway para resolver el {@code src}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExerciseDetailDto(
        Long id,
        String name,
        String force,
        String level,
        String mechanic,
        String equipment,
        String category,
        String code,
        List<String> primaryMuscles,
        List<String> secondaryMuscles,
        List<String> instructions,
        List<String> imageUrls
) implements Serializable {
}
