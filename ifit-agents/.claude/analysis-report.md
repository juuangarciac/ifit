# Analysis report

## Affected service(s)

### Ifit (`com.uca.juangarcia.ifit`)
Único archivo a modificar:
`ifit/src/main/java/com/uca/juangarcia/ifit/modules/training/service/RoutineService.java`

Tres métodos no respetan la constraint de negocio "un usuario solo puede tener una rutina
activa en un instante dado":
- `createRoutine` (líneas 96-132): siempre activa la nueva rutina sin desactivar las previas.
- `toggleRoutineActive` (líneas 292-303): activa la rutina target sin desactivar las demás.
- `updateRoutine` (líneas 259-261): al procesar `isActive=true`, no desactiva otras rutinas.

### ApiGateway
No afectado. Sin cambios en routes, predicates, filters ni JWT config.

### Ronnie
No afectado. Sin cambios en coaches, @AiService ni system prompts.

---

## Dependency graph (creation order)
No se crean clases nuevas. Modificación única:

1. `RoutineService.java` — nuevo método privado helper + tres correcciones en métodos existentes.

Clases relacionadas (solo lectura, sin modificación):
- `RoutineRepository` — ya expone `findByUserIdAndIsActive(Long, boolean)` (línea 32) y
  `saveAll` heredado de `JpaRepository<Routine, Long>`.
- `Routine` — entidad con campo `isActive` (boolean) y `@ManyToOne(fetch=LAZY)` hacia `AppUser`.

---

## Cross-service impacts
Ninguno.
- No se añaden ni modifican endpoints: el contrato HTTP existente no cambia.
- No se requieren nuevas rutas en `application.yaml` de ApiGateway.
- Ronnie no envía ni recibe ningún campo relacionado con `isActive`.

---

## Risk flags
- [x] **Lazy loading en `toggleRoutineActive` y `updateRoutine`**: `routine.getUser().getId()`
  accede a `@ManyToOne(fetch=LAZY)`. Ambos métodos son `@Transactional`, por lo que el
  contexto de persistencia está abierto y el proxy Hibernate se resuelve correctamente.
  Sin riesgo real.
- [ ] **Concurrencia**: dos peticiones simultáneas para el mismo usuario podrían pasar el
  check al mismo tiempo y crear dos rutinas activas. No es un riesgo en el contexto del
  TFG (sesión única por usuario).
- [ ] **Test coverage**: no existe `RoutineServiceTest.java`. Los tres cambios no están
  cubiertos por tests automáticos. Se documenta como deuda técnica.

---

## Recommended agent focus
- Agent 2A: SKIP — sin impacto en ApiGateway.
- Agent 2B: Verificar `@Transactional` en el helper privado, acceso lazy a `getUser().getId()`
  dentro de transacción, null-guard en el helper, y orden de operaciones en `createRoutine`.
- Agent 2C: SKIP — sin cambios en Ronnie.
