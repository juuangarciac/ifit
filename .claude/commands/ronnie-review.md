# Revisión de Prompts de IA — Microservicio Ronnie

**Fecha de revisión:** 2026-05-11
**Archivos analizados:**
- `ronnie/src/main/java/.../coach/master/Master.java`
- `ronnie/src/main/java/.../coach/ronnie/RonnieRoutineService.java`
- `ronnie/src/main/java/.../coach/eliud/EliudRoutineService.java`
- `ronnie/src/main/java/.../coach/kael/KaelRoutineService.java`
- `ronnie/src/main/java/.../coach/serena/SerenaRoutineService.java`
- `ronnie/src/main/resources/langchain4j/assistants-personality/exercises.txt`
- `ronnie/src/main/resources/application.properties`
- `ronnie/src/main/java/.../coach/dto/RoutineResponseDto.java`
- `ifit/src/main/java/.../training/model/CoachType.java`
- `ifit/src/main/java/.../training/service/RoutineService.java`

---

## 1. Arquitectura general del sistema de prompts

El sistema tiene dos capas de prompts:

**Capa 1 — Coach especializado (en Ronnie):** Cada coach (`Master`, `RonnieRoutineService`, `EliudRoutineService`, `KaelRoutineService`, `SerenaRoutineService`) es un `@AiService` LangChain4j con `@SystemMessage` propio. Todos usan `groqJsonChatLanguageModel` (Llama 3.3 70B, temperature=0.3, responseFormat=json_object).

**Capa 2 — Contexto del coach (en ifit):** `CoachType` (enum) define un bloque de texto de especialidad del coach. Los coaches especializados (Ronnie, Eliud, Kael, Serena) tienen sus propios endpoints en el microservicio Ronnie y sus propios `@SystemMessage` completos.

El prompt de usuario final lo construye `RoutineService.buildRoutinePrompt()` en el microservicio ifit, que concatena: perfil del usuario, nivel de catálogo resuelto, nombre del cuestionario y las respuestas Q&A. Este prompt se envía como `{questionnaireData}` al coach.

---

## 2. Fortalezas del sistema actual

### 2.1 Catálogo de ejercicios bien estructurado
El fichero `exercises.txt` es un JSON con 4 secciones de dificultad (warmup, beginner, intermediate, advanced) y stretching. Cada ejercicio incluye `exerciseName`, `sets`, `reps`, `restSeconds`, `notes` y `orderIndex`. Es completo para un MVP: 27 ejercicios de beginner, 27 de intermediate, 26 de advanced, 7 de warmup, 8 de stretching.

### 2.2 Regla de copia literal del nombre de ejercicio
La "REGLA DE ORO" está presente en todos los prompts de los 5 coaches: copiar `exerciseName` carácter por carácter, sin traducir ni abreviar. Esta es una de las instrucciones más importantes y está bien posicionada y resaltada.

### 2.3 Estructura de día obligatoria bien definida
Todos los coaches exigen la misma estructura por día: Calentamiento (2-3 ejercicios de warmup) + Bloque principal (5-8 ejercicios) + Estiramientos (2-3 ejercicios de stretching). Los nombres válidos se listan explícitamente en cada prompt como refuerzo.

### 2.4 Temperature baja para JSON estructurado
`groqJsonChatLanguageModel` usa temperature=0.3, lo que favorece el determinismo y reduce la probabilidad de alucinaciones en los nombres de ejercicios.

### 2.5 Diferenciación de coaches bien ejecutada
Cada coach especializado tiene prohibiciones explícitas y listas de ejercicios prioritarios por nivel. Eliud prohíbe explícitamente ejercicios de hipertrofia pura. Kael prohíbe maquinaria de gimnasio. Serena prohíbe movimientos de alta carga técnica. Esto es correcto y necesario.

### 2.6 Distribución de días coherente
Las tablas de frecuencia (1-2 días → Full Body, 3 días → Push/Pull/Legs, etc.) están en todos los prompts y son coherentes entre sí.

---

## 3. Problemas y riesgos identificados

