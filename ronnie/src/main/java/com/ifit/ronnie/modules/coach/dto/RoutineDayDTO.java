package com.ifit.ronnie.modules.coach.dto;

import java.util.List;

public record RoutineDayDto(
        Integer dayNumber,
        String dayName,
        String description,
        List<RoutineExerciseDto> exercises) {
}
