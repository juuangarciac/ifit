/**
 * Mock para Groq API
 * Evita depender de la API real de Groq en tests
 */

export const groqMockResponses = {
  coaching: {
    basic: `¡Hola! Soy Ronnie, tu coach personal especializado en hipertrofia y musculación.
    Basándome en lo que me compartiste, puedo ayudarte a desarrollar un plan de entrenamiento personalizado.
    Para ganar masa muscular a tu nivel, necesitamos enfocarnos en ejercicios compuestos y crear un deficit calórico controlado.
    ¿Cuáles son tus objetivos específicos?`,

    advanced: `Entendido. Para maximizar la hipertrofia con 4 días por semana de entrenamiento, recomiendo:

    - Dividir en Upper/Lower split
    - Enfoque en ejercicios compuestos (Squat, Bench, Deadlift, Row)
    - Volumen moderado a alto
    - Descanso de 48-72 horas entre grupos musculares

    ¿Tienes acceso a un gimnasio completamente equipado?`,

    followup: `Perfecto. Vamos a crear una rutina optimizada para ti.
    Considerando tus 25 años, altura 180cm y peso 75kg, tenemos bastante potencial.

    Mi recomendación es un programa de 4 días tipo Upper/Lower Push/Pull.
    Comenzaremos con volumen moderado e iremos incrementando conforme ganes experiencia.`,
  },

  routine: {
    hypertrophy: `{
  "nombre": "Rutina Hipertrofia 4 Días - Ronnie Edition",
  "nivel": "Intermedio",
  "duracion_semanas": 12,
  "dias_semana": 4,
  "descripcion": "Programa diseñado para maximizar ganancia de masa muscular con enfoque en ejercicios compuestos.",
  "schedule": [
    {
      "dia": 1,
      "nombre": "Upper Power",
      "musculos": ["pecho", "espalda", "hombros"],
      "ejercicios": [
        {
          "nombre": "Bench Press",
          "series": 4,
          "repeticiones": "6-8",
          "descanso_segundos": 120,
          "intensidad": "80-85%"
        },
        {
          "nombre": "Bent Over Rows",
          "series": 4,
          "repeticiones": "6-8",
          "descanso_segundos": 120,
          "intensidad": "80-85%"
        },
        {
          "nombre": "Incline Dumbbell Press",
          "series": 3,
          "repeticiones": "8-10",
          "descanso_segundos": 90,
          "intensidad": "70-75%"
        },
        {
          "nombre": "Chest-Supported Rows",
          "series": 3,
          "repeticiones": "8-10",
          "descanso_segundos": 90
        },
        {
          "nombre": "Lateral Raises",
          "series": 3,
          "repeticiones": "12-15",
          "descanso_segundos": 60
        }
      ]
    },
    {
      "dia": 2,
      "nombre": "Lower Power",
      "musculos": ["piernas", "glúteos"],
      "ejercicios": [
        {
          "nombre": "Back Squat",
          "series": 4,
          "repeticiones": "6-8",
          "descanso_segundos": 120,
          "intensidad": "80-85%"
        },
        {
          "nombre": "Deadlifts",
          "series": 3,
          "repeticiones": "5-6",
          "descanso_segundos": 150,
          "intensidad": "85-90%"
        },
        {
          "nombre": "Leg Press",
          "series": 3,
          "repeticiones": "8-10",
          "descanso_segundos": 90
        },
        {
          "nombre": "Leg Curls",
          "series": 3,
          "repeticiones": "10-12",
          "descanso_segundos": 60
        }
      ]
    },
    {
      "dia": 3,
      "nombre": "Upper Hypertrophy",
      "musculos": ["pecho", "espalda", "biceps", "triceps"],
      "ejercicios": [
        {
          "nombre": "Dumbbell Bench Press",
          "series": 3,
          "repeticiones": "8-10"
        },
        {
          "nombre": "Pull-ups / Lat Pulldown",
          "series": 3,
          "repeticiones": "8-10"
        },
        {
          "nombre": "Dumbbell Flyes",
          "series": 3,
          "repeticiones": "10-12"
        },
        {
          "nombre": "Barbell Rows",
          "series": 3,
          "repeticiones": "8-10"
        },
        {
          "nombre": "Dips",
          "series": 3,
          "repeticiones": "8-10"
        },
        {
          "nombre": "Barbell Curls",
          "series": 2,
          "repeticiones": "8-10"
        }
      ]
    },
    {
      "dia": 4,
      "nombre": "Lower Hypertrophy",
      "musculos": ["piernas"],
      "ejercicios": [
        {
          "nombre": "Front Squat",
          "series": 3,
          "repeticiones": "8-10"
        },
        {
          "nombre": "Romanian Deadlifts",
          "series": 3,
          "repeticiones": "8-10"
        },
        {
          "nombre": "Leg Extensions",
          "series": 3,
          "repeticiones": "10-12"
        },
        {
          "nombre": "Walking Lunges",
          "series": 3,
          "repeticiones": "10-12"
        },
        {
          "nombre": "Seated Leg Curls",
          "series": 3,
          "repeticiones": "10-12"
        },
        {
          "nombre": "Calf Raises",
          "series": 3,
          "repeticiones": "15-20"
        }
      ]
    }
  ],
  "notas": "Esta rutina está diseñada para principiantes-intermedios. Aumenta el peso cuando puedas completar todas las series. Descansa 48-72 horas entre sesiones del mismo tipo.",
  "generado_por": "Ronnie AI Coach",
  "fecha": "2026-06-21"
}`,

    beginner: `{
  "nombre": "Rutina Principiante 3 Días",
  "nivel": "Principiante",
  "dias_semana": 3,
  "descripcion": "Programa completo de cuerpo para construcción de base sólida",
  "ejercicios_principales": [
    {"nombre": "Squats", "series": 3, "reps": "8-10"},
    {"nombre": "Bench Press", "series": 3, "reps": "8-10"},
    {"nombre": "Rows", "series": 3, "reps": "8-10"}
  ]
}`,
  },
};

/**
 * Generar respuesta aleatoria del mock
 */
export function getMockGroqResponse(
  type: 'coaching' | 'routine' = 'coaching'
): string {
  if (type === 'routine') {
    return groqMockResponses.routine.hypertrophy;
  }

  const coachingKeys = Object.keys(groqMockResponses.coaching) as Array<
    keyof typeof groqMockResponses.coaching
  >;
  const randomKey =
    coachingKeys[Math.floor(Math.random() * coachingKeys.length)];
  return groqMockResponses.coaching[randomKey];
}

/**
 * Helper para mock de respuestas Groq
 */
export class GroqMock {
  static chatResponse(message: string): string {
    if (message.toLowerCase().includes('rutina')) {
      return groqMockResponses.routine.hypertrophy;
    }
    if (message.toLowerCase().includes('principiante')) {
      return groqMockResponses.routine.beginner;
    }
    return getMockGroqResponse('coaching');
  }

  static generateRoutine(questionnaire: string): string {
    if (questionnaire.includes('principiante')) {
      return groqMockResponses.routine.beginner;
    }
    return groqMockResponses.routine.hypertrophy;
  }
}
