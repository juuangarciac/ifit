package com.uca.juangarcia.ifit.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @Data Transfer Object para la creación de un nuevo nivel de experiencia.
 * Contiene los datos necesarios para crear un nivel de experiencia en el sistema.
 * 
 * 
 * @author Juan Garcia Candon
 * 
 */
@Schema(description = "Datos requeridos para crear un nuevo nivel de experiencia")
public record CreateExperienceLevelDto( 
    @Schema(description = "Nombre del nivel de experiencia", example = "Principiante", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 3, max = 30, message = "El nombre debe tener entre 3 y 30 caracteres")
    String name,

    @Schema(description = "Descripción del nivel de experiencia", example = "Nivel para personas que están empezando a entrenar")
    @NotBlank(message = "La descripción no puede estar vacía")
    String description
){

}
