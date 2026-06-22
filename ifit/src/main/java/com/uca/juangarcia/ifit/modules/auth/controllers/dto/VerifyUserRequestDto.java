package com.uca.juangarcia.ifit.modules.auth.controllers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO de solicitud para verificación de usuario")
public record VerifyUserRequestDto(

    /**
     * Email del usuario (también se usa como username).
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email,

    /**
     * Código de verificación enviado al email del usuario.
     */
    @NotBlank(message = "Verification code is required")
    String verificationCode,

    /**
     * Contraseña del usuario.
     */
    @NotBlank(message = "Password is required")
    String password
) { 
}
