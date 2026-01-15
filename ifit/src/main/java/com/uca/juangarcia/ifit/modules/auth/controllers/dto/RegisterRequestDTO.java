package com.uca.juangarcia.ifit.modules.auth.controllers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para solicitudes de registro de nuevos usuarios.
 * 
 * Contiene todos los datos necesarios para crear un nuevo usuario
 * tanto en Keycloak como en la base de datos de la aplicación.
 * 
 * Validaciones aplicadas:
 * - Email válido y obligatorio
 * - Password mínimo 8 caracteres
 * - Nombre y apellido entre 2-50 caracteres
 * - Fecha de nacimiento en el pasado
 * - Teléfono español válido (opcional)
 * 
 * @author Juan Garcia
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {
    
    /**
     * Nombre del usuario.
     */
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;
    
    /**
     * Apellido del usuario.
     */
    @NotBlank(message = "Surname is required")
    @Size(min = 2, max = 50, message = "Surname must be between 2 and 50 characters")
    private String surname;
    
    /**
     * Email del usuario (también se usa como username).
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    /**
     * Contraseña del usuario (mínimo 8 caracteres).
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    
    /**
     * Fecha de nacimiento del usuario.
     */
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;
    
    /**
     * Teléfono del usuario (formato español: +34XXXXXXXXX o XXXXXXXXX).
     */
    @Pattern(
        regexp = "^(\\+34)?[6-9][0-9]{8}$", 
        message = "Phone must be a valid Spanish phone number"
    )
    private String phone;
}
