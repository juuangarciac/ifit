# Plan de implementación — Panel de Administración iFit (Vaadin)

> Documento de planificación. Objetivo: un **cliente web de administración sencillo** construido con
> **Vaadin Flow**, que cubra el caso de uso **CU-10** (*Administrar cuestionarios, ejercicios,
> entrenadores y usuarios*) centrándose en **CRUD de clientes y sus rutinas asignadas**, reutilizando
> la identidad visual definida en `res/Styles.xaml` y `res/Colors.xaml`.
>
> Fuente del análisis: `admin-panel/res/analysis.tex` (modelo conceptual y casos de uso) y el grafo de
> conocimiento `graphify-out/` (contratos de API, seguridad y rutas).

---

## 1. Objetivo y alcance

La aplicación móvil (MAUI) ya cubre el rol **Usuario**. Falta una herramienta para el rol
**Administrador** descrito en el `analysis.tex`:

> *"Administrador: gestiona los cuestionarios, los ejercicios, los entrenadores y los usuarios de la plataforma."* (CU-10)

Para mantener la **implementación sencilla**, el alcance se divide en dos niveles:

### Alcance núcleo (MVP — obligatorio)
1. **Login de administrador** contra la API existente (Keycloak/JWT) y verificación de rol `ADMIN`.
2. **Gestión de Usuarios/Clientes** (`AppUser`): listar, buscar, ver detalle, editar, eliminar.
   *(Crear queda fuera del MVP: `POST /users` solo crea fila en BD sin identidad en Keycloak → no podría
   iniciar sesión. Los clientes se auto-registran desde la app; la creación coherente Keycloak+BD es el
   "Paso 2" de seguridad, pendiente.)*
3. **Rutinas asignadas a un cliente** (`Routine`): listar por usuario, ver detalle (días/ejercicios),
   activar/desactivar, eliminar.

### Alcance ampliado (opcional, mismo patrón CRUD)
4. **Catálogo de ejercicios** (`ExerciseCatalog`) — solo lectura + alta/edición si hay tiempo.
5. **Entrenadores** (`CoachModelType`) — CRUD.
6. **Niveles de experiencia** (`ExperienceLevel`) — CRUD.
7. **Cuestionarios** (`Questionnaire`) — lectura (el editor de árbol de decisión es complejo; queda fuera del MVP).

> **Decisión de simplicidad:** el MVP se limita a 1–3. Todo lo demás reutiliza exactamente el mismo
> patrón (Grid + formulario lateral + servicio REST), por lo que añadir cada entidad cuesta ~1 vista.

---

## 2. Decisiones tecnológicas

| Aspecto | Decisión | Justificación |
|---|---|---|
| Framework UI | **Vaadin Flow 24.x** | Todo en Java, sin frontend JS aparte. CRUD con `Grid` + `Binder` es trivial. |
| Runtime | **Spring Boot 3.5.7 / Java 21 / Maven** | Idéntico al resto de microservicios (ver `CLAUDE.md`). |
| Acceso a datos | **Cliente REST** contra el **API Gateway** | El panel **no** accede a la BD ni a los repos JPA; consume la misma API que la app móvil. |
| Cliente HTTP | `RestClient` (Spring 6) o `WebClient` | Envío del `Authorization: Bearer <jwt>` en cada llamada. |
| Seguridad | Spring Security + token JWT obtenido vía login de iFit | Reutiliza Keycloak; el panel solo guarda el token en sesión. |
| Tema | **Lumo (dark)** + `styles.css` con la paleta de `Colors.xaml` | Coincide con la identidad de la app (§5). |

**Módulo nuevo:** `admin-panel/` pasa a ser un proyecto Maven Spring Boot independiente
(hermano de `ifit/`, `ronnie/`, `api-gateway/`). La carpeta `res/` se conserva como material de diseño.

---

## 3. Arquitectura y enrutamiento (API Gateway)

```
┌─────────────────┐   Authorization: Bearer <jwt>   ┌────────────────────┐
│  admin-panel    │ ──────────────────────────────► │ API Gateway :8080  │
│  (Vaadin Flow)  │   http://localhost:8080         │ (Spring Cloud GW)  │
└─────────────────┘   /ifit/api/v1/<ruta>           │  StripPrefix=3     │
                                                     └─────────┬──────────┘
                                       lb://IFIT  ◄────────────┤  (vía Eureka)
                                       lb://RONNIE ◄───────────┘
```

