package com.uca.juangarcia.ifit.exception.dto;

/**
 * Excepción lanzada cuando un usuario intenta hacer login sin haber verificado su email.
 * 
 * <p>Esta excepción se lanza en dos casos:
 * <ul>
 *   <li>El usuario tiene emailVerified=false en la base de datos</li>
 *   <li>El usuario tiene emailVerified=false en Keycloak</li>
 * </ul>
 * 
 * @author Juan Garcia
 * @version 1.0
 * @since 2.1
 */
public class EmailNotVerifiedException extends RuntimeException {
    
    private final String email;
    
    /**
     * Constructor con email del usuario
     * 
     * @param email el email que no está verificado
     */
    public EmailNotVerifiedException(String email) {
        super(String.format("Email no verificado: %s. Por favor, verifica tu correo antes de iniciar sesión.", email));
        this.email = email;
    }
    
    /**
     * Obtiene el email del usuario
     * 
     * @return el email no verificado
     */
    public String getEmail() {
        return email;
    }
}