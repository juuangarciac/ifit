package com.uca.juangarcia.ifit.exception;

/**
 * Excepción lanzada cuando no se encuentra una rutina en el sistema.
 * 
 * @author Juan Garcia
 * @version 1.0
 */
public class RoutineNotFoundException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    public RoutineNotFoundException(String message) {
        super(message);
    }
    
    public RoutineNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
