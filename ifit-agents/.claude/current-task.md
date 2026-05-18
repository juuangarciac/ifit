# Task: Añadir opción "Prefiero no responder" en todas las preguntas del cuestionario

## Request
Revisar el fichero data.sql de Ifit para añadir la opción 'Prefiero no responder' en todas las preguntas del cuestionario. Esta nueva opción debe persistirse correctamente en la base de datos, gestionarse en la capa de servicio de Ifit, y los microservicios de Ronnie deben interpretarla adecuadamente en los system prompts de cada coach, ofreciendo alternativas o ignorando ese dato sin romper la generación de rutinas.

## Affected services
- **Ifit** — data.sql (inserción de nuevas question_option), RoutineService.buildRoutinePrompt (marcar respuestas omitidas), QuestionnaireService (validación ya existente compatible)
- **Ronnie** — RonnieRoutineService, SerenaRoutineService, KaelRoutineService, EliudRoutineService, Master (system prompts deben interpretar "[No respondida]" y generar valores por defecto)

## Change type
REFACTOR (enriquecimiento de datos + ajuste de system prompts)

## Agent assignments

### Agent 1 — Context analyst
Analizar:
- `ifit/src/main/resources/data.sql`: árbol completo de preguntas (Q1-Q65) y opciones (IDs 1-215) para identificar el display_order máximo y el next_question_id correcto de cada pregunta donde añadir la nueva opción.
- `ifit/src/main/java/.../questionnaire/service/QuestionnaireService.java`: método `answerQuestion` — validar que `requiresTextInput=false` en la nueva opción no rompe la validación existente.
- `ifit/src/main/java/.../training/service/RoutineService.java`: método `buildRoutinePrompt` — cómo se serializa `answer.selectedOption()` al prompt.
- `ronnie/src/main/java/.../coach/ronnie/RonnieRoutineService.java`, `SerenaRoutineService.java`, `KaelRoutineService.java`, `EliudRoutineService.java`, `master/Master.java`: sección REGLAS DE CALIDAD en cada @SystemMessage.

### Agent 2A — Gateway guardian
SKIP — no hay cambios en rutas ApiGateway ni en predicados/filtros.

### Agent 2B — Domain guardian
Verificar:
- `QuestionOption`: los campos `requiresTextInput=FALSE`, `textInputPrompt=NULL`, `textInputPlaceholder=NULL` son correctos para la nueva opción.
- `QuestionnaireService.answerQuestion`: la validación `if (selectedOption.getRequiresTextInput() && additionalText == null)` es safe con la nueva opción (requiresTextInput=false → no falla).
- `RoutineService.buildRoutinePrompt`: actualmente escribe `answer.selectedOption()` directamente. Añadir lógica para que si el texto de la opción es "Prefiero no responder", se serialice como `[No respondida]` en lugar del texto literal, para que el sistema de prompts lo interprete correctamente.
- IDs de nuevas opciones: rango 216-280 (65 preguntas × 1 opción cada una). Confirmar que no colisionan con registros existentes.
- `display_order` de la nueva opción en cada pregunta: debe ser `(max display_order existente + 1)`.
- `next_question_id` de la nueva opción: mismo que la siguiente pregunta en el árbol de decisión de esa pregunta; NULL en preguntas finales.

### Agent 2C — AI guardian
Verificar en cada @AiService de Ronnie:
- `RonnieRoutineService.java` @SystemMessage — añadir regla: si una respuesta es `[No respondida]`, usar un valor por defecto razonable según el contexto del coach y no romper la estructura de la rutina.
- `SerenaRoutineService.java` @SystemMessage — ídem.
- `KaelRoutineService.java` @SystemMessage — ídem.
- `EliudRoutineService.java` @SystemMessage — ídem.
- `Master.java` @SystemMessage — ídem (regla en REGLAS DE CALIDAD).
- Confirmar que `wiringMode=EXPLICIT` y beans declarados no se ven afectados por el cambio.
- Confirmar que el parámetro `@V("questionnaireData")` sigue siendo la única entrada, y la nueva marca `[No respondida]` llega a través de él sin cambios estructurales en la interfaz.

### Agent 3 — Quality reviewer
- Verificar que todos los INSERT en data.sql usan `ON DUPLICATE KEY UPDATE` para ser idempotentes.
- Verificar que los IDs 216-280 no colisionan con ningún ID existente (rango actual termina en 215).
- Verificar que `next_question_id` de la nueva opción en cada pregunta final (Q13, Q26, Q39, Q52, Q65) es NULL.
- Verificar que `RoutineService.buildRoutinePrompt` tiene null-guard para `answer.selectedOption()` antes de comparar el texto.
- Verificar que cada @SystemMessage modificado mantiene coherencia de formato (delimitadores ═══, secciones con nombres en mayúscula).
- Verificar que no hay texto adicional en inglés en las nuevas reglas de los system prompts.
- Javadoc: no se crean nuevos métodos públicos, no se requiere Javadoc adicional.

