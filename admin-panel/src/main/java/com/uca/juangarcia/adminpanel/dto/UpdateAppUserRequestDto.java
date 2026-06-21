package com.uca.juangarcia.adminpanel.dto;

import java.io.Serializable;

/**
 * Petición de actualización parcial de usuario ({@code PUT /ifit/api/v1/users/{id}}).
 * Refleja el {@code UpdateAppUserRequestDto} de iFit (solo nombre y email).
 */
public record UpdateAppUserRequestDto(String name, String email) implements Serializable {
}
