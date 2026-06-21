package com.uca.juangarcia.adminpanel.dto;

import java.io.Serializable;

/**
 * Petición de alta de un nuevo cliente ({@code POST /ifit/api/v1/auth/register}).
 *
 * <p>Refleja los campos obligatorios del {@code RegisterRequestDto} de iFit. Se usa el endpoint
 * de registro (no {@code POST /users}) porque es el único que crea la identidad de forma coherente
 * en <strong>Keycloak + base de datos</strong> de forma transaccional y dispara el email de
 * verificación. {@code birthDate} y {@code phone} se omiten (su validación está desactivada en el
 * backend y el panel no los gestiona).
 */
public record RegisterRequestDto(
        String name,
        String surname,
        String email,
        String password
) implements Serializable {
}