**El panel SIEMPRE habla con el Gateway (`http://localhost:8080`), nunca con los microservicios
directamente.** El Gateway descubre las instancias por **Eureka** (`lb://IFIT`, `lb://RONNIE`).

Configuración real (`api-gateway/src/main/resources/application.yaml`):

- **No hay segmento `{service}`.** `StripPrefix=3` elimina exactamente `ifit/api/v1`, y lo que queda
  es la **ruta del propio controller** (`@RequestMapping`). Ej.: `GET /ifit/api/v1/users` → el Gateway
  reenvía `GET /users` a `lb://IFIT`.
- Tres rutas, **evaluadas en orden** (la primera que casa, gana):

| Ruta (id) | Predicado `Path` | Filtros | Destino |
|---|---|---|---|
| `IFIT-PUBLIC` | `/ifit/api/v1/auth/**`, `/ifit/api/v1/exercise-images/**` | `StripPrefix=3` (**sin** TokenRelay) | `lb://IFIT` |
| `RONNIE` | `/ifit/api/v1/{ronnie,serena,kael,eliud,messages}/**` | `StripPrefix=3` + **TokenRelay** | `lb://RONNIE` |
| `IFIT-PRIVATE` | `/ifit/api/v1/**` (catch-all) | `StripPrefix=3` + **TokenRelay** | `lb://IFIT` |

- El **login** cae en `IFIT-PUBLIC` (sin token, como debe ser). El resto de operaciones del panel
  (usuarios, rutinas, catálogos) caen en `IFIT-PRIVATE`, que **valida el JWT** en el Gateway
  (OAuth2 Resource Server, issuer Keycloak `ifit-realm`) y lo **reenvía** con `TokenRelay`.
- En el cliente, basta con enviar `Authorization: Bearer <accessToken>` en cada petición al Gateway.

---

## 4. Autenticación y autorización

Modelo confirmado (gateway + iFit `SpringSecurityConfig` + grafo `graphify-out`):
- Tanto el **Gateway** como **iFit** son **OAuth2 Resource Server** con JWT (issuer Keycloak `ifit-realm`).
- `JwtAuthenticationConverter.extractResourceRoles()` extrae los **roles de recurso de Keycloak** del JWT.
- El **rol de administrador es la *resource role* de Keycloak `admin_client_role`** (visto en
  `KeycloakController` → `@PreAuthorize("hasRole('admin_client_role')")`). **No** es el campo
  `appUser.roleName` (`USER`/`ADMIN`), que es un concepto separado del modelo de datos.
- **Importante:** en iFit, los CRUD de `/users`, `/routines`, etc. solo exigen `authenticated()`
  (no rol admin) salvo `KeycloakController`. Ver riesgo en §12.

**Flujo de login del panel:**
1. `LoginView` (formulario usuario/contraseña).
2. `POST http://localhost:8080/ifit/api/v1/auth/login` con `LoginRequestDto { username, password }`
   (ruta **pública** `IFIT-PUBLIC`, sin token).
3. Respuesta `LoginResponseDto { accessToken, refreshToken, expiresIn, tokenType, appUser }`.
4. **Verificar autorización admin:** decodificar el `accessToken` (JWT) y comprobar que sus roles de
   recurso contienen **`admin_client_role`**. Si no, denegar acceso ("No autorizado").
   *(Alternativa más simple como complemento: `appUser.roleName == "ADMIN"`, pero la fuente autoritativa
   es la resource role del token.)*
5. Guardar `accessToken` (y `refreshToken`) en la sesión Vaadin (`VaadinSession`).
6. Un `AuthService` añade `Authorization: Bearer <accessToken>` a cada llamada del `RestClient` al Gateway.
7. Refrescar token con `POST /ifit/api/v1/auth/refreshToken` cuando expire (opcional para MVP; basta con re-login al recibir `401`).

> **Simplicidad:** para el MVP no hace falta `BeforeEnterObserver`/`@Route` con roles de Spring Security
> completo; basta con un guard simple que redirige a `LoginView` si no hay token válido en sesión.

---

## 5. Mapeo de la identidad visual (`Colors.xaml` / `Styles.xaml` → tema Vaadin)

La app usa un **tema oscuro** con acento amarillo y tipografía **Montserrat**. Se traduce a variables
Lumo en `src/main/frontend/themes/ifit-admin/styles.css`:

| Concepto en XAML | Valor | Variable Lumo / uso |
|---|---|---|
| `PrimaryColor` / `BackgroundPrimaryDark` | `#222831` | Fondo de la app (`--lumo-base-color`) |
| `SecondaryColor` / `BackgroundSecondaryDark` | `#393E46` | Superficies, cards, cabecera de Grid |
| `TertiaryColor` (acento) | `#FFD369` | `--lumo-primary-color` (botones, enlaces, selección) |
| `QuaternaryColor` / `TextPrimaryColorLight` | `#EEEEEE` | `--lumo-body-text-color` |
| Tipografía títulos | `Montserrat-Bold` | `--lumo-font-family` + pesos en encabezados |
| Tipografía cuerpo | `Montserrat-Medium` | Texto base |
| Radio de borde | `8` (`CornerRadiusRectangle`) | `--lumo-border-radius-m` |

```css
/* themes/ifit-admin/styles.css */
[theme~="dark"], :root {
  --lumo-base-color: #222831;
  --lumo-tint-5pct: #393E46;
  --lumo-primary-color: #FFD369;
  --lumo-primary-text-color: #FFD369;
  --lumo-primary-contrast-color: #222831;  /* texto sobre botón amarillo */
  --lumo-body-text-color: #EEEEEE;
  --lumo-header-text-color: #FFFFFF;
  --lumo-border-radius-m: 8px;
  --lumo-font-family: "Montserrat", system-ui, sans-serif;
}
```

- Cargar la fuente **Montserrat** (Bold/Medium) como webfont (Google Fonts o copiando los `.ttf` que ya use la app móvil).
- Activar el modo oscuro en `MainLayout` con `UI.getCurrent().getElement().setAttribute("theme", "dark")`.
- Cards/paneles de detalle imitan los `Border` con `RoundRectangle CornerRadius=8` de `HomeView.xaml`.

---

## 6. Modelo de datos del cliente (DTOs a replicar)

Se crean POJOs/records en el panel que reflejan los DTOs de la API (confirmados en el grafo):

**`AppUserResponseDto`** → `id, name, email, isRegistrationComplete, isVerified, createdAt, updatedAt, roleName, coachModelTypeName, experienceLevelName`

**`RoutineResponseDto`** → `id, userId, message, description, trainingDays, isActive, deleted, currentDay, createdAt, updatedAt, days[]` (cada `RoutineDayDto` con sus ejercicios)

> Solo se replican los campos que el panel necesita mostrar/editar. Para crear/editar usuario se usarán
> los DTOs de petición correspondientes (`CreateAppUserRequestDto`, `UpdateAppUserRequestDto`).

---

## 7. Endpoints consumidos (mapeo funcionalidad → API)

Todas las URLs son **a través del Gateway**: `http://localhost:8080` + ruta. Tras `StripPrefix=3`,
el prefijo `/ifit/api/v1` desaparece y la petición llega al controller con su `@RequestMapping`
(confirmados en el código). Base de cada controller:

| Controller | `@RequestMapping` | URL base vía Gateway |
|---|---|---|
| `AuthenticationController` | `/auth` | `/ifit/api/v1/auth` |
| `AppUserController` | `/users` | `/ifit/api/v1/users` |
| `RoutineController` | `/routines` | `/ifit/api/v1/routines` |
| `ExerciseCatalogController` | `/exercises` | `/ifit/api/v1/exercises` |
| `CoachModelTypeController` | `/coach-models` | `/ifit/api/v1/coach-models` |
| `ExperienceLevelController` | `/experience-levels` | `/ifit/api/v1/experience-levels` |
| `QuestionnaireController` | `/questionnaires` | `/ifit/api/v1/questionnaires` |

Mapeo funcionalidad → operación:

| Funcionalidad del panel | Método + URL (vía Gateway) |
|---|---|
| Login admin | `POST /ifit/api/v1/auth/login` · `POST /ifit/api/v1/auth/refreshToken` |
| Listar/buscar clientes | `GET /ifit/api/v1/users` · `GET /ifit/api/v1/users/paginated` · `GET /ifit/api/v1/users/email/{email}` |
| Ver / crear / editar / borrar cliente | `GET /ifit/api/v1/users/{id}` · `POST /ifit/api/v1/users` · `PUT /ifit/api/v1/users/{id}` · `DELETE /ifit/api/v1/users/{id}` |
| Asignar coach / nivel | `PATCH /ifit/api/v1/users/{id}/...` (setCoachModelType / setExperienceLevel) |
| Rutinas de un cliente | `GET /ifit/api/v1/routines/user/{userId}` · `GET /ifit/api/v1/routines/{id}` |
| Gestionar rutina | `PATCH /ifit/api/v1/routines/{id}/...` (toggleActive) · `PUT /ifit/api/v1/routines/{id}` · `DELETE /ifit/api/v1/routines/{id}` |
| (Ampliado) Ejercicios | `GET /ifit/api/v1/exercises` · `GET /ifit/api/v1/exercises/{id}` |
| (Ampliado) Entrenadores | `GET/POST/PUT/DELETE/PATCH /ifit/api/v1/coach-models[/{id}]` |
| (Ampliado) Niveles | `GET/POST/PUT/DELETE /ifit/api/v1/experience-levels[/{id}]` |
| (Ampliado) Cuestionarios | `GET /ifit/api/v1/questionnaires` · `GET /ifit/api/v1/questionnaires/{id}` (lectura) |

