package com.uca.juangarcia.adminpanel.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Réplica (lectura) del {@code RoutineResponseDto} de iFit.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RoutineResponseDto(
        Long id,
        Long userId,
        String message,
        String description,
        Integer trainingDays,
        Boolean isActive,
        Boolean deleted,
        Integer currentDay,
        List<RoutineDayDto> days
) implements Serializable {
}
