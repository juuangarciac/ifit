package com.uca.juangarcia.ifit.modules.user.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

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

    /**
     * Constructor compacto que valida que los campos requeridos no sean nulos.
     * 
     * @throws IllegalArgumentException si algún campo requerido es nulo
     */
    public AppUserResponseDto(Long id, String name, String email, boolean isRegistrationComplete,
            boolean isVerified, LocalDateTime createdAt, LocalDateTime updatedAt,
            String roleName, String coachModelTypeName, String experienceLevelName) {
        if (id == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("User name cannot be null or blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("User email cannot be null or blank");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("Created date cannot be null");
        }

        this.id = id;
        this.name = name;
        this.email = email;
        this.isRegistrationComplete = isRegistrationComplete;
        this.isVerified = isVerified;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.roleName = roleName;
        this.coachModelTypeName = coachModelTypeName;
        this.experienceLevelName = experienceLevelName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isRegistrationComplete() {
        return isRegistrationComplete;
    }

    public void setRegistrationComplete(boolean isRegistrationComplete) {
        this.isRegistrationComplete = isRegistrationComplete;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getCoachModelTypeName() {
        return coachModelTypeName;
    }

    public void setCoachModelTypeName(String coachModelTypeName) {
        this.coachModelTypeName = coachModelTypeName;
    }

    public String getExperienceLevelName() {
        return experienceLevelName;
    }

    public void setExperienceLevelName(String experienceLevelName) {
        this.experienceLevelName = experienceLevelName;
    }

    
}
