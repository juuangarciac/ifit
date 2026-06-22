package com.ifit.ronnie.modules.coach;

import com.ifit.ronnie.modules.coach.dto.RoutineResponseDto;

/**
 * Validación de la calidad de una rutina generada por el LLM.
 *
 * <p>El modelo razonador (gpt-oss) a veces devuelve un JSON estructuralmente
 * válido pero con los días <em>sin ejercicios</em> ({@code exercises: []}). No se
 * puede rechazar cualquier día vacío, porque un día de descanso legítimo también
 * va sin ejercicios. Por eso se considera <strong>degradada</strong> solo cuando
 * los días sin ejercicios son <strong>estrictamente mayoría</strong> frente a los
 * que sí tienen.
 */
public final class RoutineValidation {

    private RoutineValidation() {
    }

    /**
     * Indica si una rutina está degradada y conviene regenerarla.
     *
     * <p>Regla: {@code díasSinEjercicios > díasConEjercicios} (estricto).
     * Una rutina sin días se considera degradada.
     *
     * @param routine rutina devuelta por el modelo (puede ser nula)
     * @return {@code true} si debe regenerarse
     */
    public static boolean isDegraded(RoutineResponseDto routine) {
        if (routine == null || routine.days() == null || routine.days().isEmpty()) {
            return true;
        }

        long withExercises = routine.days().stream()
                .filter(d -> d.exercises() != null && !d.exercises().isEmpty())
                .count();
        long withoutExercises = routine.days().size() - withExercises;

        return withoutExercises > withExercises;
    }
}
