# Review report

## Agent 2A — Gateway review

Agent 2A: not applicable for this task.

---

## Agent 2B — Domain review

### Status: APPROVED

### Checks

- [x] Constructor injection: `RoutineService` tiene 7 dependencias inyectadas por constructor
  (líneas 73-87). El nuevo método privado no añade dependencias.
- [x] `@Transactional(readOnly = true)` a nivel de clase (línea 57). No se modifica.
- [x] Métodos de escritura afectados (`createRoutine`, `toggleRoutineActive`, `updateRoutine`)
  ya tienen `@Transactional` propio. El helper privado NO necesita `@Transactional` porque
  siempre se invoca desde un contexto transaccional existente (propagación REQUIRED por defecto).
- [x] Null-guard en el helper: `if (userId == null) throw new IllegalArgumentException(...)`.
  Cumple el patrón de guards obligatorios.
- [x] No se lanza `RuntimeException` raw: el helper lanza `IllegalArgumentException` (aceptable
  para validación de parámetros internos, no es un error de dominio sino una precondición).
- [x] No se crean nuevas entidades — checks de tabla/ID/hashCode/equals no aplican.
- [x] Logger ya presente en `RoutineService` (línea 60: `LoggerFactory.getLogger`). No se
  necesita nuevo logger para el método privado.
- [x] Javadoc no requerido en el helper privado (las convenciones del proyecto solo exigen
  Javadoc en métodos públicos de servicios y controladores).
- [x] No hay lógica de negocio nueva en el controlador — el cambio es íntegramente en el servicio.

### Acceso lazy resuelto dentro de transacción
En `toggleRoutineActive` y `updateRoutine`, `routine.getUser().getId()` accede a la relación
`@ManyToOne(fetch=LAZY)`. Ambos métodos están anotados con `@Transactional`, por lo que el
EntityManager está activo cuando se ejecuta el proxy de Hibernate. La carga lazy es segura.

### Orden de operaciones en `createRoutine`
El orden correcto es:
1. `deactivatePreviousActiveRoutine(requestDto.getUserId())` — desactiva previas (saveAll en BD)
2. `routine.setActive(true)` — marca la nueva como activa
3. `routineRepository.save(routine)` — persiste la nueva rutina
4. Añadir días → `routineDayRepository.save(day)` por cada día
5. `routineRepository.save(routine)` — actualización final

Este orden garantiza atomicidad dentro de la misma transacción: si falla el paso 3 o 4,
el rollback revierte también el paso 1.

### Issues found
Ninguno que bloquee la implementación.

---

## Agent 2C — AI review

Agent 2C: not applicable for this task.
