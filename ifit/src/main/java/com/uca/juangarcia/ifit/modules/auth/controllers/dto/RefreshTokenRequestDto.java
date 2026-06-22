package com.uca.juangarcia.ifit.modules.auth.controllers.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDto(
    @NotBlank(message = "Refresh token is required")
    String refreshToken
) {}
