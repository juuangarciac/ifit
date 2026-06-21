package com.uca.juangarcia.adminpanel.dto;

import java.io.Serializable;

/**
 * Petición de creación de un tipo de coach ({@code POST /ifit/api/v1/coach-models}).
 */
public record CreateCoachModelTypeRequestDto(
        String name,
        String description,
        String emojiCharacter,
        Boolean enabled
) implements Serializable {
}
