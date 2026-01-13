package com.uca.juangarcia.ifit.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Datos para actualizar un nivel de experiencia existente")
public record UpdateExperienceLevelDto (
    @Schema(description = "Descripción del nivel de experiencia", example = "Nivel para personas que están empezando a entrenar")
    @NotBlank(message = "La descripción no puede estar vacía")
    String description
){
}
