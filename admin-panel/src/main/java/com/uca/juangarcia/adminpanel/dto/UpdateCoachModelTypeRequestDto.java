package com.uca.juangarcia.adminpanel.dto;

import java.io.Serializable;

/**
 * Petición de actualización de un tipo de coach ({@code PUT /ifit/api/v1/coach-models/{id}}).
 */
public record UpdateCoachModelTypeRequestDto(
        String name,
        String description,
        String emojiCharacter,
        Boolean enabled
) implements Serializable {
}
