package com.uca.juangarcia.ifit.modules.exercises.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumen de un ejercicio del catálogo (sin instrucciones)")
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
) {}
