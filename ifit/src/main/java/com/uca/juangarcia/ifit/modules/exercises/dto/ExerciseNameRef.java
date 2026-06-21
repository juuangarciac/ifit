package com.uca.juangarcia.ifit.modules.exercises.dto;

/**
 * Proyección ligera del catálogo: solo id y nombre.
 *
 * <p>Se usa para construir el índice de normalización de nombres de ejercicios
 * sin cargar las columnas pesadas (instrucciones, imágenes…).
 *
 * @author Juan Garcia
 * @version 1.0
 */
public record ExerciseNameRef(Long id, String name) {
}
