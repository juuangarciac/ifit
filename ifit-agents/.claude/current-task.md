# Task: Añadir dos flujos de testing E2E al proyecto Playwright (test/e2e)

## Request
El proyecto base de Playwright E2E ya existe en `test/e2e/` con sus 8 suites de tests sobre
los controladores. Se necesitan dos flujos adicionales que no son tests ordinarios sino scripts
de desarrollo/utilidad:

**Flujo A — Seed (datos de prueba persistentes)**
Script idempotente que crea un usuario de test y completa su cuestionario automáticamente, de
modo que cada vez que se reinicia la app se puede restaurar el estado mínimo necesario para
ejecutar los demás tests sin volver a rellenar el cuestionario a mano. Guarda el estado en
`.seed-state.json`.

**Flujo B — Prompt Lab (matriz de generación de rutinas)**
Script que genera rutinas usando múltiples perfiles de usuario (diferentes coaches, niveles y
objetivos) para poder estudiar y comparar las respuestas de la IA y ajustar los prompts.
Guarda cada rutina generada en `test/e2e/outputs/` como JSON y una tabla resumen en Markdown.

## Affected services
- **Ifit** — auth (register/login), users (assign-coach, assign-experience, complete-registration),
  questionnaires (start, answer), routines (generate)
- **Ronnie** — indirectamente, a través de POST /routines/generate que ifit delega en Ronnie
- **ApiGateway** — todas las llamadas pasan por él; StripPrefix=3 en todas las rutas

## Change type
FEAT (infraestructura de testing — ningún cambio en código Java)

## API flow completo — protocolo del cuestionario

```
1. POST /auth/login o /auth/register  → { accessToken, refreshToken, appUser.id }
2. PATCH /users/{id}/assign-experience/{levelId}  → 200
3. PATCH /users/{id}/assign-coach/{coachId}       → 200
4. PATCH /users/{id}/complete-registration        → 200
5. GET  /questionnaires/coach/{coachId}/experience-level/{levelId} → { id: questionnaireId }
6. POST /questionnaires/{userId}/start/{questionnaireId}
   → { responseId, currentQuestion: { id, text, options:[{id,text}] }, isCompleted: false }
7. Bucle hasta isCompleted=true:
     POST /questionnaires/responses/{responseId}/answer
     body: { questionId, selectedOptionId, additionalText? }
     → { responseId, currentQuestion, isCompleted, totalQuestionsAnswered }
8. Guardar: { userId, responseId, accessToken, refreshToken }
```

## GenerateRoutineRequestDto
```json
{ "userId": 1, "responseId": 42, "coachType": "RONNIE" }
```
CoachType enum: RONNIE | SERENA | KAEL | ELIUD | MASTER

## RegisterRequestDto
```json
{ "name": "...", "surname": "...", "email": "...", "password": "..." }
```

## Agent assignments

### Agent 1 — Context analyst
Verificar en `test/e2e/`:
- Confirmar que el proyecto base compila limpio y tiene los helpers necesarios.
- Identificar qué endpoints necesita cada flujo:
  - Seed: /auth/login, /auth/register, /users/{id}/assign-experience,
    /users/{id}/assign-coach, /users/{id}/complete-registration,
    /questionnaires/coach/{cId}/experience-level/{lId},
    /questionnaires/{userId}/start/{qId}, /questionnaires/responses/{rId}/answer,
    /questionnaires/responses/my-completed-responses
  - Prompt Lab: todos los del Seed + POST /routines/generate
- Confirmar que `helpers/api-client.ts` tiene los métodos `get`, `post`, `patch` necesarios.
- Confirmar estructura de salida: `test/e2e/outputs/` (crearla si no existe).

### Agent 2A — Gateway guardian
Verificar en `api-gateway/src/main/resources/application.yaml`:
- IFIT-PUBLIC: `/ifit/api/v1/auth/**` → sin TokenRelay. Correcto para /auth/login y /auth/register.
- IFIT-PRIVATE: `/ifit/api/v1/**` → con TokenRelay. Cubre users, questionnaires, routines.
- RONNIE: `/ifit/api/v1/ronnie/**` etc → con TokenRelay. No aplica directamente al Prompt Lab
  porque éste llama a POST /routines/generate (en ifit), que luego ifit delega en Ronnie.
