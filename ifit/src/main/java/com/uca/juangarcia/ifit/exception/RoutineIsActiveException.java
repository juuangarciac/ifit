package com.uca.juangarcia.ifit.exception;

/**
 * Excepción lanzada cuando se intenta eliminar una rutina que está activa.
 *
 * @author Juan Garcia
 * @version 1.0
 */
public class RoutineIsActiveException extends Exception {

    private static final long serialVersionUID = 1L;

    public RoutineIsActiveException(Long routineId) {
        super("La rutina con ID " + routineId + " es la rutina activa del usuario y no puede ser eliminada. " +
              "Activa otra rutina antes de eliminar ésta.");
    }
}
