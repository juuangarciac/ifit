# Quality gate

## Overall status: APPROVED

## Guardian summary
| Agent | Status |
|---|---|
| 2A Gateway | SKIP — sin impacto en ApiGateway |
| 2B Domain  | APPROVED — sin issues bloqueantes |
| 2C AI      | SKIP — sin cambios en Ronnie |

## Test coverage

No existen tests previos para `RoutineService` en el proyecto. No se crean nuevos métodos
públicos; solo se añade un método privado y se modifican tres métodos existentes.

Tests recomendados (deuda técnica, no bloqueantes para esta implementación):
- `RoutineServiceTest` — `createRoutine` con rutina activa previa → resultado: exactamente 1 activa.
- `RoutineServiceTest` — `toggleRoutineActive(id, true)` con otra activa → resultado: 1 activa.
- `RoutineServiceTest` — `updateRoutine(id, {isActive:true})` con otra activa → resultado: 1 activa.
- `RoutineServiceTest` — `toggleRoutineActive(id, false)` → helper NO se llama (verificar sin side-effects).

## Defensive programming issues

Ninguno. El helper privado incluye null-guard explícito (`if (userId == null) throw
IllegalArgumentException`). La lista devuelta por `findByUserIdAndIsActive` es never-null
(JPA devuelve lista vacía, no null), por lo que el `!active.isEmpty()` es seguro.

## API contract issues

Ninguno. No se crean ni modifican endpoints. El contrato HTTP permanece intacto.

## Security issues

Ninguno. No se añaden credenciales, tokens ni datos sensibles.

## Decision

Proceeding to Agent 4 — Implementor

## Agent 4 — Implementation complete
Files generated:
- `ifit/src/main/java/com/uca/juangarcia/ifit/modules/training/service/RoutineService.java`

Cambios aplicados:
1. Nuevo método privado `deactivatePreviousActiveRoutine(Long userId)` — con null-guard,
   llama a `findByUserIdAndIsActive` + `saveAll`.
2. `createRoutine` — llamada al helper antes de `routine.setActive(true)`.
3. `toggleRoutineActive` — llamada condicional al helper solo cuando `isActive == true`.
4. `updateRoutine` — llamada condicional al helper dentro del bloque `isActive != null`,
   solo cuando `updateDto.getIsActive() == true`.

Proceeding to Agent 5 — Final Verifier.
