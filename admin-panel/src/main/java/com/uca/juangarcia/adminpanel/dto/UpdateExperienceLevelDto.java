package com.uca.juangarcia.adminpanel.dto;

import java.io.Serializable;

/**
 * Petición de actualización de un nivel de experiencia ({@code PATCH /ifit/api/v1/experience-levels/{id}}).
 * El backend solo permite actualizar la descripción.
 */
public record UpdateExperienceLevelDto(String description) implements Serializable {
}
