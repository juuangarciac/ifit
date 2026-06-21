package com.uca.juangarcia.adminpanel.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Día de entrenamiento dentro de una rutina (lectura), con sus ejercicios.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RoutineDayDto(
        Long id,
        Integer dayNumber,
        String dayName,
        String description,
        List<RoutineExerciseDto> exercises
) implements Serializable {
}
