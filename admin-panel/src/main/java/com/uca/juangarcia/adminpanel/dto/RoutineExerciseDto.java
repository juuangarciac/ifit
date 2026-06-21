package com.uca.juangarcia.adminpanel.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Ejercicio dentro de un día de rutina (lectura).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RoutineExerciseDto(
        Long id,
        String exerciseName,
        Integer sets,
        String reps,
        Integer restSeconds,
        String notes,
        Integer orderIndex
) implements Serializable {
}