- Confirmar que PATCH endpoints (/users/{id}/assign-coach, /users/{id}/assign-experience,
  /users/{id}/complete-registration) están cubiertos por la ruta IFIT-PRIVATE.
- Confirmar StripPrefix=3 convierte `/ifit/api/v1/users/**` → `/users/**` en el microservicio.

### Agent 2B — Domain guardian
Verificar en ifit (microservicio):
- `AppUserController`: PATCH /users/{userId}/assign-coach/{coachId} — confirmar que el coachId
  coincide con el ID de la entidad CoachModelType (no con el enum CoachType de Ronnie).
- `AppUserController`: PATCH /users/{userId}/assign-experience/{levelId} — confirmar campo.
- `AppUserController`: PATCH /users/{userId}/complete-registration — confirmar que no necesita body.
- `QuestionnaireController`: POST /questionnaires/{userId}/start/{questionnaireId} — 201 CREATED.
  El userId es un path variable (no del principal JWT), confirmar.
- `QuestionnaireController`: GET /questionnaires/responses/my-completed-responses usa
  @AuthenticationPrincipal — el script debe pasar Bearer token correcto.
- `RoutineController`: POST /routines/generate — confirmar que acepta { userId, responseId, coachType }
  y que coachType es el string del enum CoachType (RONNIE, SERENA, KAEL, ELIUD, MASTER).
- Confirmar que QuestionnaireResponseDto tiene `isCompleted` y `currentQuestion.options[].id`
  accesibles para el bucle de auto-respuesta.

### Agent 2C — AI guardian
Verificar en Ronnie:
- Que POST /routines/generate en ifit delega la llamada a Ronnie correctamente (MasterController
  o el controller del coach específico).
- Que la respuesta de generación devuelve un JSON con la rutina completa (estructura esperada
  en el Prompt Lab para guardar en outputs/).
- Confirmar que los timeouts de 30s en playwright.config.ts son suficientes, o si el Prompt Lab
  necesita un timeout mayor (las llamadas de IA pueden tardar 15-60s con Groq/Ollama).
  → El Prompt Lab debe configurar timeout: 120_000 o bien usar su propio APIRequestContext.

### Agent 3 — Quality reviewer
- Verificar que la implementación base (Playwright) compila sin errores.
- Verificar que los dos nuevos scripts NO son specs de Playwright (`*.spec.ts`) sino scripts
  standalone (`scripts/*.ts`) ejecutados con `ts-node` o compilados y corridos con `node`.
- Verificar que el Seed es idempotente:
  - Si el usuario ya existe (login exitoso) y ya tiene completed responses → NO re-completa el
    cuestionario, simplemente guarda el responseId existente.
  - Si el usuario ya existe pero NO tiene completed responses → completa el cuestionario.
  - Si el usuario no existe → lo registra y completa el cuestionario.
- Verificar que el Prompt Lab NO usa `test()` de Playwright (no tiene assert pass/fail) sino
  que es un script imperativo que guarda resultados.
- Verificar que los outputs del Prompt Lab tienen nombre determinista:
  `{coachType}-{experienceLevel}-{timestamp}.json` para poder comparar entre ejecuciones.
- Confirmar que `.seed-state.json` y `outputs/` están en `.gitignore`.

### Agent 4 — Implementor
Archivos a crear en `test/e2e/`:

**1. `helpers/questionnaire-runner.ts`**
Helper que dado un `ApiClient` + `userId` + `questionnaireId`:
- Inicia la sesión (POST start)
- Bucle: lee `currentQuestion.options`, selecciona según estrategia, posta answer
- Retorna el `responseId` final cuando `isCompleted=true`
- Estrategias: `FIRST` (siempre primera opción), `LAST` (última), `INDEX(n)` (posición fija)

**2. `scripts/seed.ts`**
Script Node.js standalone que:
1. Lee .env (dotenv)
2. Intenta login con TEST_USER_EMAIL / TEST_USER_PASSWORD
3. Si falla (401) → registra con POST /auth/register
4. Comprueba si ya tiene completed responses → si sí, toma el último responseId
5. Si no: asigna experience-level (id=1), asigna coach (id=1), complete-registration,
   busca cuestionario para esa combinación, ejecuta QuestionnaireRunner (estrategia FIRST)
