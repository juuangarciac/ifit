# Analysis report

## Affected service(s)

- **Ifit** — endpoints de auth, users, questionnaires y routines consumidos por los scripts.
  No se modifica ningún archivo Java; se consume la API existente.
- **Ronnie** — consumido indirectamente a través de `POST /routines/generate` en ifit.
  No hay cambios en código Ronnie.
- **ApiGateway** — todas las llamadas atraviesan el gateway. No se añaden ni modifican rutas.

## Dependency graph (creation order)

Solo se crean archivos TypeScript en `test/e2e/`. Sin dependencias Java.

1. `test/e2e/.gitignore`
2. `test/e2e/helpers/questionnaire-runner.ts`
3. `test/e2e/scripts/seed.ts`
4. `test/e2e/scripts/prompt-lab.ts`
5. `test/e2e/package.json` (actualizado: ts-node, npm scripts)
6. `test/e2e/.env.example` (actualizado: LAB_USER_*, LAB_PASS)

## Endpoints consumidos

### Flujo A — Seed
| Método | Ruta gateway | Config |
|---|---|---|
| POST | /ifit/api/v1/auth/login | IFIT-PUBLIC (sin token) |
| POST | /ifit/api/v1/auth/register | IFIT-PUBLIC (sin token) |
| GET | /ifit/api/v1/experience-levels | IFIT-PRIVATE (token) |
| GET | /ifit/api/v1/coach-models | IFIT-PRIVATE (token) |
| PATCH | /ifit/api/v1/users/{id}/assign-experience/{lId} | IFIT-PRIVATE (token) |
| PATCH | /ifit/api/v1/users/{id}/assign-coach/{cId} | IFIT-PRIVATE (token) |
| PATCH | /ifit/api/v1/users/{id}/complete-registration | IFIT-PRIVATE (token) |
| GET | /ifit/api/v1/questionnaires/responses/my-completed-responses | IFIT-PRIVATE (token) |
| GET | /ifit/api/v1/questionnaires/coach/{cId}/experience-level/{lId} | IFIT-PRIVATE (token) |
| GET | /ifit/api/v1/questionnaires | IFIT-PRIVATE (token) |
| POST | /ifit/api/v1/questionnaires/{userId}/start/{qId} | IFIT-PRIVATE (token) |
| POST | /ifit/api/v1/questionnaires/responses/{rId}/answer | IFIT-PRIVATE (token) |

### Flujo B — Prompt Lab (todo lo anterior más:)
| Método | Ruta gateway | Config |
|---|---|---|
| POST | /ifit/api/v1/routines/generate | IFIT-PRIVATE (token) |

## Cross-service impacts

- `POST /routines/generate` en ifit delega internamente en Ronnie. Los scripts solo llaman
  al gateway — la delegación es transparente.
- No hay nuevas rutas en ApiGateway.
- No hay cambios en DTOs de ningún servicio.

## Risk flags

- [x] **Timeout IA** — `POST /routines/generate` puede tardar 15-60s. El Prompt Lab usa
  `timeout: 120_000` por llamada. MITIGADO.
- [x] **Cuestionario dinámico** — questionId y optionId se leen dinámicamente en runtime.
  CORRECTO.
- [x] **CoachType vs CoachModelType.id** — el PATCH de assign-coach usa ID numérico. Los
  scripts lo resuelven con `GET /coach-models` antes de llamar. CORRECTO.
- [ ] **Verificación de email** — si Keycloak exige verificación, el login post-registro falla.
  MITIGACIÓN: usar usuario pre-verificado en .env.
- [x] **Idempotencia del Seed** — comprueba completed responses antes de re-ejecutar.
  CORRECTO.

## Recommended agent focus

- **Agent 2A**: Verificar cobertura de IFIT-PRIVATE sobre los PATCH endpoints de users,
  y que IFIT-PUBLIC cubre /auth sin TokenRelay.
- **Agent 2B**: Confirmar que `PATCH /complete-registration` no requiere body y que
  `my-completed-responses` resuelve el usuario desde el JWT principal.
- **Agent 2C**: Confirmar que la respuesta de `POST /routines/generate` incluye
  `trainingDays[]` iterable y que el timeout de 120s es suficiente.
