# Task: Arreglar inconsistencias en la gestión de rutinas activas (microservicio Ifit)

## Request
La constraint de negocio "un usuario solo puede tener una rutina activa en un instante dado"
no se respeta en tres métodos de `RoutineService`. Al crear, activar mediante toggle o actualizar
una rutina con `isActive=true`, el sistema nunca desactiva las rutinas activas previas del usuario,
lo que permite que existan múltiples rutinas activas simultáneamente.

El fix consiste en extraer un método privado `deactivatePreviousActiveRoutine(Long userId)` e
invocarlo en los tres métodos afectados antes de activar cualquier rutina. No se añade complejidad
extra: se reutilizan los métodos de repositorio ya existentes (`findByUserIdAndIsActive` + `saveAll`).

## Affected services
- **Ifit** — `RoutineService.java` (único archivo a modificar)
- **ApiGateway** — sin cambios
- **Ronnie** — sin cambios

## Change type
BUG FIX (lógica de negocio)

## Agent assignments

### Agent 1 — Context analyst
Analizar en `ifit/src/main/java/.../training/service/RoutineService.java`:
- `createRoutine` (líneas 96-132): actualmente hace `routine.setActive(true)` sin desactivar activas previas.
- `toggleRoutineActive` (líneas 292-303): activa la rutina target sin tocar las demás activas del mismo usuario.
- `updateRoutine` (líneas 259-261): al procesar `updateDto.getIsActive() == true`, no desactiva otras rutinas.
- `deactivatePreviousActiveRoutine` (método privado a crear): debe llamar a
  `routineRepository.findByUserIdAndIsActive(userId, true)` y hacer `saveAll` con `setActive(false)`.
- Confirmar que `RoutineRepository` ya expone `findByUserIdAndIsActive` y `saveAll` heredado de `JpaRepository`.
- Verificar que los tres métodos afectados ya tienen `@Transactional`, garantizando atomicidad del fix.

### Agent 2A — Gateway guardian
SKIP — no hay cambios en rutas ApiGateway ni en predicados/filtros.

### Agent 2B — Domain guardian
Verificar en `RoutineService.java`:
- El nuevo método privado `deactivatePreviousActiveRoutine(Long userId)` no necesita `@Transactional`
  propio porque siempre se llama desde métodos que ya son `@Transactional`.
- En `createRoutine`: la llamada debe hacerse ANTES de `routine.setActive(true)` y ANTES del
  `routineRepository.save(routine)`, usando el `userId` de `requestDto.getUserId()`.
- En `toggleRoutineActive`: la llamada solo debe hacerse cuando `isActive == true` (no al desactivar).
  El `userId` se obtiene de `routine.getUser().getId()` tras el `findById`.
- En `updateRoutine`: la llamada solo debe hacerse cuando `updateDto.getIsActive() != null &&
  updateDto.getIsActive() == true`. El `userId` se obtiene de `routine.getUser().getId()`.
- Confirmar que el helper tiene null-guard: `if (userId == null) throw new IllegalArgumentException(...)`.
- Confirmar que si no hay rutinas activas previas, `findByUserIdAndIsActive` devuelve lista vacía y
  `saveAll` sobre lista vacía es no-op (sin efectos secundarios).
- El acceso a `routine.getUser().getId()` en `toggleRoutineActive` puede disparar lazy loading;
  verificar que el método carga el usuario antes de que la transacción cierre, o usar
  `routineRepository.findByIdWithDaysAndExercises` en su lugar si fuera necesario.

### Agent 2C — AI guardian
SKIP — no hay cambios en Ronnie ni en @AiService.

### Agent 3 — Quality reviewer
- Verificar que los tres métodos afectados siguen respetando `@Transactional` (ya existente).
- Verificar que el helper privado no tiene Javadoc (es privado; las convenciones del proyecto no
  lo requieren en métodos privados).
- Verificar que no se introduce ninguna llamada extra al repositorio cuando `isActive == false`
  (no tiene sentido desactivar activas cuando lo que se hace es desactivar la rutina target).
- Verificar que el orden de operaciones en `createRoutine` es: deactivate previas → crear entidad →
  save → asociar días → save final. No alterar el flujo de días.