### Agent 4 — Implementor
Archivos a crear/modificar en orden:

1. `ifit/src/main/resources/data.sql`
   - Añadir bloque final: 65 INSERT INTO question_option para la opción "Prefiero no responder" en cada pregunta (IDs 216-280), con `requires_text_input=FALSE`, `text_input_prompt=NULL`, `text_input_placeholder=NULL`, `next_question_id` correspondiente y `display_order` = max + 1 por pregunta.
   - Convención: `ON DUPLICATE KEY UPDATE` con todos los campos.

2. `ifit/src/main/java/.../training/service/RoutineService.java`
   - En `buildRoutinePrompt`, dentro del bucle `for (AnswerDto answer : summary.getAnswers())`, si `answer.selectedOption()` es igual a `"Prefiero no responder"`, serializar como `"[No respondida]"` en lugar del texto literal.

3. `ronnie/src/main/java/.../coach/ronnie/RonnieRoutineService.java`
   - Añadir en @SystemMessage, dentro de la sección REGLAS DE CALIDAD, una nueva regla: "Si un dato del cuestionario aparece como [No respondida], usa un valor por defecto razonable para ese parámetro y no lo menciones explícitamente en el mensaje motivador."

4. `ronnie/src/main/java/.../coach/serena/SerenaRoutineService.java`
   - Ídem en @SystemMessage, sección REGLAS DE CALIDAD.

5. `ronnie/src/main/java/.../coach/kael/KaelRoutineService.java`
   - Ídem en @SystemMessage, sección REGLAS DE CALIDAD.

6. `ronnie/src/main/java/.../coach/eliud/EliudRoutineService.java`
   - Ídem en @SystemMessage, sección REGLAS DE CALIDAD.

7. `ronnie/src/main/java/.../coach/master/Master.java`
   - Ídem en @SystemMessage, sección REGLAS DE CALIDAD.

### Agent 5 — Final verifier
- Verificar que todos los IDs de las nuevas opciones (216-280) existen en data.sql y no colisionan con IDs previos.
- Verificar que `next_question_id` de cada nueva opción apunta a la misma pregunta siguiente que las otras opciones de ese grupo.
- Verificar que `RoutineService.buildRoutinePrompt` usa comparación de cadena exacta para "Prefiero no responder" y tiene null-check previo.
- Verificar que los cinco @AiService de Ronnie tienen exactamente la misma nueva regla formulada de forma consistente.
- Verificar que el flujo completo (usuario responde "Prefiero no responder" → se almacena en user_answer → getResponseSummary → buildRoutinePrompt serializa [No respondida] → coach genera rutina con defaults) es coherente de extremo a extremo.

## Acceptance criteria
1. En data.sql existen exactamente 65 nuevas opciones "Prefiero no responder" (IDs 216-280), una por cada pregunta Q1-Q65, cada una con `requires_text_input=FALSE` y el `next_question_id` correcto.
2. El método `RoutineService.buildRoutinePrompt` serializa la respuesta "Prefiero no responder" como `[No respondida]` en el prompt enviado al coach, sin romper el formato existente.
3. Los cinco @AiService de Ronnie (Ronnie, Serena, Kael, Eliud, Master) contienen la regla explícita sobre cómo tratar `[No respondida]`.
4. El flujo de cuestionario existente sigue funcionando correctamente para usuarios que NO seleccionan "Prefiero no responder".
5. Los INSERT en data.sql son idempotentes (ON DUPLICATE KEY UPDATE) y no rompen el script de inicialización.

## Risk areas
- **Colisión de IDs**: si en el futuro se añaden preguntas u opciones al data.sql antes de este rango, puede haber conflicto. Mitigación: el bloque nuevo está agrupado al final con comentario explicativo.
- **next_question_id incorrecto en Q9**: la opción 34 ("Actualmente entreno de forma regular") lleva a Q11 en vez de Q10. La nueva opción "Prefiero no responder" de Q9 debe ir a Q10 (ruta conservadora).
- **Prompt injection**: el texto "[No respondida]" podría ser confundido con instrucciones si el LLM no tiene el contexto correcto. Mitigación: la regla en el system prompt explica exactamente qué hacer con esta marca.
- **Preguntas con única opción (NUMERIC)**: Q6, Q7, Q19, Q20, Q32, Q33, Q45, Q46, Q58, Q59 tienen solo una opción (requiresTextInput=true). Añadir "Prefiero no responder" como segunda opción es una adición no destructiva; el frontend ya soporta múltiples opciones en preguntas NUMERIC.
- **Consistencia entre coaches**: si se actualiza solo algunos coaches, el comportamiento será inconsistente. Actualizar los cinco @AiService en el mismo commit.
