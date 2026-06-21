# IFit — Microservicio Principal

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-orange.svg)](https://www.mysql.com/)

> Motor principal del sistema iFit. Gestiona usuarios, cuestionarios de evaluación, generación de rutinas de entrenamiento con IA y seguridad basada en Keycloak.

---

## Tabla de Contenidos

- [Descripción General](#descripción-general)
- [Arquitectura del Sistema](#arquitectura-del-sistema)
- [Stack Tecnológico](#stack-tecnológico)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Módulos](#módulos)
  - [Auth — Autenticación con Keycloak](#auth--autenticación-con-keycloak)
  - [Questionnaire — Cuestionario de Evaluación](#questionnaire--cuestionario-de-evaluación)
  - [Training — Rutinas y Generación IA](#training--rutinas-y-generación-ia)
  - [User — Gestión de Usuarios](#user--gestión-de-usuarios)
  - [Coach — Tipos de Coach](#coach--tipos-de-coach)
  - [Exercises — Catálogo de Ejercicios](#exercises--catálogo-de-ejercicios)
  - [Notification — Notificaciones por Email](#notification--notificaciones-por-email)
- [Seguridad](#seguridad)
- [Base de Datos](#base-de-datos)
- [Configuración](#configuración)
- [Puesta en Marcha](#puesta-en-marcha)
- [Referencia de Endpoints](#referencia-de-endpoints)

---

## Descripción General

IFit es una API RESTful desarrollada con **Spring Boot 3.4.4** y **Java 21** que actúa como núcleo de la plataforma de fitness personalizado del mismo nombre. Sus responsabilidades principales son:

- Gestionar el ciclo de vida completo del usuario: registro, verificación de email, autenticación vía Keycloak y perfil.
- Dirigir al usuario a través de un **cuestionario de evaluación física** implementado como árbol de decisión.
- Enviar las respuestas del cuestionario al microservicio de IA **Ronnie** para **generar una rutina de entrenamiento personalizada**.
- Persistir y exponer dichas rutinas al cliente (frontend .NET MAUI).

IFit es uno de tres microservicios que componen el sistema:

| Servicio | Rol |
|---|---|
| **ApiGateway** | Punto de entrada único. Enrutamiento, JWT relay, StripPrefix. |
| **IFit** (este) | Lógica de negocio principal, usuarios, cuestionarios, rutinas. |
| **Ronnie** | Motor de IA con LangChain4j + Groq para generar rutinas. |

---

## Arquitectura del Sistema

```
                         ┌────────────────────────────────┐
 Cliente                 │         API GATEWAY             │
 (.NET MAUI)  ──────────►│  Spring Cloud Gateway           │
                         │  Puerto: 8080                   │
                         │  Ruta: /ifit/api/v1/**          │
                         └────────────┬───────────────────┘
                                      │ StripPrefix=3
                         ┌────────────▼───────────────────┐
                         │            IFIT                 │
                         │  Spring Boot 3.4.4 / Java 21    │
                         │  Puerto: 8081                   │
                         └───────┬──────────┬─────────────┘
                                 │          │
              ┌──────────────────▼┐  ┌──────▼────────────────┐
              │     MySQL         │  │       RONNIE           │
              │  Base de datos    │  │  LangChain4j + Groq    │
              │  Puerto: 3306     │  │  Puerto: 8082          │
              └───────────────────┘  └────────────────────────┘
                         │
              ┌──────────▼────────┐
              │     KEYCLOAK      │
              │  OAuth2 + JWT     │
              │  Puerto: 9090     │
              │  Realm: ifit-realm│
              └───────────────────┘
```

Todas las peticiones del frontend pasan por el gateway, que añade el prefijo `/ifit/api/v1` y retransmite el token JWT. IFit actúa como OAuth2 Resource Server: valida los tokens emitidos por Keycloak sin intervención del gateway para ello.

---

## Stack Tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.4.4 |
| Seguridad | Spring Security + OAuth2 Resource Server |
| Persistencia | Spring Data JPA / Hibernate |
| Base de datos | MySQL 8.0+ |
| Identidades | Keycloak 24+ |
| Email | Spring Mail (SMTP Gmail) |
| Documentación | SpringDoc OpenAPI 3 (Swagger UI) |
| Build | Maven 3.9+ |
| Boilerplate | Lombok 1.18.36 |
| Microservicios | Spring Cloud (Eureka client) |
| HTTP Client | RestTemplate (llamadas a Ronnie y Keycloak) |

---

## Estructura del Proyecto

```
ifit/
├── src/main/java/com/uca/juangarcia/ifit/
│   ├── IfitApplication.java
│   ├── config/
│   │   ├── OpenApiConfig.java          # Configuración Swagger/OpenAPI
│   │   ├── RestTemplateConfig.java     # Bean RestTemplate
│   │   └── WebConfig.java             # CORS y MVC
│   ├── exception/                      # Excepciones de dominio custom
│   │   ├── UserIdNotFoundException.java
│   │   ├── ExperienceLevelNotFoundException.java
│   │   ├── CoachModelTypeNotFoundException.java
│   │   ├── QuestionnaireNotFoundException.java
│   │   ├── RoutineNotFoundException.java
│   │   └── model/ErrorResponse.java    # Respuesta de error estándar
│   ├── security/
│   │   └── SpringSecurityConfig.java   # SecurityFilterChain
│   ├── utils/                          # JwtUtils, etc.
│   └── modules/
│       ├── auth/                       # Autenticación y Keycloak
│       ├── user/                       # Usuarios y niveles de experiencia
│       ├── coach/                      # Tipos de coach (CoachModelType)
│       ├── questionnaire/              # Cuestionario de evaluación
│       ├── training/                   # Rutinas y cliente IA
│       ├── exercises/                  # Catálogo de ejercicios
│       └── notification/              # Envío de emails
├── src/main/resources/
│   ├── application.properties
│   ├── data.sql                        # DDL + DML (schema + datos iniciales)
│   └── templates/                     # Plantillas Thymeleaf para emails
└── pom.xml
```

Cada módulo sigue la estructura `controller / dto / mapper / model / repository / service`.

---

## Módulos

### Auth — Autenticación con Keycloak

#### ¿Qué es Keycloak y por qué se usa aquí?

Keycloak es un servidor de identidades y acceso (IAM) que implementa los estándares OAuth 2.0 y OpenID Connect. En iFit, Keycloak es la **única fuente de verdad para credenciales**: almacena contraseñas de usuarios de forma segura y emite tokens JWT firmados.

IFit **no es un servidor de autorización**; es un **Resource Server OAuth2**. Esto significa que:
- No almacena contraseñas directamente (solo las delega a Keycloak).
- Valida los tokens JWT que llegan con cada petición comprobando su firma contra la clave pública publicada por Keycloak en `/.well-known/openid-configuration`.
- Extrae el `subject` (keycloakUserId) y los roles del payload del token.

#### Flujo de Registro

El registro es una operación **doble y transaccional**:

```
Cliente                     IFit                        Keycloak              MySQL
   │                          │                             │                    │
   │── POST /auth/register ──►│                             │                    │
   │   { name, email, pass }  │── POST /admin/users ───────►│                   │
   │                          │   (crea usuario Keycloak)   │                    │
   │                          │◄── 201 Created ─────────────│                    │
   │                          │                             │                    │
   │                          │── INSERT INTO user... ──────────────────────────►│
   │                          │   (crea usuario en BD local)│                    │
   │                          │                             │                    │
   │                          │── Envía email con código    │                    │
   │                          │   de verificación (6 dígitos│                    │
   │                          │   + expiración 15 min)      │                    │
   │◄── 201 { userId, email }──│                             │                    │
```

**Rollback automático**: si la inserción en MySQL falla (p.ej. email duplicado), `AuthenticationService` llama a la API de administración de Keycloak para eliminar el usuario recién creado, garantizando consistencia entre ambos sistemas.

#### Flujo de Verificación de Email

Tras registrarse, el usuario recibe un código de 6 dígitos numéricos por email. Sin verificar, el login devuelve tokens nulos y reenvía el código automáticamente.

```
POST /auth/verify
Body: { "email": "...", "verificationCode": "123456" }

→ Comprueba código en BD y que no haya expirado
→ Marca is_verified = true en la tabla user
→ Devuelve tokens JWT (igual que un login)
```

#### Flujo de Login

```
POST /auth/login
Body: { "email": "...", "password": "..." }

IFit llama a Keycloak:
  POST http://localhost:9090/realms/ifit-realm/protocol/openid-connect/token
  grant_type=password & client_id=springboot-ifit-client & client_secret=... & username=... & password=...

Keycloak devuelve:
  { "access_token": "eyJ...", "refresh_token": "eyJ...", "expires_in": 300, ... }

IFit extrae el keycloakUserId del access_token y lo añade a la respuesta:
  { "accessToken": "...", "refreshToken": "...", "keycloakUserId": "uuid" }
```

El `access_token` tiene duración corta (~5 min). El cliente lo incluye en cada petición como `Authorization: Bearer <access_token>`.

#### Flujo de Refresco de Token

```
POST /auth/refresh
Body: { "refreshToken": "eyJ..." }

IFit llama a Keycloak:
  grant_type=refresh_token & refresh_token=...

Devuelve nuevos access_token y refresh_token.
```

#### Flujo de Logout

```
POST /auth/logout
Body: { "refreshToken": "eyJ..." }

IFit llama al endpoint de logout de Keycloak, invalidando la sesión en el servidor.
El refresh_token queda revocado; el access_token caduca por tiempo.
```

#### Endpoints del módulo Auth

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | `/auth/login` | Público | Login con email/contraseña. Devuelve tokens JWT. |
| POST | `/auth/register` | Público | Registro nuevo usuario. Envía email de verificación. |
| POST | `/auth/refresh` | Público | Renueva tokens con refresh_token. |
| POST | `/auth/logout` | Público | Invalida sesión en Keycloak. |
| POST | `/auth/verify` | Público | Verifica email con código de 6 dígitos. |
| POST | `/auth/resend-verification` | Público | Reenvía código de verificación. |
| GET | `/auth/user` | ADMIN | Lista usuarios de Keycloak. |
| GET | `/auth/user/{id}` | ADMIN | Obtiene usuario de Keycloak por ID. |
| PUT | `/auth/user/{id}` | ADMIN | Actualiza usuario en Keycloak. |
| DELETE | `/auth/user/{id}` | ADMIN | Elimina usuario de Keycloak. |

---

### Questionnaire — Cuestionario de Evaluación

El módulo de cuestionarios es el punto de entrada a la experiencia personalizada de iFit. Antes de generar una rutina, el usuario debe completar un cuestionario que recopila su perfil físico y sus objetivos.

#### Diseño como Árbol de Decisión

El cuestionario no es un formulario lineal fijo. Está implementado como un **grafo dirigido acíclico** donde cada opción de respuesta apunta a la siguiente pregunta mediante el campo `next_question_id`. Cuando ese campo es `NULL`, la sesión concluye.

```
Entidades del árbol:

  Questionnaire ──(first_question_id)──► Question
                                             │
                                        QuestionOption[]
                                             │
                                      next_question_id ──► Question (o NULL = FIN)
```

Este mecanismo permite ramificaciones: la opción 34 de la pregunta Q9 ("Actualmente entreno de forma regular") lleva a Q11 (pregunta sobre experiencia en fuerza), mientras que las otras opciones de Q9 saltan directamente a Q12.

#### Árbol de Preguntas

Existen **65 preguntas** distribuidas en 5 subtrees:

| Árbol | Preguntas | Opciones | Coach |
|---|---|---|---|
| General (común) | Q1 – Q13 | IDs 1–47 | Todos |
| Ronnie | Q14 – Q26 | IDs 48–89 | Musculación |
| Serena | Q27 – Q39 | IDs 90–131 | Bienestar / Fitness |
| Kael | Q40 – Q52 | IDs 132–174 | Calistenia |
| Eliud | Q53 – Q65 | IDs 175–215 | Running / Resistencia |

El árbol **General** cubre datos transversales válidos para cualquier coach:

```
Q1  ¿Objetivo principal?            → Q2
Q2  ¿Adaptaciones de ejercicio?     → Q3  (con texto libre si requiere cuidado)
Q3  ¿Frecuencia semanal?            → Q4
Q4  ¿Lugar de entrenamiento?        → Q5
Q5  ¿Rango de edad?                 → Q6
Q6  ¿Peso (kg)?                     → Q7  (input numérico)
Q7  ¿Altura (cm)?                   → Q8  (input numérico)
Q8  ¿Nivel de actividad diaria?     → Q9
Q9  ¿Experiencia previa?            → Q10 (o Q11 si entrena regularmente)
Q10 ¿Comodidad con rutinas?         → Q12
Q11 ¿Experiencia con fuerza?        → Q12  (solo si vino de Q9 opción 5)
Q12 ¿Duración preferida?            → Q13
Q13 ¿Preferencias alimentarias?     → NULL (FIN)
```

Cada árbol específico de coach repite una estructura similar adaptada a su especialidad. Por ejemplo, Ronnie añade preguntas sobre equipamiento y grupos musculares a priorizar; Kael pregunta cuántas dominadas y fondos puede hacer el usuario; Eliud pregunta sobre el ritmo de carrera y objetivo de carrera.

#### Tipos de Pregunta

| Tipo | Comportamiento |
|---|---|
| `MULTIPLE_CHOICE` | El usuario selecciona una opción predefinida. |
| `NUMERIC` | La opción tiene `requires_text_input = TRUE`; el usuario introduce un número. |
| `TEXT_INPUT` | Respuesta de texto libre (usado en pregunta de alimentación y adaptaciones). |

Para preguntas numéricas o de texto, la opción actúa como "envoltorio" que activa el campo adicional `additionalText` en el DTO de respuesta.

#### Cuestionarios Predefinidos

La tabla `questionnaire` combina `(coach_model_type_id, experience_level_id)` para ofrecer cuestionarios especializados:

| ID | Nombre | Coach | Nivel |
|---|---|---|---|
| 1 | Ronnie - Fuerza para Principiantes | Ronnie | Principiante |
| 2 | Serena - Bienestar para Principiantes | Serena | Principiante |
| 3 | Eliud - Resistencia Intermedia | Eliud | Intermedio |
| 4 | Kael - Calistenia Avanzada | Kael | Avanzado |
| 5 | Evaluación General de Fitness | — | — |
| 6 | Eliud - Resistencia para Principiantes | Eliud | Principiante |
| 7 | Eliud - Resistencia Avanzada | Eliud | Avanzado |
| 8 | Kael - Calistenia para Principiantes | Kael | Principiante |
| 9 | Kael - Calistenia Intermedia | Kael | Intermedio |

El cuestionario 5 (genérico) no está asociado a ningún coach ni nivel concreto. Los cuestionarios con coach pero sin nivel específico permiten mayor flexibilidad en la selección.

#### Sesión de Cuestionario

Una **sesión** (`QuestionnaireResponse`) es una instancia de usuario respondiendo un cuestionario. El flujo típico es:

```
1. POST /{userId}/start/{questionnaireId}
   → Crea QuestionnaireResponse con status=ACTIVE
   → Devuelve la primera pregunta con sus opciones

2. POST /responses/{responseId}/answer
   Body: { questionId, selectedOptionId, additionalText }
   → Persiste UserAnswer
   → Lee next_question_id de la opción elegida
   → Si next_question_id = NULL → marca sesión como COMPLETED
   → Devuelve la siguiente pregunta (o señal de finalización)

3. POST /responses/{responseId}/previous    (retroceso)
   → Elimina la última UserAnswer
   → Devuelve la pregunta anterior para corregir

4. GET /responses/{responseId}/summary
   → Devuelve lista de { pregunta, opción elegida, texto adicional }
   → Usado por RoutineService para construir el prompt de IA
```

#### Opción "Prefiero no responder"

Cada una de las 65 preguntas tiene una opción adicional "Prefiero no responder" (IDs 216–280). Estas opciones tienen `requires_text_input = FALSE` y `next_question_id` igual al de las opciones hermanas (avance normal). En `RoutineService.buildRoutinePrompt`, las respuestas con ese texto se convierten a `[No respondida]` antes de enviar el prompt a Ronnie, y los coaches tienen instrucción explícita de ignorar ese parámetro y usar un valor por defecto razonable.

#### Endpoints del módulo Questionnaire

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/questionnaires` | Lista todos los cuestionarios habilitados. |
| GET | `/questionnaires/{id}` | Obtiene cuestionario completo por ID. |
| GET | `/questionnaires/{id}/with-first-question` | Cuestionario + primera pregunta en una sola llamada. |
| GET | `/questionnaires/coach/{coachId}/experience-level/{levelId}` | Busca cuestionario por coach y nivel. |
| POST | `/questionnaires` | Crea nuevo cuestionario (admin). |
| PUT | `/questionnaires/{id}` | Actualiza cuestionario (admin). |
| DELETE | `/questionnaires/{id}` | Elimina cuestionario (admin). |
| POST | `/questionnaires/{userId}/start/{questionnaireId}` | Inicia sesión de cuestionario. |
| POST | `/questionnaires/responses/{responseId}/answer` | Registra respuesta y avanza. |
| POST | `/questionnaires/responses/{responseId}/previous` | Retrocede a la pregunta anterior. |
| GET | `/questionnaires/responses/{responseId}/summary` | Resumen completo de una sesión. |
| GET | `/questionnaires/responses/my-responses` | Todas las sesiones del usuario autenticado. |
| GET | `/questionnaires/responses/my-completed-responses` | Solo sesiones completadas. |
| GET | `/questionnaires/responses/my-active-responses` | Solo sesiones en curso. |

---

### Training — Rutinas y Generación IA

Este módulo es el punto de conexión entre el cuestionario completado y el motor de IA. Gestiona el ciclo de vida completo de las rutinas de entrenamiento y orquesta la llamada al microservicio Ronnie.

#### Modelo de Datos

Una rutina (`Routine`) tiene la siguiente jerarquía:

```
Routine
 ├── userId (referencia al usuario)
 ├── name, description, trainingDays
 ├── isActive, isCompleted
 └── List<RoutineDay>
      ├── dayNumber (1–7), dayName, description, isCompleted
      └── List<RoutineExercise>
           ├── exerciseName, sets, reps, restSeconds
           ├── notes, orderIndex
           └── (relación con Exercise del catálogo)
```

#### CoachType — Los 5 Coaches de IA

El enum `CoachType` define los cinco coaches disponibles. Cada uno aporta su especialidad mediante un `systemContext` que se inyecta en el prompt enviado a Ronnie:

| Coach | Especialidad | Endpoint en Ronnie |
|---|---|---|
| `MASTER` | Planificador generalista (usa su propio `@SystemMessage`) | `/master/generate-routine` |
| `RONNIE` | Hipertrofia y fuerza muscular (inspirado en Ronnie Coleman) | `/ronnie/generate-routine` |
| `ELIUD` | Running, cardio y rendimiento aeróbico | `/eliud/generate-routine` |
| `SERENA` | Fitness femenino, tonificación y bienestar | `/serena/generate-routine` |
| `KAEL` | Calistenia y street workout | `/kael/generate-routine` |

Para `MASTER`, `systemContext` es `null`; su comportamiento viene definido en el `@SystemMessage` de la interfaz `Master` en el microservicio Ronnie. Para los demás coaches, `systemContext` se incluye en el prompt bajo la sección `ROL DEL ENTRENADOR`.

#### IFitAIClient — Cliente HTTP hacia Ronnie

`IFitAIClient` es el componente `@Component` encargado de toda la comunicación con el microservicio Ronnie. Usa `RestTemplate` y maneja errores 4xx/5xx lanzando `RuntimeException` con el mensaje original de Ronnie.

Expone dos operaciones:

```java
// Genera una rutina llamando al coach correspondiente
RoutineResponseDto generateRoutine(int memoryId, String prompt,
                                   String keycloakUserId, CoachType coachType)

// Obtiene el mayor memoryId en uso (para asignar uno nuevo a cada conversación)
IFitAIMaxMemoryIdResponseDto getMaxMemoryId()
```

El `memoryId` permite a LangChain4j mantener historial de conversación en Ronnie. Cada generación de rutina recibe un `memoryId` único derivado del máximo actual + 1, evitando colisiones.

#### Flujo Completo de Generación de Rutina

```
Cliente  →  POST /routines/generate
            { userId, responseId, coachType, note }

RoutineService.generateRoutine():
  1. Valida que el usuario existe (AppUserRepository)
  2. Obtiene el keycloakUserId del usuario
  3. Llama a questionnaireService.getResponseSummary(responseId)
     → devuelve lista de { pregunta, opción elegida, texto adicional }
  4. Construye el prompt en buildRoutinePrompt():
     - Encabezado con nombre del usuario y coach
     - Si coachType != MASTER: sección "ROL DEL ENTRENADOR" con systemContext
     - Por cada pregunta respondida:
         "- <texto pregunta>\n  Respuesta: <opción elegida o [No respondida]>\n"
       + si tiene texto adicional: "  Detalle: <additionalText>\n"
     - Si request.note != null: sección "NOTA ESPECIAL DEL USUARIO"
  5. Obtiene el siguiente memoryId: aiClient.getMaxMemoryId() + 1
  6. Llama a aiClient.generateRoutine(memoryId, prompt, keycloakUserId, coachType)
     → HTTP POST a Ronnie: /{coachType}/generate-routine
        Body: { memoryId, message: <prompt>, keycloakUserId }
  7. Ronnie devuelve IFitAIRoutineResponseDto con estructura de rutina
  8. RoutineService transforma y persiste la rutina en MySQL
  9. Devuelve RoutineResponseDto al cliente
```

#### Otros Endpoints de Training

Además de la generación, el controlador expone gestión completa de rutinas:

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/routines/generate` | Genera rutina personalizada con IA. |
| POST | `/routines` | Crea rutina manualmente. |
| GET | `/routines` | Lista todas las rutinas. |
| GET | `/routines/paginated` | Lista paginada con ordenamiento. |
| GET | `/routines/{id}` | Obtiene rutina por ID. |
| GET | `/routines/user/{userId}` | Rutinas de un usuario. |
| GET | `/routines/user/{userId}/paginated` | Rutinas paginadas de un usuario. |
| GET | `/routines/user/{userId}/active` | Solo rutinas activas de un usuario. |
| GET | `/routines/user/{userId}/count-active` | Número de rutinas activas. |
| GET | `/routines/{routineId}/day/{day}` | Detalle de un día específico. |
| PUT | `/routines/{id}` | Actualiza rutina completa. |
| PATCH | `/routines/{id}/toggle-active` | Activa o desactiva rutina. |
| POST | `/routines/{routineId}/day/{day}/complete` | Marca día como completado. |
| POST | `/routines/{routineId}/complete` | Marca rutina completa. |
| DELETE | `/routines/{id}` | Elimina rutina (irreversible). |

---

### User — Gestión de Usuarios

El módulo de usuario gestiona la entidad `AppUser`, que es la representación local del usuario en la base de datos de IFit (separada del registro en Keycloak).

#### Entidad AppUser

Campos principales de la tabla `user`:

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | BIGINT PK | Identificador local. |
| `name` | VARCHAR | Nombre completo. |
| `email` | VARCHAR UNIQUE | Email usado como username en Keycloak. |
| `password` | VARCHAR | Hash BCrypt (copia local por compatibilidad). |
| `keycloak_user_id` | VARCHAR | UUID del usuario en Keycloak. |
| `is_verified` | BOOLEAN | True si verificó el email. |
| `verification_code` | VARCHAR | Código temporal de 6 dígitos. |
| `verification_code_expires_at` | DATETIME | Expiración del código (15 min). |
| `is_registration_complete` | BOOLEAN | True cuando completó el onboarding. |
| `role_id` | FK → approle | Rol: USER o ADMIN. |
| `coachmodeltype_id` | FK → coachmodeltype | Coach de IA asignado. |
| `experiencelevel_id` | FK → experiencelevel | Nivel de experiencia asignado. |

#### Endpoints del módulo User

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/users` | Lista todos los usuarios. |
| GET | `/users/paginated` | Lista paginada con ordenamiento. |
| GET | `/users/{id}` | Obtiene usuario por ID. |
| GET | `/users/email/{email}` | Busca usuario por email. |
| GET | `/users/exists/email/{email}` | Verifica si un email está registrado (boolean). |
| POST | `/users` | Crea usuario (uso interno/admin). |
| PUT | `/users/{id}` | Actualiza datos del usuario. |
| PATCH | `/users/{userId}/assign-coach/{coachId}` | Asigna tipo de coach al usuario. |
| PATCH | `/users/{userId}/assign-experience/{levelId}` | Asigna nivel de experiencia. |
| PATCH | `/users/{userId}/complete-registration` | Marca el onboarding como completado. |
| DELETE | `/users/{id}` | Elimina usuario. |

#### Niveles de Experiencia

La tabla `experiencelevel` define los niveles que un usuario puede tener (Principiante, Intermedio, Avanzado). Se accede vía `ExperienceLevelController` bajo `/experience-levels`:

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/experience-levels` | Lista todos los niveles. |
| GET | `/experience-levels/{id}` | Por ID. |
| POST | `/experience-levels` | Crea nivel (admin). |
| PATCH | `/experience-levels/{id}` | Actualiza nivel (admin). |
| DELETE | `/experience-levels/{id}` | Elimina nivel (admin). |

---

### Coach — Tipos de Coach

La tabla `coachmodeltype` almacena los coaches disponibles en la plataforma. Esta entidad es el registro maestro de coaches; el comportamiento real de cada coach se define en el microservicio Ronnie.

Campos: `id`, `name` (único), `description`, `emoji_character`, `enabled` (soft-delete), `created_at`, `updated_at`.

**Soft delete**: el endpoint `DELETE /coach-models/{id}` no elimina el registro, sino que pone `enabled = false`. Los modelos deshabilitados dejan de aparecer en la lista pública pero siguen funcionando para usuarios que ya los tenían asignados.

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| GET | `/coach-models` | Autenticado | Lista coaches habilitados. |
| GET | `/coach-models/all` | ADMIN | Lista todos (incluye deshabilitados). |
| GET | `/coach-models/{id}` | Autenticado | Por ID. |
| GET | `/coach-models/name/{name}` | Autenticado | Por nombre exacto. |
| POST | `/coach-models` | ADMIN | Crea nuevo tipo de coach. |
| PUT | `/coach-models/{id}` | ADMIN | Actualiza tipo de coach. |
| DELETE | `/coach-models/{id}` | ADMIN | Deshabilita (soft delete). |
| PATCH | `/coach-models/{id}/enable` | ADMIN | Rehabilita coach deshabilitado. |

---

### Exercises — Catálogo de Ejercicios

El catálogo de ejercicios es una copia de los ejercicios importados desde el microservicio Ronnie. Sirve al frontend para mostrar fichas de ejercicio con imágenes.

Las **imágenes** se sirven directamente desde Ronnie a través del gateway:
```
GET /ifit/api/v1/exercise-images/{carpeta}/{archivo}.jpg
```

El controlador expone dos endpoints de solo lectura:

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/exercises` | Lista paginada con filtros opcionales por `level`, `category`, `equipment`, `muscle`. |
| GET | `/exercises/{id}` | Detalle completo: instrucciones, músculos trabajados, imágenes. |

Parámetros de filtro del listado: `level` (principiante/intermedio/avanzado), `category` (fuerza/estiramiento/cardio/…), `equipment` (solo_cuerpo/barra/mancuernas/…), `muscle` (búsqueda parcial, ej. "pecho").

---

### Notification — Notificaciones por Email

El módulo de notificación gestiona el envío de emails transaccionales. El componente principal es `AppEmailService`, que usa **Spring Mail** con SMTP de Gmail.

El email más relevante del sistema es el de **verificación de cuenta**: se genera un código numérico de 6 dígitos, se almacena en la base de datos con una expiración de 15 minutos, y se envía al usuario mediante una plantilla HTML Thymeleaf.

La verificación de email se invoca automáticamente:
- En el registro: se envía tras crear el usuario.
- En el login sin verificar: IFit reenvía el código automáticamente.
- En `/auth/resend-verification`: el usuario puede solicitar un nuevo código.

El controlador `AppEmailController` existe como stub en `/appemail` pero actualmente no expone endpoints públicos; los envíos son operaciones internas del servicio de autenticación.

---

## Seguridad

### Configuración de Spring Security

`SpringSecurityConfig` define una `SecurityFilterChain` stateless (sin sesiones HTTP) con las siguientes reglas:

**Endpoints públicos** (no requieren token):
- `/v3/api-docs/**` — OpenAPI spec
- `/swagger-ui/**`, `/swagger-ui.html` — Documentación interactiva
- `/auth/**` — Todos los endpoints de autenticación
- `/exercise-images/**` — Imágenes de ejercicios

**Resto de endpoints**: requieren token JWT válido (`anyRequest().authenticated()`).

### Validación de JWT

IFit actúa como **OAuth2 Resource Server**. Spring Security valida cada token entrante:
1. Descarga la clave pública de Keycloak desde `/.well-known/openid-connect/certs`.
2. Verifica la firma del JWT.
3. Comprueba `iss` (issuer = `http://localhost:9090/realms/ifit-realm`) y `exp` (expiración).
4. Construye el `Authentication` con los roles extraídos mediante `JwtAuthenticationConverter`.

### Extracción de Roles

Los roles de Keycloak se extraen del claim `realm_access.roles` del JWT. El rol `admin_client_role` otorga acceso a operaciones administrativas protegidas con `@PreAuthorize("hasRole('admin_client_role')")`.

### Extracción del UserId

`JwtUtils.extractUserId(request, objectMapper)` lee el campo `sub` del payload del JWT para obtener el `keycloakUserId`. Este ID es el vínculo entre el usuario en Keycloak y su rutina en Ronnie.

### CSRF

CSRF está deshabilitado. Es la práctica estándar para APIs REST stateless donde el cliente gestiona el token explícitamente en la cabecera `Authorization`.

---

## Base de Datos

### Inicialización

El fichero `data.sql` combina DDL (DROP/CREATE TABLE) y DML (INSERT) en un único script. Spring Boot lo ejecuta en cada arranque (`spring.sql.init.mode=always`). Todos los INSERT usan `ON DUPLICATE KEY UPDATE` para que el script sea **idempotente**: re-ejecutarlo no destruye datos existentes.

`spring.jpa.hibernate.ddl-auto=update` está activo, pero como el DDL lo gestiona `data.sql`, Hibernate solo añade columnas nuevas si fuera necesario.

### Tablas Principales

| Tabla | Descripción |
|---|---|
| `approle` | Roles: USER, ADMIN. |
| `experiencelevel` | Niveles de experiencia fitness. |
| `coachmodeltype` | Tipos de coach de IA (nombre, descripción, enabled). |
| `user` | Usuario local. FK a approle, coachmodeltype, experiencelevel. |
| `questionnaire` | Cuestionario: FK a coachmodeltype, experiencelevel, first_question. |
| `question` | Pregunta individual con tipo (MULTIPLE_CHOICE, NUMERIC, TEXT_INPUT). |
| `question_option` | Opción de respuesta: text, next_question_id (FK nullable), requires_text_input. |
| `questionnaire_response` | Sesión de usuario respondiendo un cuestionario. |
| `user_answer` | Respuesta de usuario a una pregunta (FK a sesión, pregunta, opción). |
| `routine` | Rutina de entrenamiento. FK a user. |
| `routine_day` | Día de entrenamiento dentro de una rutina. |
| `routine_exercise` | Ejercicio dentro de un día: nombre, series, reps, descanso. |
| `exercise` | Catálogo estático de ejercicios. |

---

## Configuración

Archivo: `src/main/resources/application.properties`

```properties
# Aplicación
spring.application.name=ifit
server.port=8081

# Base de datos
spring.datasource.url=jdbc:mysql://localhost:3306/ifit
spring.datasource.username=root
spring.datasource.password=root
spring.sql.init.mode=always
spring.jpa.hibernate.ddl-auto=update

# Email (SMTP Gmail)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=adminifit96@gmail.com
spring.mail.password=<app-password>

# OAuth2 Resource Server — Keycloak
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9090/realms/ifit-realm
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:9090/realms/ifit-realm/protocol/openid-connect/certs

# Keycloak admin (para register/delete users)
keycloak.client-id=springboot-ifit-client
keycloak.client-secret=<secret>
keycloak.token-url=http://localhost:9090/realms/ifit-realm/protocol/openid-connect/token
keycloak.auth-server-url=http://localhost:9090
keycloak.realm=ifit-realm

# Ronnie (microservicio IA)
ronnie.service.url=http://localhost:8082

# Eureka (service discovery)
eureka.instance.instance-id=${spring.application.name}:${server.port}
```

---

## Puesta en Marcha

### Prerrequisitos

- Java 21+
- Maven 3.9+
- MySQL 8.0+ con base de datos `ifit` creada
- Keycloak 24+ arrancado en `localhost:9090` con realm `ifit-realm` configurado
- Microservicio Ronnie arrancado en `localhost:8082` (para endpoints de generación IA)

### Crear la base de datos

```sql
CREATE DATABASE ifit CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Spring Boot ejecutará `data.sql` automáticamente en el primer arranque, creando todas las tablas e insertando los datos iniciales (preguntas, opciones, cuestionarios, coaches, niveles de experiencia).

### Compilar y arrancar

```bash
cd ifit
mvn clean package -DskipTests
mvn spring-boot:run
```

La API queda disponible en: `http://localhost:8081`

### Documentación interactiva (Swagger UI)

```
http://localhost:8081/swagger-ui.html
```

---

## Referencia de Endpoints

Base URL a través del gateway: `http://localhost:8080/ifit/api/v1`  
Base URL directa: `http://localhost:8081`

### Autenticación

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| POST | `/auth/login` | — | Login. Devuelve access_token, refresh_token, keycloakUserId. |
| POST | `/auth/register` | — | Registro. Envía código de verificación por email. |
| POST | `/auth/refresh` | — | Renueva tokens. |
| POST | `/auth/logout` | — | Cierra sesión en Keycloak. |
| POST | `/auth/verify` | — | Verifica email con código de 6 dígitos. |
| POST | `/auth/resend-verification` | — | Reenvía código de verificación. |

### Usuarios

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| GET | `/users` | JWT | Lista usuarios. |
| GET | `/users/paginated` | JWT | Lista paginada. |
| GET | `/users/{id}` | JWT | Por ID. |
| GET | `/users/email/{email}` | JWT | Por email. |
| GET | `/users/exists/email/{email}` | JWT | Comprueba existencia. |
| POST | `/users` | JWT | Crear usuario. |
| PUT | `/users/{id}` | JWT | Actualizar. |
| PATCH | `/users/{userId}/assign-coach/{coachId}` | JWT | Asignar coach. |
| PATCH | `/users/{userId}/assign-experience/{levelId}` | JWT | Asignar nivel. |
| PATCH | `/users/{userId}/complete-registration` | JWT | Completar onboarding. |
| DELETE | `/users/{id}` | JWT | Eliminar. |

### Cuestionarios

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| GET | `/questionnaires` | JWT | Lista cuestionarios habilitados. |
| GET | `/questionnaires/{id}` | JWT | Por ID. |
| GET | `/questionnaires/{id}/with-first-question` | JWT | Con primera pregunta incluida. |
| GET | `/questionnaires/coach/{cId}/experience-level/{lId}` | JWT | Por coach y nivel. |
| POST | `/questionnaires` | JWT | Crear cuestionario. |
| PUT | `/questionnaires/{id}` | JWT | Actualizar. |
| DELETE | `/questionnaires/{id}` | JWT | Eliminar. |
| POST | `/questionnaires/{userId}/start/{questionnaireId}` | JWT | Iniciar sesión. |
| POST | `/questionnaires/responses/{responseId}/answer` | JWT | Responder pregunta. |
| POST | `/questionnaires/responses/{responseId}/previous` | JWT | Retroceder. |
| GET | `/questionnaires/responses/{responseId}/summary` | JWT | Resumen de sesión. |
| GET | `/questionnaires/responses/my-responses` | JWT | Todas mis sesiones. |
| GET | `/questionnaires/responses/my-completed-responses` | JWT | Sesiones completadas. |
| GET | `/questionnaires/responses/my-active-responses` | JWT | Sesiones en curso. |

### Rutinas

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| POST | `/routines/generate` | JWT | Genera rutina personalizada con IA. |
| POST | `/routines` | JWT | Crea rutina manual. |
| GET | `/routines` | JWT | Lista todas. |
| GET | `/routines/paginated` | JWT | Paginada. |
| GET | `/routines/{id}` | JWT | Por ID. |
| GET | `/routines/user/{userId}` | JWT | Por usuario. |
| GET | `/routines/user/{userId}/paginated` | JWT | Por usuario, paginada. |
| GET | `/routines/user/{userId}/active` | JWT | Solo activas. |
| GET | `/routines/user/{userId}/count-active` | JWT | Contador activas. |
| GET | `/routines/{routineId}/day/{day}` | JWT | Día específico. |
| PUT | `/routines/{id}` | JWT | Actualizar. |
| PATCH | `/routines/{id}/toggle-active` | JWT | Activar/desactivar. |
| POST | `/routines/{routineId}/day/{day}/complete` | JWT | Marcar día completado. |
| POST | `/routines/{routineId}/complete` | JWT | Marcar rutina completada. |
| DELETE | `/routines/{id}` | JWT | Eliminar. |

### Coaches y Niveles

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| GET | `/coach-models` | JWT | Lista coaches habilitados. |
| GET | `/coach-models/{id}` | JWT | Por ID. |
| POST | `/coach-models` | ADMIN | Crear. |
| PUT | `/coach-models/{id}` | ADMIN | Actualizar. |
| DELETE | `/coach-models/{id}` | ADMIN | Deshabilitar (soft delete). |
| PATCH | `/coach-models/{id}/enable` | ADMIN | Rehabilitar. |
| GET | `/experience-levels` | JWT | Lista niveles. |
| GET | `/experience-levels/{id}` | JWT | Por ID. |
| POST | `/experience-levels` | JWT | Crear. |
| PATCH | `/experience-levels/{id}` | JWT | Actualizar. |
| DELETE | `/experience-levels/{id}` | JWT | Eliminar. |

### Ejercicios

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| GET | `/exercises` | JWT | Catálogo paginado con filtros. |
| GET | `/exercises/{id}` | JWT | Detalle de ejercicio. |

---

## Autor

**Juan García Candón**  
Universidad de Cádiz — Escuela Superior de Ingeniería  
Trabajo Final de Grado (TFG), 2024–2025
