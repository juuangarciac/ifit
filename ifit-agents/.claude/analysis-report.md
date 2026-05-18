# Analysis report

## Affected service(s)

### Ifit (`com.uca.juangarcia.ifit`)
- `ifit/src/main/resources/data.sql` — Añadir 65 opciones "Prefiero no responder" (IDs 216-280), una por cada pregunta Q1-Q65.
- `com.uca.juangarcia.ifit.modules.training.service.RoutineService` — Método `buildRoutinePrompt` (línea ~421): convertir la cadena "Prefiero no responder" a "[No respondida]" antes de serializar al prompt.

### Ronnie (`com.ifit.ronnie`)
- `com.ifit.ronnie.modules.coach.ronnie.RonnieRoutineService` — @SystemMessage, sección REGLAS DE CONSTRUCCIÓN.
- `com.ifit.ronnie.modules.coach.serena.SerenaRoutineService` — @SystemMessage, sección REGLAS DE CONSTRUCCIÓN.
- `com.ifit.ronnie.modules.coach.kael.KaelRoutineService` — @SystemMessage, sección REGLAS DE CONSTRUCCIÓN.
- `com.ifit.ronnie.modules.coach.eliud.EliudRoutineService` — @SystemMessage, sección REGLAS DE CONSTRUCCIÓN.
- `com.ifit.ronnie.modules.coach.master.Master` — @SystemMessage, sección REGLAS DE CALIDAD.

### ApiGateway
No afectado. Sin cambios en routes, predicates, filters ni JWT config.

---

## Dependency graph (creation order)

No se crean nuevas entidades, repositorios, DTOs, mappers ni excepciones. El cambio es transversal a datos y lógica de prompt.

1. **data.sql** (datos) — Nuevas filas en `question_option` para cada Q1-Q65. Sin DDL.
2. **RoutineService** (servicio Ifit) — Ajuste en `buildRoutinePrompt` para marcar respuestas omitidas como `[No respondida]`.
3. **RonnieRoutineService, SerenaRoutineService, KaelRoutineService, EliudRoutineService, Master** (Ronnie) — Nueva regla en @SystemMessage para manejar `[No respondida]`.

---

## Cross-service impacts

### Flujo actual
```
Ifit: RoutineService.generateRoutine
  → QuestionnaireService.getResponseSummary(responseId) → List<AnswerDto>
  → buildRoutinePrompt(user, summary, note) → String prompt
  → IFitAIClient.generateRoutine(memoryId, prompt, keycloakId, coachType)
    → HTTP call → Ronnie (coach endpoint)
      → [Coach]RoutineService.generateRoutine(@V("questionnaireData") = prompt)
```

### Impacto del cambio
- `AnswerDto.selectedOption()` (record field) puede contener "Prefiero no responder" → `buildRoutinePrompt` debe interceptarlo y emitir "[No respondida]".
- El coach recibe `{questionnaireData}` con entradas como:
  ```
  - ¿Cuánto pesas? (kg)
    Respuesta: [No respondida]
  ```
  Los @SystemMessage actualizados deben indicar al LLM cómo tratar esta marca.
- No hay cambios en el contrato HTTP entre Ifit y Ronnie (mismo endpoint, mismo DTO, mismo campo `questionnaireData`).
- No hay nuevas rutas en ApiGateway.

---

## Risk flags

- [ ] **R1 — Null-check en `answer.selectedOption()`**: `toAnswerDto` en `QuestionnaireService` (línea 626) ya maneja el caso `answer.getSelectedOption() != null ? answer.getSelectedOption().getText() : null`. Si el usuario seleccionara una opción sin texto (imposible con el schema actual, pero defensivo), `answer.selectedOption()` sería `null`. La comparación en `buildRoutinePrompt` debe tener null-check previo a la llamada a `.equals(...)`.

- [ ] **R2 — Q9 next_question_id ambiguo**: la opción 34 ("Actualmente entreno de forma regular") lleva a Q11, mientras las demás opciones de Q9 llevan a Q10. La nueva opción "Prefiero no responder" de Q9 debe llevar a Q10 (ruta por defecto conservadora), no a Q11.

- [ ] **R3 — NUMERIC questions con única opción**: Q6, Q7, Q19, Q20, Q32, Q33, Q45, Q46, Q58, Q59 tienen actualmente exactamente 1 opción (`requiresTextInput=TRUE`). Añadir "Prefiero no responder" con `requiresTextInput=FALSE` como segunda opción es compatible con `answerQuestion`: la validación solo rechaza `additionalText=null` cuando `requiresTextInput=TRUE`, que sigue siendo correcto para la opción de peso/altura.

- [ ] **R4 — Idempotencia data.sql**: los nuevos INSERT deben usar `ON DUPLICATE KEY UPDATE` para que el script sea re-ejecutable. Si los IDs 216-280 ya existen (por una ejecución previa), el script debe actualizar los valores, no fallar.

- [ ] **R5 — Prompt injection**: la cadena "[No respondida]" podría confundirse con sintaxis de plantilla si el LLM tiene un parser especial. Sin embargo, dado el formato plano del `questionnaireData` (sin marcadores de Mustache ni corchetes con nombre de variable), el riesgo es mínimo. Las reglas en @SystemMessage contextualizan explícitamente la marca.

- [ ] **R6 — Consistencia entre coaches**: si los cinco @AiService no reciben exactamente la misma nueva regla con la misma marca "[No respondida]", el comportamiento será inconsistente según el coach elegido. Todos deben actualizarse en el mismo commit.

- [ ] **R7 — `AnswerDto` es un record Java 16+**: los campos son inmutables. No se puede modificar `selectedOption()` post-construcción. La conversión debe hacerse en `buildRoutinePrompt` en `RoutineService`, no en `QuestionnaireService.toAnswerDto`, para no alterar el contrato del DTO.

---

## Recommended agent focus

- **Agent 2A**: SKIP — sin impacto en ApiGateway.
- **Agent 2B**: Verificar los 65 INSERT (IDs 216-280), `display_order` por pregunta, `next_question_id` correcto (especial atención a Q9), null-guard en `buildRoutinePrompt`, y que `AnswerDto` (record) no se modifica.
- **Agent 2C**: Verificar que la nueva regla en cada @SystemMessage usa exactamente la marca `[No respondida]`, está en la sección REGLAS DE CONSTRUCCIÓN (o REGLAS DE CALIDAD en Master), y no altera el `wiringMode=EXPLICIT` ni los beans declarados.
