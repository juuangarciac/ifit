package com.uca.juangarcia.ifit.modules.exercises.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalle completo de un ejercicio del catálogo")
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
) {}