### 3.1 CRITICO — API key de Groq expuesta en application.properties

**Problema:** `application.properties` contiene la API key de Groq en texto plano:
`groq.api-key=gsk_***` *(key redactada)*

**Impacto:** Si el repositorio es público o el fichero se incluye en el artefacto desplegado, la key queda expuesta. Groq detecta habitualmente las keys hardcodeadas en repositorios y las revoca automáticamente.

**Recomendación:** Mover la key a una variable de entorno (`${GROQ_API_KEY}`) antes del deploy. Verificar si ya fue expuesta en algún commit previo y revocarla/regenerarla.

---

### 3.2 ALTO — Contradicción entre regla de no-repetición y tamaño del catálogo de warmup

**Problema:** La regla "ningún ejercicio puede aparecer en más de dos días distintos" se aplica a toda la rutina. Sin embargo, los ejercicios de warmup son exactamente 7. Si una rutina tiene 6-7 días, el modelo tiene que repetir ejercicios de calentamiento más de dos veces inevitablemente, ya que con 7 ejercicios y la restricción de máximo 2-3 por día, en el mejor caso cubren 2-3 días sin repetición.

Hay una **contradicción estructural** entre la regla de no-repetición en más de 2 días y el número de ejercicios de warmup disponibles para rutinas largas.

**Impacto:** Para rutinas de 5-7 días, la regla es imposible de cumplir en el calentamiento. El modelo ignorará la regla o generará calentamientos idénticos, dependiendo de cómo interprete la prioridad.

**Recomendación:** Modificar la regla en todos los prompts para excluir explícitamente los ejercicios de calentamiento y estiramiento: "La regla de no más de dos días aplica ÚNICAMENTE al bloque principal. Los ejercicios de calentamiento y estiramiento pueden repetirse entre días."

---

### 3.3 ALTO — Ambigüedad del campo orderIndex

**Problema:** El catálogo incluye `orderIndex` en cada ejercicio indicando su posición dentro de la sección del catálogo (posición fija). El prompt del Master instruye: "orderIndex sigue el orden real de aparición en el día (1, 2, 3...)". Hay una ambigüedad fundamental: ¿debe el modelo copiar el `orderIndex` del catálogo (fijo por ejercicio) o asignar uno nuevo secuencial dentro del día generado?

Si copia del catálogo: el "Puente de glúteos" siempre tendrá `orderIndex=4` aunque sea el séptimo ejercicio del día.
Si asigna nuevo: contradice la instrucción de "copia exactamente del catálogo".

**Impacto:** El `orderIndex` en la rutina generada será inconsistente, causando que el frontend muestre ejercicios potencialmente en orden incorrecto.

**Recomendación:** Añadir en todos los prompts: "`orderIndex` debe ser la posición secuencial del ejercicio dentro del día generado (1, 2, 3...), comenzando desde 1. NO copies el orderIndex del catálogo."

---

### 3.4 ALTO — Instrucciones de ROL DEL ENTRENADOR en el Master nunca se activan

**Problema:** El prompt del Master contiene instrucciones extensas sobre "Si el mensaje contiene un bloque 'ROL DEL ENTRENADOR', adóptalo íntegramente." Sin embargo, en el flujo actual, `buildRoutinePrompt()` en `RoutineService` NO inyecta el `CoachType.systemContext` en el prompt que envía al Master. `CoachType.MASTER` tiene `systemContext=null`.

Por lo tanto, el Master siempre actúa como entrenador generalista. El bloque completo de instrucciones sobre los 4 tipos de especialidades (Cardio/running, Calistenia, Musculación, Bienestar) es código muerto en el flujo actual.

**Impacto:** Tokens desperdiciados en el prompt del Master. Si la intención era que el Master pudiera especializarse según el coach seleccionado, esa funcionalidad no funciona.

**Recomendación (opción A):** Si el Master debe ser especializable, modificar `buildRoutinePrompt()` para que cuando `coachType != MASTER`, inyecte el `CoachType.systemContext` al inicio del prompt bajo la cabecera `ROL DEL ENTRENADOR:`.