> Los sub-paths exactos de cada método (p. ej. el segmento de `setCoachModelType` o `toggleRoutineActive`)
> se confirman en el `@GetMapping`/`@PostMapping`/`@PatchMapping` de cada método al implementar; la **URL base
> y el enrutamiento del Gateway** ya están fijados arriba.

---

## 8. Estructura del proyecto

```
admin-panel/
├─ pom.xml                       # Spring Boot + vaadin-spring-boot-starter
├─ res/                          # (material de diseño existente — sin tocar)
├─ src/main/java/com/uca/juangarcia/adminpanel/
│  ├─ AdminPanelApplication.java
│  ├─ client/                    # capa REST hacia el Gateway
│  │  ├─ ApiClientConfig.java    # RestClient con baseUrl del Gateway
│  │  ├─ AuthService.java        # login, almacenamiento de token en VaadinSession
│  │  ├─ UserApiClient.java
│  │  └─ RoutineApiClient.java
│  ├─ dto/                       # AppUserResponseDto, RoutineResponseDto, Login*Dto...
│  ├─ security/
│  │  └─ SessionGuard.java       # redirige a login si no hay token/rol ADMIN
│  └─ views/
│     ├─ MainLayout.java         # AppLayout (drawer + navbar, tema dark)
│     ├─ LoginView.java          # @Route("login")
│     ├─ UsersView.java          # @Route("users") — Grid + editor
│     └─ RoutinesView.java       # @Route("users/:id/routines")
└─ src/main/frontend/themes/ifit-admin/
   ├─ styles.css                 # paleta + Montserrat (§5)
   └─ theme.json
```

---

## 9. Vistas / UI

- **`MainLayout`** (`AppLayout`): cabecera con logo iFit + nombre del admin + logout; *drawer* lateral con
  navegación (Clientes, Rutinas, [Ejercicios], [Entrenadores]). Fondo `#222831`, acento `#FFD369`.
- **`LoginView`**: `LoginForm` de Vaadin centrado sobre fondo oscuro; en `onLogin` llama a `AuthService`.
- **`UsersView`**:
  - `Grid<AppUserResponseDto>` con columnas `name, email, roleName, experienceLevelName, isVerified`.
  - `TextField` de búsqueda (filtra por email/nombre).
  - Botón **"Nuevo cliente"** y, al seleccionar fila, panel/diálogo de edición con `Binder` y validaciones.
  - Acciones por fila: **Editar**, **Eliminar** (con diálogo de confirmación), **Ver rutinas**.
- **`RoutinesView`** (rutinas del cliente seleccionado):
  - `Grid<RoutineResponseDto>` con `description, trainingDays, isActive, currentDay, createdAt`.
  - Detalle expandible que muestra los `days[]` y sus ejercicios (solo lectura en MVP).
  - Acciones: **Activar/Desactivar** (`toggleRoutineActive`), **Eliminar** (`deleteRoutine`).

---

## 10. Plan de implementación por fases

**Fase 0 — Andamiaje (0.5 día)**
1. Crear proyecto Vaadin Flow + Spring Boot en `admin-panel/` (start.vaadin.com o arquetipo Maven).
2. Configurar `application.properties`: puerto propio (≠ 8080) y `ifit.gateway.base-url=http://localhost:8080`.
   El `RestClient` se construye con esa `baseUrl` y todas las rutas se escriben como `/ifit/api/v1/...`.
3. Aplicar el tema `ifit-admin` (§5) y cargar Montserrat.

**Fase 1 — Autenticación (0.5 día)**
4. `AuthService` + `LoginView`: login contra `auth/login`, validar rol `ADMIN`, guardar token en sesión.
5. `SessionGuard` que protege las rutas (redirección a `/login`).

