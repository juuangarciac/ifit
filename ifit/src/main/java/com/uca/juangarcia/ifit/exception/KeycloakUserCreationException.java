package com.uca.juangarcia.ifit.exception;

/**
 * Excepción lanzada cuando falla la creación de un usuario en Keycloak.
 * 
 * Esta excepción se utiliza para encapsular errores durante el proceso
 * de creación de usuarios en Keycloak, como:
 * - Usuario ya existe (conflicto 409)
 * - Error de comunicación con el servidor Keycloak
 * - Configuración incorrecta de roles
 * 
 * @author Juan Garcia
 * @version 1.0
 */
public class KeycloakUserCreationException extends RuntimeException {
    
    /**
     * Crea una nueva excepción con el mensaje especificado.
     * 
     * @param message mensaje descriptivo del error
     */
    public KeycloakUserCreationException(String message) {
        super(message);
    }
    
    /**
     * Crea una nueva excepción con mensaje y causa raíz.
     * 
     * @param message mensaje descriptivo del error
     * @param cause causa original de la excepción
     */
    public KeycloakUserCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
