package com.uca.juangarcia.ifit.modules.auth.controllers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO para solicitar el reenvío del email de verificación")
public record ResendVerificationRequestDto(

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email
) {}
