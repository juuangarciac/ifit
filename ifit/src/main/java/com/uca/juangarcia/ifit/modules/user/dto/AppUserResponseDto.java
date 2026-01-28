package com.uca.juangarcia.ifit.modules.user.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para información de usuario de la aplicación.
 * 
 * @param id ID único del usuario
 * @param name Nombre completo del usuario
 * @param email Dirección de correo electrónico del usuario
 * @param isRegistrationComplete Indica si el usuario completó el proceso de registro
 * @param isVerified Indica si el email del usuario ha sido verificado
 * @param createdAt Fecha y hora de creación del usuario
 * @param updatedAt Fecha y hora de última actualización del usuario
 * @param roleName Nombre del rol asignado al usuario
 * @param coachModelTypeName Nombre del modelo de coach asignado (puede ser null)
 * @param experienceLevelName Nombre del nivel de experiencia del usuario (puede ser null)
 * 
 * @author Juan Garcia
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información completa de un usuario")
public class AppUserResponseDto{

    @Schema(description = "ID único del usuario", example = "1")
    private Long id;
    
    @Schema(description = "Nombre completo del usuario", example = "Juan García")
    private String name;
    
    @Schema(description = "Email del usuario", example = "juan@example.com")
    private String email;
    
    @Schema(description = "Indica si el registro está completo", example = "true")
    private boolean isRegistrationComplete;
    
    @Schema(description = "Indica si el email está verificado", example = "true")
    private boolean isVerified;
    
    @Schema(description = "Fecha de creación del usuario")
    private LocalDateTime createdAt;
    
    @Schema(description = "Fecha de última actualización")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Rol del usuario", example = "USER")
    private String roleName;
    
    @Schema(description = "Tipo de modelo de coach asignado", example = "GPT-4", nullable = true)
    private String coachModelTypeName;
    
    @Schema(description = "Nivel de experiencia del usuario", example = "INTERMEDIATE", nullable = true)
    private String experienceLevelName;

    /* Json Ignore properties */
    @JsonIgnore
    @Schema(description = "ID del usuario en Keycloak")
    private String keycloakUserId;

    @JsonIgnore
    @Schema(description = "Código de verificación del usuario")
    private String verificationCode;
}
