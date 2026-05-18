# Review report

## Agent 2A — Gateway review

Agent 2A: not applicable for this task.

---

## Agent 2B — Domain review

### Status: APPROVED (con observaciones incorporadas en el plan de implementación)

### Checks
- [x] Constructor injection usado en `RoutineService` (7 dependencias, todas por constructor)
- [x] `@Transactional(readOnly = true)` a nivel de clase en `RoutineService`
- [x] Métodos de escritura con `@Transactional` cuando corresponde
- [x] `generateRoutine` tiene null-guards para `userId` y `responseId`
- [x] No se crean nuevas entidades — N/A: `{Entity}NotFoundException` no aplica
- [x] No se crean nuevas entidades — N/A: tabla/ID/hashCode/equals no aplican
- [x] Logger presente en `RoutineService` (línea 60: `LoggerFactory.getLogger(RoutineService.class)`)
- [x] `buildRoutinePrompt` es privado — Javadoc no requerido
- [x] No hay lógica de negocio en el controlador del cuestionario (la conversión ocurre en servicio)

### Issues found

**[PREVENTIVO] RoutineService.buildRoutinePrompt — null-check en selectedOption**

Clase: `com.uca.juangarcia.ifit.modules.training.service.RoutineService`  
Método: `buildRoutinePrompt` (línea ~421-429)

El método actual itera sobre `summary.getAnswers()` y escribe `answer.selectedOption()`. El DTO `AnswerDto` es un record, y `toAnswerDto` en `QuestionnaireService` (línea 626) ya protege con `answer.getSelectedOption() != null ? answer.getSelectedOption().getText() : null`, por lo que `selectedOption()` puede ser `null`.

La comparación para detectar "Prefiero no responder" debe usar `"Prefiero no responder".equals(answer.selectedOption())` (orden invertido, null-safe) en lugar de `answer.selectedOption().equals(...)` para evitar NPE.

**[PREVENTIVO] data.sql — idempotencia de los 65 nuevos INSERT**

Todos los INSERT de opciones nuevas (IDs 216-280) deben seguir el mismo patrón que el script existente:
```sql
ON DUPLICATE KEY UPDATE
    text = VALUES(text),
    next_question_id = VALUES(next_question_id),
    display_order = VALUES(display_order),
    requires_text_input = VALUES(requires_text_input),
    text_input_prompt = VALUES(text_input_prompt),
    text_input_placeholder = VALUES(text_input_placeholder);
```

**[PREVENTIVO] Q9 — next_question_id de "Prefiero no responder"**

Las opciones de Q9 se dividen: 4 de ellas llevan a Q10, y la opción 34 ("Actualmente entreno de forma regular") lleva a Q11. La nueva opción debe llevar a Q10 (ruta conservadora por defecto, sin asumir experiencia previa activa).

---

## Agent 2C — AI review

### Status: APPROVED (reglas de "[No respondida]" a añadir en implementación)

### Checks
- [x] `wiringMode = AiServiceWiringMode.EXPLICIT` en los 5 @AiService de rutina: `RonnieRoutineService`, `SerenaRoutineService`, `KaelRoutineService`, `EliudRoutineService`, `Master`
- [x] Todos los RoutineService usan `groqJsonChatLanguageModel` (salida JSON estructurada)
- [x] `chatMemoryProvider = "messageWindowChatMemory"` declarado en todos
- [x] `contentRetriever` solo en `RonnieService` (chat libre con RAG) — los RoutineService no lo tienen correctamente
- [x] `ChatContext` gestionado en `try/finally` en `RonnieController` (verificado); se asume correcto en los demás por convención del proyecto
- [x] System prompts siguen el formato de bloques con `════════════════════════════════════════`
- [x] Todo el contenido en español
- [x] Catálogo inyectado vía `@Value("classpath:langchain4j/assistants-personality/exercises.txt")` en el controlador y pasado como `@V("exerciseCatalog")`
- [x] No se crea ningún coach nuevo — checklist de 7 pasos no aplica
- [x] `ChatContext.set(userId, "ronnie")` — nombre del coach en minúscula (verificado en `RonnieController`)

### Issues found

**[REQUERIDO] Los cinco @SystemMessage no tienen regla para "[No respondida]"**

Estado actual: ninguno de los cinco system prompts maneja el caso de datos omitidos del cuestionario.

Regla a añadir en la sección `REGLAS DE CONSTRUCCIÓN` de cada coach (y `REGLAS DE CALIDAD` en `Master.java`):

```
5. Si un dato del cuestionario aparece con el valor "[No respondida]", ignora ese
   parámetro y usa un valor por defecto razonable según el contexto del plan.
   No menciones ni comentes la ausencia de ese dato en el mensaje motivador ni
   en la descripción de la rutina.
```

El número de la regla debe seguir la numeración existente en cada service:
- `RonnieRoutineService`: actualmente 1-5 → nueva regla = 6
- `SerenaRoutineService`: actualmente 1-4 → nueva regla = 5
- `KaelRoutineService`: actualmente 1-5 → nueva regla = 6
- `EliudRoutineService`: actualmente 1-4 → nueva regla = 5
- `Master.java` (sección REGLAS DE CALIDAD): actualmente 1-4 → nueva regla = 5

La marca exacta que llegará en `{questionnaireData}` es `[No respondida]` (con corchetes, sin comillas), producida por `RoutineService.buildRoutinePrompt`.

**[CONFIRMADO] Contrato @V no se rompe**

El cambio es puramente en el contenido de la cadena que llega a `@V("questionnaireData")`. No se modifica la firma del método `generateRoutine` en ningún @AiService. No se requieren cambios en los beans de LangChain4j.
