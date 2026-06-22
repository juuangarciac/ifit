package com.ifit.ronnie.modules.coach.dto;

public record RoutineExerciseDto(
        String exerciseName,
        Integer sets,
        String reps,
        Integer restSeconds,
        String notes,
        Integer orderIndex) {
}
