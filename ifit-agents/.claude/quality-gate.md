# Quality gate

## Overall status: APPROVED

## Guardian summary
| Agent | Status |
|---|---|
| 2A Gateway | SKIP — sin impacto en ApiGateway |
| 2B Domain  | APPROVED con observaciones preventivas incorporadas en el plan |
| 2C AI      | APPROVED con reglas de [No respondida] a añadir en implementación |

## Test coverage

No se crean nuevos métodos públicos en servicios ni controladores. El cambio en `RoutineService.buildRoutinePrompt` es una modificación interna del método privado existente.

Tests recomendados (no bloqueantes para esta implementación):
- `RoutineServiceTest` — añadir caso: cuando `answer.selectedOption()` es "Prefiero no responder", el prompt resultante contiene "[No respondida]" y no "Prefiero no responder".
- `RoutineServiceTest` — añadir caso: cuando `answer.selectedOption()` es `null`, el prompt no lanza NPE.

Dado que no hay tests unitarios previos para `buildRoutinePrompt` en el repositorio actual, estos casos son mejoras recomendadas, no bloqueantes.

## Defensive programming issues

Un único punto preventivo ya identificado y cubierto en el plan de implementación:

- `RoutineService.buildRoutinePrompt` línea ~424: la comparación debe usar `"Prefiero no responder".equals(answer.selectedOption())` para ser null-safe. El plan de implementación incluye este null-guard explícitamente.

Sin otros issues detectados.

## API contract issues

Ninguno. No se crean ni modifican endpoints. El contrato HTTP entre Ifit y Ronnie permanece intacto.

## Security issues

Ninguno:
- Los nuevos datos en `question_option` no contienen información sensible.
- La marca "[No respondida]" que llega al LLM no expone datos del usuario.
- No se añaden credenciales ni secrets.
- `buildRoutinePrompt` es un método privado que ya existía; el logger existente cubre la trazabilidad.

## Decision

Proceeding to Agent 4 — Implementor

## Agent 4 — Implementation complete
Files generated:
- `ifit/src/main/resources/data.sql` — Sección 9 añadida: 65 opciones IDs 216-280
- `ifit/src/main/java/.../training/service/RoutineService.java` — buildRoutinePrompt actualizado
- `ronnie/src/main/java/.../coach/ronnie/RonnieRoutineService.java` — regla 6 añadida
- `ronnie/src/main/java/.../coach/serena/SerenaRoutineService.java` — regla 5 añadida
- `ronnie/src/main/java/.../coach/kael/KaelRoutineService.java` — regla 6 añadida
- `ronnie/src/main/java/.../coach/eliud/EliudRoutineService.java` — regla 5 añadida
- `ronnie/src/main/java/.../coach/master/Master.java` — regla 5 añadida

Proceeding to Agent 5 — Final Verifier.

### Resumen de cambios a implementar
1. **data.sql** — 65 nuevas opciones "Prefiero no responder" (IDs 216-280), una por pregunta Q1-Q65.
2. **RoutineService.buildRoutinePrompt** — null-safe: si `selectedOption` es "Prefiero no responder" → serializar como "[No respondida]".
3. **RonnieRoutineService** — añadir regla 6 en REGLAS DE CONSTRUCCIÓN.
4. **SerenaRoutineService** — añadir regla 5 en REGLAS DE CONSTRUCCIÓN.
5. **KaelRoutineService** — añadir regla 6 en REGLAS DE CONSTRUCCIÓN.
6. **EliudRoutineService** — añadir regla 5 en REGLAS DE CONSTRUCCIÓN.
7. **Master.java** — añadir regla 5 en REGLAS DE CALIDAD.