**Fase 2 — CRUD de Clientes (1–1.5 días)**
6. `UserApiClient` (GET lista, GET por id, POST, PUT, DELETE) inyectando el Bearer.
7. `UsersView`: Grid + búsqueda + formulario con `Binder` + confirmación de borrado.

**Fase 3 — Rutinas del cliente (1 día)**
8. `RoutineApiClient` (`getRoutinesByUserId`, `toggleRoutineActive`, `deleteRoutine`).
9. `RoutinesView`: Grid + detalle de días/ejercicios + activar/desactivar/eliminar.

**Fase 4 — Pulido y opcionales (según tiempo)**
10. Notificaciones (`Notification`) de éxito/error y manejo de errores HTTP (401 → re-login).
11. (Opcional) Vistas de Ejercicios, Entrenadores, Niveles reutilizando el patrón de `UsersView`.

> Estimación total MVP (Fases 0–3): **~3–4 días**.

---

## 11. Criterios de aceptación

- [ ] Un usuario con rol `ADMIN` puede iniciar sesión; uno sin rol `ADMIN` es rechazado.
- [ ] Se listan los clientes desde la API real y se puede **buscar** por nombre/email.
- [ ] Se puede **crear, editar y eliminar** un cliente y los cambios persisten (verificable recargando).
- [ ] Desde un cliente se ven sus **rutinas asignadas** con sus días/ejercicios.
- [ ] Se puede **activar/desactivar** y **eliminar** una rutina de un cliente.
- [ ] Todas las llamadas pasan por el **Gateway** con `Authorization: Bearer`.
- [ ] La UI usa la paleta (`#222831`/`#393E46`/`#FFD369`/`#EEEEEE`) y Montserrat, en modo oscuro.

---

## 12. Riesgos y notas

- **Autorización admin en backend (RESUELTO):** los endpoints administrativos de `/users`, `/routines`,
  `/coach-models`, `/experience-levels` y `/questionnaires` ya están protegidos con
  `@PreAuthorize("hasRole('admin_client_role')")` (method security activada). El *gate* del panel es ahora
  **defensa en profundidad**: la UI valida el rol en el login y el backend devuelve **403** a cualquier
  token sin `admin_client_role`. El panel debe usar un **token de admin** (el bootstrap
  `adminifit96@gmail.com` tiene el realm role `admin` → composite → `admin_client_role`).
- **Coherencia Keycloak en crear/borrar usuario:** `POST /users` crea solo en BD (sin Keycloak) y
  `DELETE /users/{id}` borra en BD pero deja la identidad en Keycloak. Por eso el MVP **no** crea usuarios;
  el borrado deja un huérfano en Keycloak (a limpiar manualmente o en el "Paso 2").
- **Colisión de ruta para `/auth/user/**`:** `KeycloakController` (`@RequestMapping("/auth/user")`, admin-only)
  cae bajo la ruta `IFIT-PUBLIC` (`/ifit/api/v1/auth/**`), que **no** lleva `TokenRelay` → el JWT no se reenvía
  y esos endpoints fallarían vía Gateway. **El MVP no los usa** (gestiona clientes vía `AppUserController`),
  pero si en el futuro se necesitan, habría que reordenar/añadir una ruta específica con `TokenRelay`.
- **TokenRelay vs token entrante:** el Gateway valida el JWT (resource server) y lo reenvía con `TokenRelay`.
  El panel solo debe mandar `Authorization: Bearer <accessToken>` al Gateway (igual que la app móvil).
- **CORS:** al ser Vaadin Flow *server-side*, el navegador no llama a la API directamente (lo hace el
  backend del panel), por lo que **no hay problema de CORS**. Solo hay que permitir el tráfico panel→gateway.
- **Dependencias de arranque:** el Gateway descubre los servicios por **Eureka** (`lb://IFIT`, `lb://RONNIE`).
  Para que el panel funcione deben estar arriba: Keycloak (`:9090`), Eureka (`:8761`), Gateway (`:8080`) e IFIT.
- **Caducidad del token:** para el MVP basta con re-login al recibir `401`; el `refreshToken` es una mejora.
- **Edición de cuestionarios** (árbol de decisión adaptativo): queda **fuera del MVP** por complejidad;
  se documenta como trabajo futuro.
- **Reutilización de DTOs:** se replican manualmente en el panel para no acoplar el módulo a `ifit/`
  (alternativa: extraer un módulo `ifit-api-contracts` compartido — no necesario para algo sencillo).
```