6. Guarda `.seed-state.json`: { userId, responseId, accessToken, refreshToken }
7. Imprime resumen en consola

**3. `scripts/prompt-lab.ts`**
Script Node.js standalone que:
1. Define array de TestProfile: { label, email, password, coachType, coachId, levelId, strategy }
2. Perfiles mínimos a incluir (ajustables):
   - RONNIE / Principiante / Ganar músculo
   - SERENA / Principiante / Tonificar
   - KAEL / Avanzado / Muscle-up
   - ELIUD / Intermedio / Preparar 10K
   - MASTER / Intermedio / Entrenamiento general
3. Para cada perfil:
   a. Login o register
   b. Asignar experience + coach + complete-registration (si es nuevo)
   c. Obtener completed response o completar cuestionario
   d. POST /routines/generate con el coachType del perfil
   e. Guardar JSON en `outputs/{label}-{date}.json`
4. Al final: escribir `outputs/summary-{date}.md` con tabla comparativa

**4. Actualizar `package.json`**
Añadir a devDependencies: `"ts-node": "^10.9.2"`
Añadir scripts:
- `"seed": "ts-node scripts/seed.ts"`
- `"prompt-lab": "ts-node scripts/prompt-lab.ts"`

**5. Actualizar `.gitignore`**
Crear `test/e2e/.gitignore` con: `.env`, `.auth-state.json`, `.seed-state.json`, `outputs/`

**6. `playwright.config.ts`**
Sin cambios — los scripts no son specs de Playwright.

### Agent 5 — Final verifier
- Ejecutar `npm run seed` (con servicios levantados) → confirmar que `.seed-state.json` se crea.
- Ejecutar `npm run seed` de nuevo → confirmar idempotencia (mismo responseId, no nuevo cuestionario).
- Ejecutar `npm run prompt-lab` → confirmar que se crean al menos los JSONs en outputs/.
- Ejecutar `npm test` → confirmar que los tests existentes siguen en verde.
- Confirmar que `test/e2e/.gitignore` excluye correctamente los artefactos generados.
- Confirmar que el timeout de la generación de IA no hace fallar los scripts (ajustar si necesario).

## Acceptance criteria
1. `npm run seed` crea `.seed-state.json` con userId y responseId válidos.
2. Ejecutado dos veces consecutivas, `npm run seed` produce el mismo responseId (idempotente).
3. `npm run prompt-lab` genera un JSON por cada perfil en `test/e2e/outputs/`.
4. `outputs/summary-{date}.md` incluye tabla con: perfil, coachType, días de la rutina, nº ejercicios.
5. `npm test` no regresa — todos los tests base siguen pasando.
6. `.env`, `.auth-state.json`, `.seed-state.json`, `outputs/` ignorados por git.
7. TypeScript compila sin errores (`tsc --noEmit` limpio).

## Risk areas
- **Cuestionario dinámico**: los questionId y optionId son IDs de BD desconocidos en tiempo
  de script. El bucle los lee dinámicamente desde la respuesta de la API — esto es correcto.
- **Timeout IA**: Groq puede tardar entre 5-60s según carga. El Prompt Lab no debe usar el
  timeout global de Playwright (30s). Usar `request.newContext` con `timeout: 0` o ajustar
  por llamada.
- **Verificación de email**: si el sistema de registro requiere verificación por email,
  el script de seed fallará en `POST /auth/login` tras el registro. Mitigación: crear el
  usuario de seed directamente en Keycloak (vía admin API) o usar un usuario pre-verificado
  definido en .env (TEST_USER_EMAIL / TEST_USER_PASSWORD). Recomendado: usar usuario ya
  verificado en .env para el Seed; el registro automático es solo fallback.
- **CoachType vs CoachModelType.id**: el `coachId` en `/users/{id}/assign-coach/{coachId}`
  es el ID numérico de la tabla `coach_model_type`, no el string del enum. El Prompt Lab
  debe resolver el coachId desde GET /coach-models/name/{name} antes de asignar.
