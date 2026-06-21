package com.uca.juangarcia.adminpanel.dto;

import java.io.Serializable;

/**
 * Petición de login enviada al Gateway ({@code POST /ifit/api/v1/auth/login}).
 * Se serializa como {@code {"username": ..., "password": ...}}.
 */
public record LoginRequestDto(String username, String password) implements Serializable {
}
