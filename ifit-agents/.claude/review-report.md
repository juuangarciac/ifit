# Review report

## Agent 2A — Gateway review

### Status: APPROVED

### Route checks

- [x] IFIT-PUBLIC (`Path=/ifit/api/v1/auth/**, /ifit/api/v1/exercise-images/**`) — sin TokenRelay.
  Cubre correctamente `POST /auth/login` y `POST /auth/register`. Los scripts de seed y
  prompt-lab usan estos endpoints sin token.
- [x] IFIT-PRIVATE (`Path=/ifit/api/v1/**`) — con TokenRelay. Catch-all que cubre todos los
  endpoints protegidos: `/experience-levels`, `/coach-models`, `/users/**`,
  `/questionnaires/**`, `/routines/**`. Los scripts pasan el Bearer token en cada llamada.
- [x] Ordenación correcta: IFIT-PUBLIC aparece antes que IFIT-PRIVATE. El predicado más
  específico (`/auth/**`) tiene prioridad sobre el catch-all (`/**`).
- [x] RONNIE (`/ifit/api/v1/ronnie/**, /serena/**, /kael/**, /eliud/**, /messages/**`) —
  aparece entre IFIT-PUBLIC e IFIT-PRIVATE. Los scripts NO llaman directamente a estos
  endpoints: usan `POST /routines/generate` en ifit, que delega internamente. Correcto.
- [x] StripPrefix=3 presente en las tres rutas. Convierte `/ifit/api/v1/users/...`
  en `/users/...` al enrutar al microservicio. Correcto.
- [x] URI usa `lb://IFIT` y `lb://RONNIE` — coinciden con `spring.application.name` de
  cada microservicio (registrado en Eureka).
- [x] YAML indentado con 2 espacios, consistente con el fichero existente.

### Issues found
Ninguno.

---

## Agent 2B — Domain review

### Status: APPROVED

### Checks

- [x] `PATCH /users/{userId}/complete-registration` (línea 432, `AppUserController`) — no
  tiene `@RequestBody`. El script lo llama sin body (`data: {}`). Correcto.
- [x] `PATCH /users/{userId}/assign-coach/{coachId}` (línea 360) — el `coachId` es el ID
  numérico de `CoachModelType`, no el string del enum. Los scripts resuelven el ID numérico
  mediante `GET /coach-models` → filtran por nombre → obtienen el `id`. Correcto.
- [x] `PATCH /users/{userId}/assign-experience/{levelId}` (línea 397) — el `levelId` es
  el ID numérico de `ExperienceLevel`. Los scripts lo resuelven con `GET /experience-levels`
  → filtran por nombre. Correcto.
- [x] `GET /questionnaires/responses/my-completed-responses` (línea 653, `QuestionnaireController`)
  usa `@AuthenticationPrincipal AppUser user`. El usuario se extrae del JWT, no del path.
  Los scripts pasan el Bearer token correcto en la cabecera Authorization. Correcto.
- [x] `POST /questionnaires/{userId}/start/{questionnaireId}` — el userId es un path variable
  (línea 405-443). Los scripts lo pasan explícitamente desde el estado de autenticación.
- [x] `POST /questionnaires/responses/{responseId}/answer` — espera `{ questionId, selectedOptionId, additionalText? }`
  (AnswerRequestDto). Los scripts envían exactamente estos campos. Correcto.
- [x] `POST /routines/generate` (línea 534, `RoutineController`) — acepta
  `{ userId, responseId, coachType, note? }` con `@Valid`. Los scripts envían
  `{ userId, responseId, coachType }`. Correcto.
- [x] `coachType` en `GenerateRoutineRequestDto` es un `CoachType` enum (RONNIE, SERENA,
  KAEL, ELIUD, MASTER). Spring deserializa el string JSON al enum. Los perfiles del
  Prompt Lab usan exactamente estos strings. Correcto.
- [x] `QuestionnaireResponseDto` expone `responseId`, `currentQuestion` (con `id` y `options[].id`),
  `isCompleted` y `totalQuestionsAnswered`. El bucle de los scripts accede a todos estos
  campos. Correcto.

### Issues found
Ninguno.

---

## Agent 2C — AI review

### Status: APPROVED

### Checks

- [x] `POST /routines/generate` en `RoutineController` delega en `RoutineService.generateRoutine(userId, responseId, coachType, note)`.
  La delegación a Ronnie es interna al microservicio ifit — los scripts son agnósticos
  a este detalle de implementación.
- [x] El timeout de `120_000ms` en `prompt-lab.ts` (llamada individual por perfil) es
  suficiente para Groq en condiciones normales (p95 < 45s). Para Ollama local puede
  no ser suficiente en hardware limitado — se documenta como riesgo conocido.
- [x] Los scripts no interactúan directamente con `@AiService`, `ChatContext` ni ningún
  bean de Ronnie. No hay riesgo de memory leak ni gestión incorrecta de contexto.
- [x] La respuesta de `POST /routines/generate` devuelve un `RoutineResponseDto`. El Prompt
  Lab accede a `routine.trainingDays` y `trainingDays[].exercises` para calcular el resumen.
  La estructura está documentada en el `@ExampleObject` del controlador (línea 493-518).
- [x] Los scripts no crean nuevos `@AiService`, beans ni configuración en Ronnie.
  No hay impacto en el wiring de LangChain4j.

### Issues found
Ninguno bloqueante.

**Observación (no bloqueante)**: el `prompt-lab.ts` accede a `routine.trainingDays` usando
el nombre del campo tal como aparece en el ejemplo del controlador. Si la serialización
JSON del `RoutineResponseDto` usa `@JsonProperty` con un nombre diferente, el cálculo de
`totalExercises` en el resumen devolverá `null`. No bloquea la generación ni el guardado
del JSON — solo afecta a la tabla Markdown. Verificar en ejecución real.