**Recomendación (opción B):** Si el Master siempre es generalista, eliminar el bloque "ROL DEL ENTRENADOR" de su prompt para reducir tokens y ruido.

---

### 3.5 MEDIO — Catálogo insuficiente para Eliud en nivel Beginner

**Problema:** Eliud prioriza ejercicios cardiovasculares y prohíbe ejercicios de hipertrofia. Para BEGINNER, solo tiene ejercicios compatibles: "Marcha en el sitio", "Caminar en cinta o al aire libre (30 min)", "Bicicleta estática suave (20 min)" como ejercicios cardio puros, más "Zancada estática", "Elevación de talones de pie", "Puente de glúteos" como soporte funcional = 6 ejercicios en total.

El prompt exige un bloque principal de 5 a 8 ejercicios. Con solo 6 ejercicios compatibles, el margen es mínimo y el modelo puede verse forzado a incluir ejercicios no apropiados para completar el bloque.

**Recomendación:** Añadir al catálogo beginner ejercicios aeróbicos de bajo impacto (jumping jacks básicos, step lateral, rodillas al pecho de pie), o reducir el mínimo del bloque principal a 4 ejercicios para el coach Eliud en nivel beginner.

---

### 3.6 MEDIO — "bird-dog" en CoachType.SERENA no existe en el catálogo

**Problema:** `CoachType.SERENA.systemContext` menciona "bird-dog" como ejercicio a incluir en las rutinas. Sin embargo, el ejercicio "bird-dog" no existe en el catálogo `exercises.txt`. Si el modelo procesa ese contexto a través del Master (cuando se implemente el ROL DEL ENTRENADOR), podría generar un ejercicio inexistente en el catálogo.

**Recomendación:** Reemplazar "bird-dog" en `CoachType.SERENA` por un ejercicio que sí exista, por ejemplo "Abducción lateral de cadera tumbado" o "Plancha estática (30s)".

---

### 3.7 MEDIO — Inconsistencia en reps=1 para ejercicios de duración

**Problema:** El catálogo usa `reps=1` para ejercicios que representan duraciones (warmup, stretching, cardio), mientras que usa números reales para ejercicios con repeticiones. El DTO `RoutineExerciseDto` tiene `reps` como `String` para tolerar esto.

La instrucción "copia `reps` exactamente del catálogo" es correcta, pero un usuario que ve `sets=1, reps=1` para "Marcha en el sitio" no sabrá que significa "3-5 minutos" sin leer las notas.

**Recomendación:** Añadir en el prompt una nota de clarificación: "Para ejercicios con reps=1 en el catálogo (ejercicios de duración), la duración está descrita en el campo notes. No modifiques el valor; el frontend lo interpretará."

---

### 3.8 BAJO — `trainingDays` sin instrucción explícita

**Problema:** El campo `trainingDays` en `RoutineResponseDto` no tiene ninguna instrucción explícita en los prompts sobre cómo calcularlo. El modelo debe inferir que debe ser igual al número de elementos en la lista `days`, pero podría poner un valor diferente.

**Recomendación:** Añadir en todos los prompts: "El campo `trainingDays` debe ser exactamente igual al número de días en la lista `days`."

---

### 3.9 BAJO — Memoria de conversación compartida entre chat y generación de rutinas

**Problema:** Los `@AiService` usan `chatMemoryProvider = "messageWindowChatMemory"` con `max-messages=10`. El `memoryId` se genera como el máximo actual + 1 (no atómico). En escenarios de alta concurrencia, dos usuarios podrían obtener el mismo `memoryId`.

**Recomendación:** Para generación de rutinas (stateless por naturaleza), usar un `memoryId` UUID por invocación en lugar del contador incremental.

---

## 4. Coherencia entre CoachType (ifit) y prompts especializados (Ronnie)

