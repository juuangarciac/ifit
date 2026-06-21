package com.uca.juangarcia.adminpanel.dto;

import java.io.Serializable;

/**
 * Petición de creación de un nivel de experiencia ({@code POST /ifit/api/v1/experience-levels}).
 */
public record CreateExperienceLevelDto(String name, String description) implements Serializable {
}