- Verificar que `updateRoutine` mantiene el patrón de actualización parcial: el helper solo se
  llama si `updateDto.getIsActive()` es explícitamente `true`, no para cualquier update.
- Confirmar que `countActiveRoutinesByUserId` sigue siendo coherente tras el fix (siempre devolverá
  0 o 1 para cualquier usuario).

### Agent 4 — Implementor
Archivo único a modificar:
`ifit/src/main/java/com/uca/juangarcia/ifit/modules/training/service/RoutineService.java`

**1. Añadir método privado (antes del cierre de clase)**
```java
private void deactivatePreviousActiveRoutine(Long userId) {
    if (userId == null)
        throw new IllegalArgumentException("User ID cannot be null");
    List<Routine> active = routineRepository.findByUserIdAndIsActive(userId, true);
    if (!active.isEmpty()) {
        active.forEach(r -> r.setActive(false));
        routineRepository.saveAll(active);
    }
}
```

**2. En `createRoutine` (línea ~110), antes de `routine.setActive(true)`**
```java
deactivatePreviousActiveRoutine(requestDto.getUserId());
routine.setActive(true);
```

**3. En `toggleRoutineActive` (línea ~298), antes de `routine.setActive(isActive)`**
```java
if (isActive) {
    deactivatePreviousActiveRoutine(routine.getUser().getId());
}
routine.setActive(isActive);
```

**4. En `updateRoutine` (línea ~259), dentro del bloque `if (updateDto.getIsActive() != null)`**
```java
if (updateDto.getIsActive() != null) {
    if (updateDto.getIsActive()) {
        deactivatePreviousActiveRoutine(routine.getUser().getId());
    }
    routine.setActive(updateDto.getIsActive());
}
```

### Agent 5 — Final verifier
- Verificar que `POST /routines` con un usuario que ya tiene rutina activa resulta en exactamente
  una rutina activa tras la operación (la nueva).
- Verificar que `PATCH /{id}/toggle-active?isActive=true` con otra rutina activa previa resulta
  en exactamente una activa (la toggled).
- Verificar que `PUT /{id}` con `"isActive": true` en body y otra activa previa resulta en
  exactamente una activa.
- Verificar que `PATCH /{id}/toggle-active?isActive=false` no invoca el helper (no debe desactivar
  otras rutinas al desactivar la target).
- Verificar que `POST /{routineId}/complete` (setRoutineAsCompleted) no necesita el helper: solo
  desactiva la rutina target, no activa ninguna otra.
- Verificar que el `countActiveRoutinesByUserId` retorna como máximo 1 para cualquier usuario tras
  el fix.
- Verificar que no hay imports nuevos necesarios (List ya importada, tipos ya presentes).

## Acceptance criteria
1. Tras `createRoutine`, el usuario tiene exactamente 1 rutina activa (la nueva), independientemente
   de cuántas tuviera antes.
2. Tras `toggleRoutineActive(id, true)`, el usuario tiene exactamente 1 rutina activa (la indicada).
3. Tras `updateRoutine(id, {isActive: true})`, el usuario tiene exactamente 1 rutina activa.
4. Las operaciones de desactivación (`isActive=false`, `complete`) no invocan el helper y no alteran
   otras rutinas.
5. El único archivo modificado es `RoutineService.java`. Ningún otro servicio, repositorio,
   controlador o DTO se toca.
6. Los tres métodos afectados mantienen sus anotaciones `@Transactional` sin cambios.

## Risk areas
- **Lazy loading en `toggleRoutineActive`**: `routine.getUser().getId()` puede fallar si el contexto
  de persistencia ya cerró. Mitigación: el método ya es `@Transactional`, por lo que el contexto
  está abierto al hacer el `findById`. Si hay problemas, alternativa: añadir query de userId al
  repositorio por routineId.
- **Concurrencia**: si dos peticiones simultáneas crean rutinas para el mismo usuario, pueden pasar
  el check al mismo tiempo. No es un riesgo real en el contexto del TFG (usuario único por sesión).
- **Regresión en `updateRoutine`**: el método soporta actualización parcial; solo tocar el bloque
  `isActive` y no interferir con descripción, trainingDays ni días.
