package com.ifit.ronnie.modules.coach.dto;

import java.util.List;

public record RoutineResponseDto(
        String message,
        String description,
        Integer trainingDays,
        List<RoutineDayDto> days) {
}
