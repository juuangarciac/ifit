package com.uca.juangarcia.ifit.modules.auth.controllers.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de response para logout.
 * 
 * <p>Utilizado en el endpoint {@code /auth/logout} para confirmar
 * que la sesión del usuario ha sido cerrada exitosamente.
 * 
 * <p>Actualmente no contiene campos, pero se puede extender en el futuro
 * para incluir información adicional si es necesario.
 * 
 * @author Juan Garcia
 * @version 1.0
 * @since 2.1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutResponseDTO {
    private String message;
    private Boolean success;
}