| Aspecto | CoachType enum | Prompt especializado | Coherente |
|---|---|---|---|
| Ronnie — ejercicios prioritarios | "press de banca, sentadilla, peso muerto, curl de bíceps" | Lista detallada por nivel | Si |
| Eliud — prohibiciones | No incluye prohibiciones explícitas | Prohíbe press banca, curl, remo, jalón, etc. | El enum es más permisivo |
| Kael — sin maquinaria | "flexiones, fondos, dominadas, sentadillas" | "EVITA jalón en polea, curl femoral en máquina..." | El enum es más permisivo |
| Serena — "bird-dog" | Mencionado en CoachType | No está en el catálogo | Inconsistencia critica |
| Serena — duración 30-45 min | Mencionado en CoachType | No mencionado en el prompt especializado | Gap menor |

---

## 5. Análisis del prompt del Master

El prompt del Master es el más completo y mejor estructurado. Puntos destacados:

**Positivo:**
- Usa separadores visuales con bloques (`════════`) que ayudan al modelo a segmentar las instrucciones.
- La "REGLA DE ORO" está visualmente destacada con `▶`.
- Los nombres válidos de calentamiento y estiramiento se listan explícitamente, reduciendo la probabilidad de alucinaciones.
- La distribución de días está bien especificada con rangos claros.

**A mejorar:**
- El bloque "ROL DEL ENTRENADOR" es código muerto en el flujo actual (ver punto 3.4).
- No hay instrucción sobre el campo `trainingDays` (ver punto 3.8).
- La instrucción de `orderIndex` es ambigua (ver punto 3.3).

---

## 6. Recomendaciones ordenadas por prioridad

### Antes del deploy (urgente)

1. **Revocar y proteger la API key de Groq** — Mover a variable de entorno antes de cualquier deploy. Verificar historial de git por exposiciones previas.

2. **Corregir la regla de no-repetición para warmup** — En todos los prompts (5 ficheros): aclarar que la regla aplica solo al bloque principal, no al calentamiento ni estiramientos.

3. **Aclarar el orderIndex** — En todos los prompts: "El `orderIndex` de cada ejercicio debe ser su posición secuencial dentro del día (1, 2, 3...). No copies el orderIndex del catálogo."

4. **Resolver el ROL DEL ENTRENADOR** — Decidir e implementar: o el Master recibe el contexto del coach, o se eliminan esas instrucciones del prompt.

5. **Corregir "bird-dog" en CoachType.SERENA** — Reemplazar por un ejercicio existente en el catálogo.

### Mejoras de calidad (post-deploy inmediato)

6. **Añadir instrucción sobre `trainingDays`** — En todos los prompts.
7. **Ampliar catálogo beginner para Eliud** — Al menos 3-4 ejercicios aeróbicos adicionales.
8. **Añadir nota sobre reps=1 en ejercicios de duración** — Aclaración en el prompt.

### Mejoras futuras

9. Añadir campo `unit` al catálogo (`REPS` vs `DURATION_SECONDS`).
10. Separar memoryId de rutinas y chat usando UUIDs.
11. Añadir validación post-generación que verifique que todos los `exerciseName` existen en el catálogo.

---

## 7. Resumen ejecutivo

El sistema de prompts del microservicio Ronnie es sólido para un MVP. La arquitectura de coaches especializados con catálogo centralizado es correcta, los prompts están bien estructurados y la temperatura baja (0.3) favorece la consistencia. Los riesgos principales antes del deploy son:

1. **API key hardcodeada** — riesgo de seguridad inmediato.
2. **Contradicción en regla de no-repetición** — generará comportamiento errático en rutinas de 5-7 días.
3. **Ambigüedad del orderIndex** — causará orden incorrecto de ejercicios en el frontend.
4. **ROL DEL ENTRENADOR nunca se activa** — funcionalidad prevista que no funciona.
5. **"bird-dog" inexistente en catálogo** — riesgo de alucinación si se activa el flujo Master+Serena.

Los problemas 2, 3 y 5 son correcciones de texto en los prompts que se pueden resolver en minutos. El problema 1 requiere rotación de credenciales. El problema 4 requiere una decisión de arquitectura.
