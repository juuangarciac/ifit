# IFit — Microservicio Principal

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-green.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-orange.svg)](https://www.mysql.com/)

> Motor principal del sistema iFit. Gestiona usuarios, cuestionarios de evaluación, generación de rutinas de entrenamiento con IA y seguridad basada en Keycloak.

> [!NOTE]
> **Capa de Documentación: Módulo de Dominio y Negocio Principal (Nivel Intermedio)**  
> Este documento detalla la lógica de negocio central de iFit: ciclos de vida transaccionales con Keycloak, flujos del cuestionario adaptativo y persistencia del catálogo y rutinas. Para la vista general y orquestación del sistema completo, consulta el [README.md del proyecto general](../../README.md).

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

IFit es una API RESTful desarrollada con **Spring Boot 3.5.7** y **Java 21** que actúa como núcleo de la plataforma de fitness personalizado del mismo nombre. Sus responsabilidades principales son:

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
                         │  Spring Boot 3.5.7 / Java 21    │
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
| Framework | Spring Boot 3.5.7 |
| Seguridad | Spring Security + OAuth2 Resource Server |
| Persistencia | Spring Data JPA / Hibernate |
| Base de datos | MySQL 8.0+ |
| Identidades | Keycloak 23.0 |
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

**Autenticación (públicos):**

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/auth/login` | Login con email/contraseña. Devuelve tokens JWT (o NULL si email no verificado). |
| POST | `/auth/register` | Registro nuevo usuario. Envía email de verificación (6 dígitos, 15 min expira). |
| POST | `/auth/verify` | Verifica email con código. Marca como verificado y realiza login automático. |
| POST | `/auth/resend-verification` | Reenvía código de verificación por email. |
| POST | `/auth/refresh` | Renueva tokens con refresh_token (sin consulta a BD). |
| POST | `/auth/logout` | Invalida sesión en Keycloak. El refresh_token queda revocado. |

**Gestión de Usuarios Keycloak (ADMIN):**

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/auth/user/search` | Lista todos los usuarios en Keycloak. |
| GET | `/auth/user/search/{username}` | Busca usuario por username (email). |
| POST | `/auth/user/create` | Crea usuario en Keycloak. |
| PUT | `/auth/user/update/{userId}` | Actualiza usuario en Keycloak. |
| DELETE | `/auth/user/delete/{userId}` | Elimina usuario en Keycloak. |

---

### Questionnaire — Cuestionario de Evaluación

El módulo de cuestionarios es el punto de entrada a la experiencia personalizada de iFit. Implementa un **cuestionario adaptativo basado en árbol de decisión** que guía al usuario desde la selección inicial de coach y nivel de experiencia, hasta una evaluación física personalizada. Los datos recopilados alimentan al motor de IA (Ronnie) para generar rutinas de entrenamiento.

#### 1. Arquitectura: Selección Coach + Nivel → Cuestionario

El flujo comienza **fuera** del módulo Questionnaire:

```
Usuario en MAUI
  ↓
1. GET /coach-models           (lista coaches: Ronnie, Serena, Kael, Eliud)
2. GET /experience-levels      (lista niveles: Principiante, Intermedio, Avanzado)
  ↓
Usuario selecciona: Ronnie + Principiante
  ↓
GET /questionnaires/coach/{ronnie_id}/experience-level/{principiante_id}
  ├─ QuestionnaireRepository.findByCoachModelTypeAndExperienceLevel()
  └─ Retorna: QuestionnaireDto { id: 1, name: "Ronnie - Fuerza para Principiantes", ... }
  ↓
POST /questionnaires/{userId}/start/{questionnaire_id}
  ├─ Crea QuestionnaireResponse (sesión)
  ├─ Obtiene firstQuestion de Questionnaire
  └─ Retorna: QuestionnaireResponseDto { responseId: 456, currentQuestion: {...}, ... }
  ↓
Ciclo: responde pregunta → siguiente pregunta → ... → completada
```

#### 2. Modelos/Entidades

##### Questionnaire (Template del Cuestionario)

```java
public class Questionnaire {
    Long id;                              // PK
    String name;                          // Nombre único (ej: "Ronnie - Fuerza para Principiantes")
    String description;                   // Descripción detallada
    CoachModelType coachModelType;        // FK a Coach (nullable - puede ser genérico)
    ExperienceLevel experienceLevel;      // FK a Nivel (nullable)
    Question firstQuestion;               // FK a Question - punto de entrada al árbol
    Boolean isEnabled;                    // Soft delete (default: true)
    LocalDateTime createdAt;              // Auto
    LocalDateTime updatedAt;              // Auto
}
```

**Combinación única**: `(coachModelType.id, experienceLevel.id)` → permite múltiples cuestionarios por coach/nivel

##### Question (Nodo del Árbol)

```java
public class Question {
    Long id;                              // PK
    String text;                          // Texto de la pregunta
    QuestionType type;                    // Enum: BINARY, MULTIPLE_CHOICE, TEXT_INPUT, NUMERIC, SCALE
    List<QuestionOption> options;         // 1:N relación
    Boolean isEnabled;                    // (default: true)
    LocalDateTime createdAt;              // Auto
}
```

**QuestionType enum:**
- `BINARY` → Sí/No
- `MULTIPLE_CHOICE` → Opciones predefinidas (lista)
- `TEXT_INPUT` → Texto libre
- `NUMERIC` → Número (con validación de rango)
- `SCALE` → Escala (1-10, etc.)

##### QuestionOption (Opción → Navegación)

```java
public class QuestionOption {
    Long id;                              // PK
    Question question;                    // FK - pregunta padre
    String text;                          // Texto de la opción (ej: "Ganar músculo")
    Question nextQuestion;                // FK nullable - LLAVE DEL ÁRBOL DE DECISIÓN
    Integer displayOrder;                 // Orden de visualización
    Boolean requiresTextInput;            // ¿Requiere entrada adicional?
    String textInputPrompt;               // Indicación para usuario (ej: "Especifique peso:")
    String textInputPlaceholder;          // Placeholder del input (ej: "ej: 75 kg")
}
```

**Navegación adaptativa:** El campo `nextQuestion` define dinámicamente el flujo:
- Si `nextQuestion != NULL` → siguiente pregunta
- Si `nextQuestion == NULL` → **fin del cuestionario**

Ejemplo:
```
Q9 "¿Experiencia previa?"
  ├─ Opción 30: "Entrenamiento regular" → nextQuestion = Q11 (pregunta específica para experimentados)
  └─ Opción 31: "Principiante"          → nextQuestion = Q12 (salta Q11)
```

##### QuestionnaireResponse (Sesión de Usuario)

```java
public class QuestionnaireResponse {
    Long id;                              // PK - ID de sesión
    AppUser user;                         // FK - usuario respondiendo
    Questionnaire questionnaire;          // FK - cuestionario en curso
    List<UserAnswer> answers;             // 1:N - respuestas registradas
    LocalDateTime startedAt;              // Auto: timestamp inicio
    LocalDateTime completedAt;            // Null si activa, timestamp si completada
    Boolean isCompleted;                  // (default: false)
    Boolean isActive;                     // (default: true) - para soft delete
}
```

**Métodos helper:**
```java
public void addAnswer(UserAnswer answer) { ... }
public void removeAnswer(UserAnswer answer) { ... }
public void markAsCompleted() { 
    isCompleted = true; 
    completedAt = LocalDateTime.now();
}
```

##### UserAnswer (Respuesta Individual)

```java
public class UserAnswer {
    Long id;                              // PK
    QuestionnaireResponse response;       // FK - sesión padre
    Question question;                    // FK - pregunta respondida
    QuestionOption selectedOption;        // FK - opción elegida
    String additionalText;                // Null si no se requería, o texto proporcionado
    String aiGeneratedDescription;        // Reservado para futura IA (actualmente null)
    LocalDateTime answeredAt;             // Auto
}
```

#### 3. DTOs (Data Transfer Objects)

##### DTOs de Cuestionario

```java
// Cuestionario completo
record QuestionnaireDto(
    Long id,
    String name,
    String description,
    String coachModelTypeName,            // Nombre coach o null
    String coachModelTypeEmoji,           // Emoji coach o null
    String experienceLevelName,           // Nivel o null
    Long firstQuestionId,
    Boolean isEnabled,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
)

// Cuestionario resumido (lista)
record QuestionnaireSummaryDto(
    Long id,
    String name,
    String description,
    String coachModelTypeName,
    String coachModelTypeEmoji,
    String experienceLevelName,
    Boolean isEnabled
)

// Cuestionario con primera pregunta incluida
record QuestionnaireWithFirstQuestionDto(
    Long id,
    String name,
    String description,
    String coachModelTypeName,
    String coachModelTypeEmoji,
    String experienceLevelName,
    Boolean isEnabled,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    QuestionDto firstQuestion              // Pregunta + opciones
)
```

##### DTOs de Preguntas y Opciones

```java
// Pregunta con opciones
record QuestionDto(
    Long id,
    String text,
    QuestionType type,
    List<OptionDto> options               // Ordenadas por displayOrder
)

// Opción seleccionable
record OptionDto(
    Long id,
    String text,
    Boolean requiresTextInput,
    String textInputPrompt,               // null si no requiere
    String textInputPlaceholder           // null si no requiere
)
```

##### DTOs de Sesión

```java
// Estado actual de sesión (respuesta a petición)
class QuestionnaireResponseDto {
    Long responseId;                      // ID sesión
    QuestionDto currentQuestion;          // Pregunta actual (null = completada)
    Boolean isCompleted;                  // true si fin
    Integer totalQuestionsAnswered;       // Contador
    // Builder pattern para construcción flexible
}

// Resumen completo de sesión (para IA)
class QuestionnaireResponseSummaryDto {
    Long responseId;
    Long userId;
    String userName;
    Long questionnaireId;
    String questionnaireName;
    String questionnaireDescription;
    List<AnswerDto> answers;              // TODAS las respuestas en orden
    LocalDateTime startedAt;
    LocalDateTime completedAt;            // null si no completada
    Boolean isCompleted;
}

// Una respuesta individual dentro del resumen
record AnswerDto(
    Long answerId,
    String questionText,                  // Texto completo de pregunta
    String selectedOption,                // Texto de opción elegida
    String additionalText,                // Texto libre si se proporcionó
    String aiDescription,                 // Null actualmente
    LocalDateTime answeredAt
)
```

##### DTOs de Request

```java
// Responder una pregunta
class AnswerRequestDto {
    @NotNull Long questionId;             // Pregunta actual
    @NotNull Long selectedOptionId;       // Opción elegida
    String additionalText;                // Null si no se requiere
}

// Crear cuestionario (admin)
record CreateQuestionnaireRequestDto(
    @NotBlank @Size(3-100) String name,
    @NotBlank @Size(10-1000) String description,
    Long coachModelTypeId,                // opcional
    Long experienceLevelId,               // opcional
    Long firstQuestionId                  // optional pero recomendado
)
```

#### 4. Flujo Completo: Paso a Paso

**FASE 1: Usuario selecciona Coach + Nivel**

```
Cliente: GET /questionnaires/coach/{coachId}/experience-level/{levelId}

Servidor (QuestionnaireController.getQuestionnaireByCoachAndLevel):
  ├─ QuestionnaireService.getQuestionnaireByCoachIdAndExperienceLevelId(coachId, levelId)
  │   ├─ QuestionnaireRepository.findByCoachModelTypeAndExperienceLevel(coachId, levelId)
  │   └─ Retorna: Questionnaire entity
  ├─ QuestionnaireMapper.toDto(questionnaire)
  └─ Response: QuestionnaireDto ✓
```

**FASE 2: Iniciar sesión de cuestionario**

```
Cliente: POST /questionnaires/{userId}/start/{questionnaireId}

Servidor (QuestionnaireController.startQuestionnaire):
  ├─ AppUserRepository.findById(userId) → OK
  ├─ QuestionnaireService.startQuestionnaire(userId, questionnaireId)
  │   ├─ QuestionnaireRepository.findById(questionnaireId)
  │   ├─ Crear QuestionnaireResponse:
  │   │   • user = AppUser
  │   │   • questionnaire = Questionnaire
  │   │   • startedAt = now()
  │   │   • isCompleted = false
  │   │   • isActive = true
  │   ├─ QuestionnaireResponseRepository.save() → responseId = 456
  │   ├─ Obtener firstQuestion = questionnaire.getFirstQuestion()
  │   ├─ Convertir Question a QuestionDto con OptionDtos
  │   └─ Retorna: QuestionnaireResponseDto {
  │        responseId: 456,
  │        currentQuestion: { id: 1, text: "¿Objetivo?", type: MULTIPLE_CHOICE, 
  │                          options: [{id: 1, text: "Ganar músculo"}, ...] },
  │        isCompleted: false,
  │        totalQuestionsAnswered: 0
  │       }
```

**FASE 3: Responder preguntas (CICLO)**

```
Cliente: POST /questionnaires/responses/{responseId}/answer
Body: { questionId: 1, selectedOptionId: 1, additionalText: null }

Servidor (QuestionnaireController.answerQuestion):
  ├─ Obtener QuestionnaireResponse(456)
  ├─ VALIDAR: response.isCompleted = false ✓
  ├─ Obtener Question(1)
  ├─ Obtener QuestionOption(1)
  ├─ VALIDAR: option.question.id == 1 ✓
  ├─ VALIDAR: if option.requiresTextInput then additionalText != null
  │   (Si requiere y no hay → IllegalArgumentException)
  ├─ Crear UserAnswer:
  │   • response = QuestionnaireResponse(456)
  │   • question = Question(1)
  │   • selectedOption = QuestionOption(1)
  │   • additionalText = null
  │   • answeredAt = now()
  ├─ UserAnswerRepository.save() ✓
  ├─ nextQuestion = option.getNextQuestion()
  │
  ├─ IF nextQuestion == null:  ← ¡¡¡ FIN DEL CUESTIONARIO !!!
  │   • response.markAsCompleted()
  │   • response.completedAt = now()
  │   • QuestionnaireResponseRepository.save()
  │   └─ Response: QuestionnaireResponseDto {
  │        responseId: 456,
  │        currentQuestion: null,        ← SEÑAL DE FIN
  │        isCompleted: true,            ← COMPLETADO
  │        totalQuestionsAnswered: 13
  │       }
  │
  └─ IF nextQuestion != null:  ← CONTINUAR
      • Convertir nextQuestion a QuestionDto
      └─ Response: QuestionnaireResponseDto {
           responseId: 456,
           currentQuestion: { id: 2, text: "¿Adaptaciones?", ... },
           isCompleted: false,
           totalQuestionsAnswered: 1
          }

  [REPITE: cliente hace POST /answer con nextQuestion.id hasta currentQuestion = null]
```

**FASE 4: Retroceder (Opcional)**

```
Cliente: POST /questionnaires/responses/{responseId}/previous

Servidor:
  ├─ UserAnswerRepository.findByResponseIdOrderByIdDesc(responseId)
  │   → Obtiene última respuesta
  ├─ Eliminar esa respuesta
  ├─ Si estaba completada: response.isCompleted = false, completedAt = null
  ├─ Guardar response
  └─ Retorna: QuestionnaireResponseDto con pregunta anterior

[Nota: Permite correcciones antes de completar]
```

**FASE 5: Obtener resumen (para Ronnie)**

```
Cliente: GET /questionnaires/responses/{responseId}/summary

Servidor:
  ├─ QuestionnaireResponseRepository.findById(responseId)
  ├─ UserAnswerRepository.findByResponseId(responseId)
  │   → Ordenado por answeredAt ASC (orden de respuesta)
  ├─ Mapear cada UserAnswer a AnswerDto
  └─ Response: QuestionnaireResponseSummaryDto {
       responseId: 456,
       userId: 123,
       userName: "juangarcia@example.com",
       questionnaireId: 1,
       questionnaireName: "Ronnie - Fuerza para Principiantes",
       answers: [
         AnswerDto { questionText: "¿Objetivo?", selectedOption: "Ganar músculo", ... },
         AnswerDto { questionText: "¿Adaptaciones?", selectedOption: "Ninguna", additionalText: null, ... },
         AnswerDto { questionText: "¿Peso (kg)?", selectedOption: null, additionalText: "82", ... },
         ...
       ],
       startedAt: 2025-06-21T10:30:00,
       completedAt: 2025-06-21T10:35:00,
       isCompleted: true
      }

[Este DTO se usa directamente en RoutineService.buildRoutinePrompt()]
```

#### 5. Validaciones Explícitas

| Operación | Validación | Si falla |
|---|---|---|
| `startQuestionnaire(userId, qId)` | userId existe | `UserIdNotFoundException` |
| `startQuestionnaire(userId, qId)` | questionnaireId existe | `QuestionnaireNotFoundException` |
| `startQuestionnaire(userId, qId)` | questionnaire.firstQuestion != null | `IllegalStateException` |
| `answerQuestion(...)` | responseId existe | `RuntimeException` |
| `answerQuestion(...)` | response.isCompleted = false | `IllegalStateException` |
| `answerQuestion(...)` | questionId existe | `RuntimeException` |
| `answerQuestion(...)` | selectedOptionId existe | `RuntimeException` |
| `answerQuestion(...)` | option.question.id == questionId | `IllegalArgumentException` |
| `answerQuestion(...)` | option.requiresTextInput ⇒ additionalText != blank | `IllegalArgumentException` |
| `getByCoachAndLevel(cId, lId)` | resultado existe | `QuestionnaireNotFoundException` |

#### 6. Patrón de Árbol de Decisión

La clave del sistema adaptativo es el **self-referential FK** en `QuestionOption`:

```
Questionnaire(id=1)
  ├─ firstQuestion = Question(id=1)
       ├─ QuestionOption(id=1, nextQuestion=Question(2))
       ├─ QuestionOption(id=2, nextQuestion=Question(3))
       ├─ QuestionOption(id=216, nextQuestion=null)  ← "Prefiero no responder"
       │
       └─ Question(id=2)  ← Siguiente pregunta si elige opción 1
            ├─ QuestionOption(id=10, nextQuestion=Question(4))
            ├─ QuestionOption(id=11, nextQuestion=Question(5))  ← rama diferente
            │
            └─ Question(id=5)  ← Solo accesible desde opción 11 de Q2

[Resultado: árbol adaptativo donde cada ruta es única según respuestas]
```

**Ejemplo real:**
```
Usuario elige "Entrenamiento regular" en Q9 → va a Q11 (exp. fuerza)
Usuario elige "Principiante" en Q9 → salta Q11, va a Q12

Sin este patrón, no habría personalización.
```

#### 7. Cuestionarios Predefinidos

| ID | Nombre | Coach | Nivel | firstQuestion |
|---|---|---|---|---|
| 1 | Ronnie - Fuerza para Principiantes | Ronnie | Principiante | Q1 (General) |
| 2 | Serena - Bienestar para Principiantes | Serena | Principiante | Q1 (General) |
| 3 | Eliud - Resistencia Intermedia | Eliud | Intermedio | Q1 (General) |
| 4 | Kael - Calistenia Avanzada | Kael | Avanzado | Q1 (General) |
| 5 | Evaluación General de Fitness | — | — | Q1 (General) |
| 6 | Eliud - Resistencia para Principiantes | Eliud | Principiante | Q1 (General) |
| 7 | Eliud - Resistencia Avanzada | Eliud | Avanzado | Q1 (General) |
| 8 | Kael - Calistenia para Principiantes | Kael | Principiante | Q1 (General) |
| 9 | Kael - Calistenia Intermedia | Kael | Intermedio | Q1 (General) |

Todos comienzan en **Q1** (árbol general compartido), luego divergen según coach.

#### 8. Opción "Prefiero no responder"

Cada pregunta tiene una opción especial:

```
Pregunta: "¿Cuál es tu objetivo?"
  Opción 1: "Ganar músculo" → nextQuestion = Q2
  Opción 2: "Perder peso" → nextQuestion = Q2
  Opción 3: "Mejorar salud" → nextQuestion = Q2
  Opción 216: "Prefiero no responder" → nextQuestion = Q2  ← mismo destino

En RoutineService.buildRoutinePrompt():
  • Detecta selectedOption.text = "Prefiero no responder"
  • Reemplaza a "[No respondida]" en el prompt
  • Los coaches tienen instrucción de usar defaults inteligentes
```

#### 9. Endpoints del Módulo Questionnaire

**Gestión de cuestionarios (admin):**

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| GET | `/questionnaires` | admin_client_role | Lista cuestionarios habilitados (resumen). |
| GET | `/questionnaires/{id}` | público | Obtiene cuestionario completo por ID. |
| GET | `/questionnaires/{id}/with-first-question` | público | Cuestionario + primera pregunta (una sola call). |
| GET | `/questionnaires/coach/{coachId}/experience-level/{levelId}` | público | Busca cuestionario por coach+nivel (KEY ENDPOINT). |
| POST | `/questionnaires` | admin_client_role | Crea nuevo cuestionario. |
| PUT | `/questionnaires/{id}` | admin_client_role | Actualiza cuestionario. |
| DELETE | `/questionnaires/{id}` | admin_client_role | Elimina cuestionario. |

**Sesiones de usuario:**

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| POST | `/questionnaires/{userId}/start/{questionnaireId}` | público | Inicia nueva sesión. Retorna primera pregunta. |
| POST | `/questionnaires/responses/{responseId}/answer` | público | Responde pregunta actual. Retorna siguiente pregunta (o fin). |
| POST | `/questionnaires/responses/{responseId}/previous` | público | Retrocede a pregunta anterior. |
| GET | `/questionnaires/responses/{responseId}/summary` | público | Obtiene resumen completo. REQUERIDO para pasar a IA. |
| GET | `/questionnaires/responses/my-responses` | autenticado | Todas las sesiones del usuario. |
| GET | `/questionnaires/responses/my-completed-responses` | autenticado | Solo sesiones finalizadas. |
| GET | `/questionnaires/responses/my-active-responses` | autenticado | Solo sesiones en curso. |

#### 10. Mappers (Conversión Entity → DTO)

El componente `QuestionnaireMapper` convierte entidades JPA a DTOs para serialización:

```java
@Component
public class QuestionnaireMapper {
    
    // Questionnaire → QuestionnaireDto (completo)
    public QuestionnaireDto toDto(Questionnaire q) {
        return new QuestionnaireDto(
            q.getId(),
            q.getName(),
            q.getDescription(),
            q.getCoachModelType() != null ? q.getCoachModelType().getName() : null,
            q.getCoachModelType() != null ? q.getCoachModelType().getEmojiCharacter() : null,
            q.getExperienceLevel() != null ? q.getExperienceLevel().getName() : null,
            q.getFirstQuestion() != null ? q.getFirstQuestion().getId() : null,
            q.getIsEnabled(),
            q.getCreatedAt(),
            q.getUpdatedAt()
        );
    }
    
    // Questionnaire → QuestionnaireSummaryDto (resumido)
    public QuestionnaireSummaryDto toSummaryDto(Questionnaire q) {
        return new QuestionnaireSummaryDto(
            q.getId(),
            q.getName(),
            q.getDescription(),
            q.getCoachModelType() != null ? q.getCoachModelType().getName() : null,
            q.getCoachModelType() != null ? q.getCoachModelType().getEmojiCharacter() : null,
            q.getExperienceLevel() != null ? q.getExperienceLevel().getName() : null,
            q.getIsEnabled()
        );
    }
    
    // Questionnaire + firstQuestion → QuestionnaireWithFirstQuestionDto
    public QuestionnaireWithFirstQuestionDto toWithFirstQuestionDto(Questionnaire q) {
        QuestionDto questionDto = toQuestionDto(q.getFirstQuestion());
        return new QuestionnaireWithFirstQuestionDto(
            q.getId(),
            q.getName(),
            q.getDescription(),
            q.getCoachModelType() != null ? q.getCoachModelType().getName() : null,
            q.getCoachModelType() != null ? q.getCoachModelType().getEmojiCharacter() : null,
            q.getExperienceLevel() != null ? q.getExperienceLevel().getName() : null,
            q.getIsEnabled(),
            q.getCreatedAt(),
            q.getUpdatedAt(),
            questionDto
        );
    }
    
    // Questionnaire entity ← CreateQuestionnaireRequestDto (creación)
    public Questionnaire toEntity(CreateQuestionnaireRequestDto dto) {
        Questionnaire q = new Questionnaire();
        q.setName(dto.name());
        q.setDescription(dto.description());
        q.setIsEnabled(true);
        // coachModelType, experienceLevel, firstQuestion se asignan en Service
        return q;
    }
    
    // Actualizar Questionnaire entity desde UpdateQuestionnaireRequestDto
    public void updateEntityFromDto(UpdateQuestionnaireRequestDto dto, Questionnaire q) {
        if (dto.name() != null) q.setName(dto.name());
        if (dto.description() != null) q.setDescription(dto.description());
        if (dto.isEnabled() != null) q.setIsEnabled(dto.isEnabled());
        // coach, level, firstQuestion se actualizan en Service
    }
}

// Dentro de Service: conversiones de Question y QuestionOption
private QuestionDto toQuestionDto(Question q) {
    List<OptionDto> options = q.getOptions().stream()
        .sorted(Comparator.comparing(QuestionOption::getDisplayOrder))
        .map(opt -> new OptionDto(
            opt.getId(),
            opt.getText(),
            opt.getRequiresTextInput(),
            opt.getTextInputPrompt(),
            opt.getTextInputPlaceholder()
        ))
        .collect(Collectors.toList());
    
    return new QuestionDto(q.getId(), q.getText(), q.getType(), options);
}

private AnswerDto toAnswerDto(UserAnswer ua) {
    return new AnswerDto(
        ua.getId(),
        ua.getQuestion().getText(),
        ua.getSelectedOption().getText(),
        ua.getAdditionalText(),
        ua.getAiGeneratedDescription(),
        ua.getAnsweredAt()
    );
}
```

#### 11. Repositorios (Data Access Layer)

**QuestionnaireRepository:**

```java
public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long> {
    // Busca por nombre exacto
    Optional<Questionnaire> findByName(String name);
    
    // Lista todos los habilitados
    List<Questionnaire> findByIsEnabledTrue();
    
    // QUERY CLAVE: busca por coach + nivel (ambos obligatorios)
    @Query("SELECT q FROM Questionnaire q WHERE q.coachModelType.id = :coachModelTypeId " +
           "AND q.experienceLevel.id = :experienceLevelId AND q.isEnabled = true")
    Optional<Questionnaire> findByCoachModelTypeAndExperienceLevel(
        @Param("coachModelTypeId") Long coachModelTypeId,
        @Param("experienceLevelId") Long experienceLevelId);
    
    // Busca por coach solamente
    @Query("SELECT q FROM Questionnaire q WHERE q.coachModelType.id = :coachModelTypeId " +
           "AND q.isEnabled = true")
    List<Questionnaire> findByCoachModelType(@Param("coachModelTypeId") Long coachModelTypeId);
    
    // Busca por nivel solamente
    @Query("SELECT q FROM Questionnaire q WHERE q.experienceLevel.id = :experienceLevelId " +
           "AND q.isEnabled = true")
    List<Questionnaire> findByExperienceLevel(@Param("experienceLevelId") Long experienceLevelId);
}
```

**QuestionnaireResponseRepository:**

```java
public interface QuestionnaireResponseRepository extends JpaRepository<QuestionnaireResponse, Long> {
    // Todas las sesiones del usuario (orden descendente)
    @Query("SELECT qr FROM QuestionnaireResponse qr WHERE qr.user.id = :userId " +
           "ORDER BY qr.startedAt DESC")
    List<QuestionnaireResponse> findByUserId(@Param("userId") Long userId);
    
    // Solo sesiones completadas
    @Query("SELECT qr FROM QuestionnaireResponse qr WHERE qr.user.id = :userId " +
           "AND qr.isCompleted = true ORDER BY qr.completedAt DESC")
    List<QuestionnaireResponse> findCompletedByUserId(@Param("userId") Long userId);
    
    // Solo sesiones activas (en curso)
    @Query("SELECT qr FROM QuestionnaireResponse qr WHERE qr.user.id = :userId " +
           "AND qr.isCompleted = false AND qr.isActive = true ORDER BY qr.startedAt DESC")
    List<QuestionnaireResponse> findActiveByUserId(@Param("userId") Long userId);
    
    // Sesiones de usuario para cuestionario específico
    @Query("SELECT qr FROM QuestionnaireResponse qr WHERE qr.user.id = :userId " +
           "AND qr.questionnaire.id = :questionnaireId ORDER BY qr.startedAt DESC")
    List<QuestionnaireResponse> findByUserIdAndQuestionnaireId(
        @Param("userId") Long userId,
        @Param("questionnaireId") Long questionnaireId);
    
    // Última sesión completada (para reutilización de datos)
    @Query("SELECT qr FROM QuestionnaireResponse qr WHERE qr.user.id = :userId " +
           "AND qr.questionnaire.id = :questionnaireId AND qr.isCompleted = true " +
           "ORDER BY qr.completedAt DESC LIMIT 1")
    Optional<QuestionnaireResponse> findLatestCompletedByUserAndQuestionnaire(
        @Param("userId") Long userId,
        @Param("questionnaireId") Long questionnaireId);
}
```

**UserAnswerRepository:**

```java
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    // Obtiene respuestas de sesión, ordenadas por respuesta
    @Query("SELECT ua FROM UserAnswer ua WHERE ua.response.id = :responseId " +
           "ORDER BY ua.answeredAt ASC")
    List<UserAnswer> findByResponseId(@Param("responseId") Long responseId);
    
    // Obtiene respuestas ordenadas inverso (para retroceso)
    @Query("SELECT ua FROM UserAnswer ua WHERE ua.response.id = :responseId " +
           "ORDER BY ua.id DESC")
    List<UserAnswer> findByResponseIdOrderByIdDesc(@Param("responseId") Long responseId);
    
    // Obtiene respuesta específica para pregunta en sesión
    @Query("SELECT ua FROM UserAnswer ua WHERE ua.response.id = :responseId " +
           "AND ua.question.id = :questionId")
    Optional<UserAnswer> findByResponseIdAndQuestionId(
        @Param("responseId") Long responseId,
        @Param("questionId") Long questionId);
    
    // Obtiene todas con descripción IA (para auditoría)
    @Query("SELECT ua FROM UserAnswer ua WHERE ua.aiGeneratedDescription IS NOT NULL")
    List<UserAnswer> findAllWithAiDescriptions();
    
    // Cuenta respuestas en sesión
    @Query("SELECT COUNT(ua) FROM UserAnswer ua WHERE ua.response.id = :responseId")
    Long countByResponseId(@Param("responseId") Long responseId);
}
```

#### 12. Lógica de Servicio Detallada

```java
@Service
@Transactional
public class QuestionnaireService {
    
    // Inicia sesión de cuestionario
    public QuestionnaireResponseDto startQuestionnaire(Long userId, Long questionnaireId) {
        // 1. Validar usuario existe
        AppUser user = appUserRepository.findById(userId)
            .orElseThrow(() -> new UserIdNotFoundException("User not found"));
        
        // 2. Validar cuestionario existe
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
            .orElseThrow(() -> new QuestionnaireNotFoundException("Questionnaire not found"));
        
        // 3. Validar que tiene primera pregunta
        if (questionnaire.getFirstQuestion() == null) {
            throw new IllegalStateException("Questionnaire has no first question");
        }
        
        // 4. Crear sesión
        QuestionnaireResponse response = new QuestionnaireResponse();
        response.setUser(user);
        response.setQuestionnaire(questionnaire);
        response.setStartedAt(LocalDateTime.now());
        response.setIsCompleted(false);
        response.setIsActive(true);
        
        // 5. Persistir
        QuestionnaireResponse saved = questionnaireResponseRepository.save(response);
        
        // 6. Convertir primera pregunta a DTO
        QuestionDto firstQuestion = toQuestionDto(questionnaire.getFirstQuestion());
        
        // 7. Retornar estado inicial
        return QuestionnaireResponseDto.builder()
            .responseId(saved.getId())
            .currentQuestion(firstQuestion)
            .isCompleted(false)
            .totalQuestionsAnswered(0)
            .build();
    }
    
    // Responde pregunta y avanza
    public QuestionnaireResponseDto answerQuestion(
        Long responseId, Long questionId, Long selectedOptionId, String additionalText) {
        
        // 1. Obtener sesión
        QuestionnaireResponse response = questionnaireResponseRepository.findById(responseId)
            .orElseThrow(() -> new RuntimeException("Response not found"));
        
        // 2. Validar que no está completada
        if (response.getIsCompleted()) {
            throw new IllegalStateException("Response is already completed");
        }
        
        // 3. Obtener pregunta actual
        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("Question not found"));
        
        // 4. Obtener opción seleccionada
        QuestionOption selectedOption = questionOptionRepository.findById(selectedOptionId)
            .orElseThrow(() -> new RuntimeException("Option not found"));
        
        // 5. Validar que opción pertenece a pregunta
        if (!selectedOption.getQuestion().getId().equals(questionId)) {
            throw new IllegalArgumentException("Option does not belong to question");
        }
        
        // 6. Validar texto adicional si se requiere
        if (selectedOption.getRequiresTextInput() && 
            (additionalText == null || additionalText.isBlank())) {
            throw new IllegalArgumentException("Additional text is required for this option");
        }
        
        // 7. Crear y persistir respuesta
        UserAnswer answer = new UserAnswer();
        answer.setResponse(response);
        answer.setQuestion(question);
        answer.setSelectedOption(selectedOption);
        answer.setAdditionalText(additionalText);
        answer.setAnsweredAt(LocalDateTime.now());
        
        userAnswerRepository.save(answer);
        
        // 8. Obtener siguiente pregunta
        Question nextQuestion = selectedOption.getNextQuestion();
        
        // 9. Si es NULL → FINALIZAR SESIÓN
        if (nextQuestion == null) {
            response.markAsCompleted();
            response.setCompletedAt(LocalDateTime.now());
            questionnaireResponseRepository.save(response);
            
            return QuestionnaireResponseDto.builder()
                .responseId(responseId)
                .currentQuestion(null)  // ← SEÑAL DE FIN
                .isCompleted(true)
                .totalQuestionsAnswered(countAnswers(responseId))
                .build();
        }
        
        // 10. Si no es NULL → retornar siguiente pregunta
        QuestionDto nextQuestionDto = toQuestionDto(nextQuestion);
        
        return QuestionnaireResponseDto.builder()
            .responseId(responseId)
            .currentQuestion(nextQuestionDto)
            .isCompleted(false)
            .totalQuestionsAnswered(countAnswers(responseId))
            .build();
    }
    
    // Retrocede a pregunta anterior
    public QuestionnaireResponseDto goToPreviousQuestion(Long responseId) {
        QuestionnaireResponse response = questionnaireResponseRepository.findById(responseId)
            .orElseThrow(() -> new RuntimeException("Response not found"));
        
        // Obtener última respuesta
        List<UserAnswer> answers = userAnswerRepository.findByResponseIdOrderByIdDesc(responseId);
        if (answers.isEmpty()) {
            throw new IllegalStateException("No answers to go back from");
        }
        
        UserAnswer lastAnswer = answers.get(0);
        
        // Si hay una pregunta anterior en la lista
        Question previousQuestion = null;
        if (answers.size() > 1) {
            previousQuestion = answers.get(1).getQuestion();
        } else {
            // Si era la primera pregunta, retornar primera del cuestionario
            previousQuestion = response.getQuestionnaire().getFirstQuestion();
        }
        
        // Eliminar última respuesta
        userAnswerRepository.delete(lastAnswer);
        
        // Si estaba completada, marcar como no completada
        if (response.getIsCompleted()) {
            response.setIsCompleted(false);
            response.setCompletedAt(null);
            questionnaireResponseRepository.save(response);
        }
        
        // Retornar pregunta anterior
        QuestionDto previousQuestionDto = toQuestionDto(previousQuestion);
        
        return QuestionnaireResponseDto.builder()
            .responseId(responseId)
            .currentQuestion(previousQuestionDto)
            .isCompleted(false)
            .totalQuestionsAnswered(countAnswers(responseId))
            .build();
    }
    
    // Obtiene resumen completo (para pasar a IA)
    public QuestionnaireResponseSummaryDto getResponseSummary(Long responseId) {
        QuestionnaireResponse response = questionnaireResponseRepository.findById(responseId)
            .orElseThrow(() -> new RuntimeException("Response not found"));
        
        // Obtener todas las respuestas en orden
        List<UserAnswer> answers = userAnswerRepository.findByResponseId(responseId);
        
        // Convertir a AnswerDto
        List<AnswerDto> answerDtos = answers.stream()
            .map(this::toAnswerDto)
            .collect(Collectors.toList());
        
        // Construir resumen
        return QuestionnaireResponseSummaryDto.builder()
            .responseId(responseId)
            .userId(response.getUser().getId())
            .userName(response.getUser().getEmail())
            .questionnaireId(response.getQuestionnaire().getId())
            .questionnaireName(response.getQuestionnaire().getName())
            .questionnaireDescription(response.getQuestionnaire().getDescription())
            .answers(answerDtos)
            .startedAt(response.getStartedAt())
            .completedAt(response.getCompletedAt())
            .isCompleted(response.getIsCompleted())
            .build();
    }
}
```

#### 13. Diagrama de Relaciones

```
              ┌─────────────────────┐
              │   Questionnaire     │
              ├─────────────────────┤
              │ id (PK)             │
              │ name (UNIQUE)       │
              │ description         │
              │ coach_model_type_id │
              │ experience_level_id │
              │ first_question_id ──┬────────┐
              │ is_enabled          │        │
              │ created_at, updated_at       │
              └─────────────┬────────┘        │
                            │                 │
         ┌──────────────────┼────────────────┘
         │                  │ 1:N
         │                  ▼
         │          ┌──────────────────────────────┐
         │          │      Question                │
         │          ├──────────────────────────────┤
         │          │ id (PK)                      │
         │          │ text                         │
         │          │ type (ENUM)                  │
         │          │ is_enabled                   │
         │          │ created_at                   │
         │          └──────────┬───────────────────┘
         │                     │ 1:N
         │                     ▼
         │       ┌──────────────────────────────────┐
         │       │   QuestionOption                 │
         │       ├──────────────────────────────────┤
         │       │ id (PK)                          │
         │       │ question_id (FK)                 │
         │       │ text                             │
         │       │ next_question_id (FK, nullable)──┼──→ self-referential
         │       │ display_order                    │    (apunta a Question)
         │       │ requires_text_input              │
         │       │ text_input_prompt                │
         │       │ text_input_placeholder           │
         │       └──────────────────────────────────┘
         │
         │
         └──→ CoachModelType
         │    ├─ id (PK)
         │    ├─ name (UNIQUE)
         │    ├─ description
         │    ├─ emoji_character
         │    └─ enabled
         │
         └──→ ExperienceLevel
              ├─ id (PK)
              ├─ name (UNIQUE)
              └─ description


┌──────────────────────────┐
│ QuestionnaireResponse    │
├──────────────────────────┤
│ id (PK)                  │
│ user_id (FK)─────────────┬──→ AppUser
│ questionnaire_id (FK)────┬──→ Questionnaire
│ started_at               │
│ completed_at             │
│ is_completed             │
│ is_active                │
└──────────────┬───────────┘
               │ 1:N
               ▼
        ┌──────────────────────┐
        │   UserAnswer         │
        ├──────────────────────┤
        │ id (PK)              │
        │ response_id (FK)     │
        │ question_id (FK)─────→ Question
        │ selected_option_id───→ QuestionOption
        │ additional_text      │
        │ ai_generated_desc.   │
        │ answered_at          │
        └──────────────────────┘
```

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

El enum `CoachType` define los coaches de IA disponibles. Cada uno aporta su especialidad mediante un `systemContext` que se inyecta en el prompt enviado a Ronnie:

| Coach | Especialidad | Endpoint en Ronnie |
|---|---|---|
| `MASTER` (DEPRECATED) | Planificador generalista (en desuso, fuera de servicio) | `/master/generate-routine` |
| `RONNIE` | Hipertrofia y fuerza muscular (inspirado en Ronnie Coleman) | `/ronnie/generate-routine` |
| `ELIUD` | Running, cardio y rendimiento aeróbico | `/eliud/generate-routine` |
| `SERENA` | Fitness femenino, tonificación y bienestar | `/serena/generate-routine` |
| `KAEL` | Calistenia y street workout | `/kael/generate-routine` |

> [!WARNING]
> **Deprecación de MASTER**: El coach `MASTER` y su endpoint correspondiente están obsoletos y no deben utilizarse. Para la generación activa de rutinas del usuario, se debe seleccionar obligatoriamente un coach de especialidad real (Ronnie, Eliud, Serena o Kael).

Para los coaches activos, `systemContext` se incluye en el prompt bajo la sección `ROL DEL ENTRENADOR` para perfilar al LLM.

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

`SpringSecurityConfig` define una `SecurityFilterChain` **stateless** (sin sesiones HTTP) con las siguientes reglas:

**Endpoints públicos** (no requieren token JWT):
```
/v3/api-docs/**                  — OpenAPI spec
/swagger-ui/**                   — UI interactiva
/swagger-ui.html
/auth/**                         — Todos: login, register, verify, refresh, logout
/exercise-images/**              — Imágenes de ejercicios de Ronnie
```

**Resto de endpoints**: requieren token JWT válido en cabecera `Authorization: Bearer <token>`.

### OAuth2 Resource Server — Arquitectura

IFit **NO es un servidor de autorización** (no emite tokens). Es un **OAuth2 Resource Server**: valida tokens JWT emitidos por Keycloak.

**Flujo de validación de cada petición HTTP:**

```
Cliente envía:
GET /routines
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI...

↓

Spring Security intercepta + JwtDecoder:
  1. Descarga clave pública de Keycloak:
     GET http://localhost:9090/realms/ifit-realm/protocol/openid-connect/certs
  
  2. Verifica firma del token (RSA-256)
  
  3. Comprueba claims críticos:
     - iss (issuer) = "http://localhost:9090/realms/ifit-realm" ✓
     - exp (expiración) > ahora ✓
  
  4. JwtAuthenticationConverter extrae:
     - subject (sub) → principal username
     - realm_access.roles → ROLE_user, ROLE_admin, etc.
     - client_id
     - preferred_username
  
  5. Construye Authentication object

↓

@PreAuthorize("hasRole('ADMIN')")  → verifica presencia del rol

↓

Endpoint ejecuta si validación exitosa
```

#### Extracción de Roles

Los roles se extraen del claim `realm_access.roles` del JWT:

```json
{
  "sub": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "preferred_username": "juangarcia@example.com",
  "realm_access": {
    "roles": ["default-roles-ifit-realm", "admin_client_role", "user"]
  },
  "iss": "http://localhost:9090/realms/ifit-realm",
  "exp": 1687954800,
  ...
}
```

En el código, se usan:
- `@PreAuthorize("hasRole('admin_client_role')")` para endpoints administrativos
- `@PreAuthorize("hasRole('ROLE_USER')")` o `isAuthenticated()` para endpoints de usuario

#### Extracción del UserId

`JwtUtils.extractUserId(request, objectMapper)` lee el claim `sub` (subject) del token:

```java
String keycloakUserId = jwtUtils.extractUserId(httpRequest);
// keycloakUserId = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
```

Este ID es **crítico** porque vincula:
- Usuario en Keycloak (identidad)
- Usuario en BD local (perfil, coach, experiencia)
- Rutinas en BD (entrenamientos)
- Sesión en Ronnie (historial de conversación con LLM)

### CSRF

CSRF **está deshabilitado**. Es la práctica estándar para APIs REST stateless donde:
- El cliente (MAUI, SPA, etc.) gestiona explícitamente el token JWT
- Cada petición incluye el token en cabecera `Authorization`
- No hay sesiones HTTP server-side
- El servidor no inyecta tokens en formularios

### Ciclo de Vida del Token

```
1. Login: Keycloak emite access_token (5 min) + refresh_token (24h)

2. Petición autenticada: Cliente envía access_token en Authorization header
   - Si access_token válido → permitir
   - Si access_token expirado → rechazar (401)

3. Refresh: Cliente envía refresh_token a POST /auth/refresh
   - Si refresh_token válido → emitir nuevo access_token
   - Si refresh_token expirado → usuario debe login de nuevo

4. Logout: Cliente envía refresh_token a POST /auth/logout
   - Keycloak revoca refresh_token
   - Cliente elimina tokens locales
   - access_token caduca por tiempo (no se revoca instantáneamente)
```

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

### application.properties

Archivo: `src/main/resources/application.properties`

```properties
# === APLICACIÓN ===
spring.application.name=ifit
server.port=8081

# === BASE DE DATOS ===
spring.datasource.url=jdbc:mysql://localhost:3306/ifit
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:root}
spring.sql.init.mode=always
spring.sql.init.data-locations=classpath:data.sql,classpath:excercise_catalog_esp.sql
spring.jpa.hibernate.ddl-auto=create-drop

# === EMAIL (SMTP Gmail) ===
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME:adminifit96@gmail.com}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# === OAuth2 Resource Server — Keycloak ===
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9090/realms/ifit-realm
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:9090/realms/ifit-realm/protocol/openid-connect/certs
jwt.auth.converter.resource-id=springboot-ifit-client
jwt.auth.converter.principle-attribute=preferred_username

# === Keycloak Admin — Autenticación de servicios ===
keycloak.client-id=springboot-ifit-client
keycloak.client-secret=${KEYCLOAK_CLIENT_SECRET}
keycloak.token-url=http://localhost:9090/realms/ifit-realm/protocol/openid-connect/token
keycloak.auth-server-url=http://localhost:9090
keycloak.realm=ifit-realm

# === Ronnie — Microservicio IA (LangChain4j) ===
ronnie.service.url=http://localhost:8082

# === Eureka — Service Discovery ===
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true
eureka.instance.instance-id=${spring.application.name}:${server.port}

# === OpenAPI / Swagger ===
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

### Variables de Entorno Requeridas

Las siguientes variables de entorno **deben estar definidas** en el entorno de ejecución:

| Variable | Descripción | Obligatoria | Default |
|---|---|---|---|
| `DB_USERNAME` | Usuario MySQL | No | `root` |
| `DB_PASSWORD` | Contraseña MySQL | No | `root` |
| `MAIL_USERNAME` | Email SMTP remitente | No | `adminifit96@gmail.com` |
| `MAIL_PASSWORD` | Contraseña de aplicación Gmail (no es la contraseña de cuenta) | **SÍ** | — |
| `KEYCLOAK_CLIENT_SECRET` | Secret del cliente OAuth2 en Keycloak | **SÍ** | — |

**Ejemplo de inicialización:**

```bash
export DB_USERNAME=ifit_user
export DB_PASSWORD=secure_password
export MAIL_PASSWORD=ggsj hyxw qwer tyui
export KEYCLOAK_CLIENT_SECRET=abc123def456ghi789jkl
mvn spring-boot:run
```

O en `application-local.properties` (gitignored):

```properties
spring.datasource.username=ifit_user
spring.datasource.password=secure_password
spring.mail.password=ggsj hyxw qwer tyui
keycloak.client-secret=abc123def456ghi789jkl
```

---

## Puesta en Marcha

### Prerrequisitos

- Java 21+
- Maven 3.9+
- MySQL 8.0+ con base de datos `ifit` creada
- Keycloak 23.0 arrancado en `localhost:9090` con realm `ifit-realm` configurado
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

---

---

# ANEXO: OAuth2, Resource Server y Flujo Keycloak

Este anexo explica los conceptos de seguridad subyacentes en IFit: qué es OAuth2, por qué se usa Keycloak como servidor de identidades, y cómo fluyen las peticiones a través del ecosistema de microservicios.

## 1. ¿Qué es OAuth2?

**OAuth2** es un estándar de **autorización abierta** que permite que un usuario autorize a una aplicación a acceder a sus recursos en otro servidor, **sin compartir su contraseña**.

### Conceptos Clave

| Concepto | Descripción |
|---|---|
| **Resource Owner** | El usuario (tú, como usuario de IFit) |
| **Client** | La aplicación que quiere acceder a recursos (p.ej. MAUI frontend) |
| **Authorization Server** | El servidor que autentica al usuario y emite tokens (Keycloak) |
| **Resource Server** | El servidor que aloja los recursos protegidos (IFit) |
| **Access Token** | Token JWT que prueba autorización (válido ~5 min) |
| **Refresh Token** | Token de larga duración para renovar access_token (válido ~24h) |

### Analogía del mundo real

```
Tu email (Gmail, Outlook, etc.) es el "Resource Owner" que contiene tus datos.
Una aplicación de terceros quiere leerlo (p.ej. "Conectar con Gmail").

En lugar de darte la app tu contraseña de Gmail:
  1. Google (Authorization Server) te pregunta: "¿Permitir acceso?"
  2. Tú das permiso
  3. Google te da un "token" (no la contraseña)
  4. La app usa el token para leer tu email
  5. El token caduca en 1 hora (seguridad)
  6. La app puede renovar con un "refresh token"

Así: la app NUNCA ve tu contraseña de Gmail.
```

## 2. Flujos de OAuth2

OAuth2 define varios "flujos" (flows) según el tipo de cliente. IFit usa **Authorization Code Flow with PKCE**:

### Authorization Code Flow (Simplificado)

```
┌─────────────┐             ┌──────────────────────┐             ┌───────────────┐
│   MAUI      │             │      Keycloak        │             │     IFit      │
│  (Cliente)  │             │ (Auth Server)        │             │ (Resource)    │
└──────┬──────┘             └──────────┬───────────┘             └───────┬───────┘
       │                               │                                 │
       │ 1. Usuario toca "Login"       │                                 │
       ├──────────────────────────────►│                                 │
       │  Redirige a pantalla Keycloak │                                 │
       │                               │                                 │
       │ 2. Usuario ingresa email/pass │                                 │
       │◄──── (en formulario Keycloak──┤                                 │
       │                               │                                 │
       │ 3. Keycloak valida credenciales
       │  y redirige con "code"        │                                 │
       │◄──────────────────────────────┤                                 │
       │  ?code=xyz&state=abc          │                                 │
       │                               │                                 │
       │ 4. MAUI (backend) intercambia │                                 │
       │    code → access_token        │                                 │
       ├──────────────────────────────►│                                 │
       │  POST /token                  │                                 │
       │  grant_type=authorization_code│                                 │
       │  code=xyz                     │                                 │
       │◄──────────────────────────────┤                                 │
       │  {access_token, refresh_token}│                                 │
       │                               │                                 │
       │ 5. MAUI almacena tokens       │                                 │
       │  (seguramente)                │                                 │
       │                               │                                 │
       │ 6. MAUI toca "Ver Rutinas"    │                                 │
       ├───────────────────────────────────────────────────────────────► │
       │      GET /routines            │                                 │
       │  Authorization: Bearer <token>│                                 │
       │                               │                                 │
       │                               │ IFit valida token              │
       │                               │ (sin consultar a Keycloak)     │
       │◄───────────────────────────────────────────────────────────────┤
       │      [{id: 1, name: "Pecho"...}]                               │
       │                               │                                 │
```

### En IFit: Validación Sin Requerir a Keycloak

Lo especial de **Resource Server** es que IFit **NO consulta a Keycloak en cada petición**. En cambio:

1. **Primera vez (boot)**: Descarga clave pública de Keycloak
   ```
   GET http://localhost:9090/realms/ifit-realm/protocol/openid-connect/certs
   ```
   
2. **Cada petición subsecuente**: Valida token **localmente** usando clave pública
   - Verifica firma criptográfica ✓
   - Comprueba expiración ✓
   - Extrae roles y user ID ✓
   - **Sin hacer HTTP a Keycloak**

Esto hace el sistema **muy rápido y escalable**.

## 3. ¿Qué es un Resource Server?

Un **Resource Server** es un servidor que:

1. **Almacena recursos** (rutinas, cuestionarios, usuarios)
2. **Acepta tokens JWT** de un Authorization Server externo
3. **Valida tokens** de forma independiente (sin consultar el auth server)
4. **Concede o deniega acceso** basado en validación local

### Recursos en IFit

| Recurso | Localización | Requisito para acceder |
|---|---|---|
| Rutinas | Base de datos MySQL | Token JWT válido + ser propietario |
| Cuestionarios | Base de datos MySQL | Token JWT válido |
| Ejercicios | Base de datos MySQL | Token JWT válido |
| Perfil de usuario | Base de datos MySQL | Token JWT válido + ser el mismo usuario |

### Validación de Acceso en IFit

```java
@GetMapping("/routines/user/{userId}")
@PreAuthorize("isAuthenticated()")
public List<RoutineResponseDto> getUserRoutines(
    @PathVariable Long userId,
    HttpServletRequest request
) {
    String keycloakUserId = jwtUtils.extractUserId(request);
    
    // El usuario autenticado solo ve SUS propias rutinas
    if (!userService.userIdBelongsToKeycloakId(userId, keycloakUserId)) {
        throw new ForbiddenException("No tienes acceso a estas rutinas");
    }
    
    return routineService.getRoutinesByUserId(userId);
}
```

**Sin Resource Server**: cada petición requeriría llamar a Keycloak para validar. **Con Resource Server**: IFit valida localmente usando criptografía.

## 4. ¿Qué es Keycloak?

**Keycloak** es un servidor de **Identidad y Acceso (IAM - Identity and Access Management)** de código abierto.

### Roles de Keycloak en IFit

| Responsabilidad | Descripción |
|---|---|
| **Almacenar credenciales** | Contraseña hasheada (bcrypt, PBKDF2, scrypt) |
| **Autenticar usuarios** | Verifica email + contraseña |
| **Emitir tokens JWT** | Crea access_token + refresh_token |
| **Administrar roles** | Define quién es ADMIN, quién es USER |
| **Renovar tokens** | Emite nuevos access_token si refresh_token es válido |
| **Revocar sesiones** | Invalida refresh_token al hacer logout |
| **Publicar claves públicas** | Para que IFit valide tokens sin consultar |

### Estructura de Keycloak

```
Keycloak
├── Realm: ifit-realm
│   ├── Client: springboot-ifit-client
│   │   └── Secret: abc123xyz789...
│   ├── Users (tabla local)
│   │   ├── juangarcia@example.com (contraseña hasheada)
│   │   ├── maria@example.com
│   │   └── ...
│   └── Roles
│       ├── user
│       ├── admin_client_role
│       └── ...
└── [Otros realms...]
```

### Endpoints Importantes de Keycloak

| Endpoint | Propósito | Quién lo usa |
|---|---|---|
| `POST /token` | Autentica usuario (email/pass) → emite tokens | MAUI, IFit (al registrar) |
| `GET /certs` | Publica clave RSA pública | IFit (al validar) |
| `POST /logout` | Revoca refresh_token | MAUI, IFit |
| `POST /admin/users` | Crea usuario (solo admin) | IFit (en register) |
| `DELETE /admin/users/{id}` | Elimina usuario (rollback) | IFit (si falla insert en BD) |
| `PUT /admin/users/{id}` | Actualiza usuario | IFit (admin) |

## 5. Flujo Completo: Frontend → API Gateway → IFit → Keycloak

Este es el flujo **real** en un día normal de un usuario iFit:

### Escena 1: Registro de Nuevo Usuario

```
┌──────────┐           ┌──────────┐           ┌──────────┐           ┌─────────┐
│   MAUI   │           │ Gateway  │           │  IFit    │           │Keycloak │
│ (cliente)│           │ (puerto  │           │ (puerto  │           │(puerto  │
│          │           │   8080)  │           │  8081)   │           │ 9090)   │
└────┬─────┘           └────┬─────┘           └────┬─────┘           └────┬────┘
     │                      │                      │                      │
     │ 1. POST /auth/register                      │                      │
     │    {email, password, name}                  │                      │
     ├─────────────────────────────────────────────┤                      │
     │     (StripPrefix: /ifit/api/v1 → /)         │                      │
     │                                             │                      │
     │                                             │ 2. Valida email      │
     │                                             │    no duplicado      │
     │                                             │    en BD local ✓     │
     │                                             │                      │
     │                                             │ 3. POST /admin/users │
     │                                             ├─────────────────────►│
     │                                             │  {email, password}   │
     │                                             │                      │
     │                                             │                      │ Keycloak:
     │                                             │                      │ • Hashea contraseña
     │                                             │                      │ • Crea usuario
     │                                             │                      │ • Genera UUID
     │                                             │                      │
     │                                             │◄─────────────────────┤
     │                                             │ 201 Created          │
     │                                             │ {userId: abc123...}  │
     │                                             │                      │
     │                                             │ 4. INSERT user BD    │
     │                                             │  keycloak_user_id=   │
     │                                             │  abc123...           │
     │                                             │  is_verified=false   │
     │                                             │                      │
     │                                             │ 5. Genera código 6   │
     │                                             │    dígitos, almacena │
     │                                             │    con 15min expira  │
     │                                             │                      │
     │                                             │ 6. Envía email con   │
     │                                             │    código (SMTP)     │
     │                                             │                      │
     │◄─────────────────────────────────────────────┤                      │
     │ 201 Created                                 │                      │
     │ {userId: 42, email, requiresVerification}   │                      │
     │                                             │                      │
     │ Usuario recibe email con código "123456"    │                      │
     │                                             │                      │
```

### Escena 2: Verificación de Email y Login

```
┌──────────┐           ┌──────────┐           ┌──────────┐           ┌─────────┐
│   MAUI   │           │ Gateway  │           │  IFit    │           │Keycloak │
│          │           │          │           │          │           │         │
└────┬─────┘           └────┬─────┘           └────┬─────┘           └────┬────┘
     │                      │                      │                      │
     │ 1. POST /auth/verify                        │                      │
     │    {email, verificationCode: "123456"}      │                      │
     ├─────────────────────────────────────────────┤                      │
     │                                             │ 2. Busca código en BD│
     │                                             │    Valida expiración │
     │                                             │    ✓                 │
     │                                             │                      │
     │                                             │ 3. UPDATE user      │
     │                                             │    is_verified=true  │
     │                                             │                      │
     │                                             │ 4. PUT /admin/users  │
     │                                             │    {emailVerified:   │
     │                                             │     true}            │
     │                                             ├─────────────────────►│
     │                                             │                      │
     │                                             │                      │ Keycloak:
     │                                             │                      │ Marca verificado
     │                                             │◄─────────────────────┤
     │                                             │ 200 OK               │
     │                                             │                      │
     │                                             │ 5. POST /token       │
     │                                             │    (password flow)   │
     │                                             ├─────────────────────►│
     │                                             │ grant_type=password  │
     │                                             │ username=email       │
     │                                             │ password=...         │
     │                                             │ client_id=...        │
     │                                             │ client_secret=...    │
     │                                             │                      │
     │                                             │                      │ Keycloak:
     │                                             │                      │ • Verifica email/pass
     │                                             │                      │ • Genera JWT
     │                                             │                      │ (iss=keycloak, sub=uuid)
     │                                             │◄─────────────────────┤
     │                                             │ {access_token,       │
     │                                             │  refresh_token}      │
     │                                             │                      │
     │◄─────────────────────────────────────────────┤                      │
     │ 200 OK                                      │                      │
     │ {accessToken, refreshToken,                 │                      │
     │  keycloakUserId, userProfile}               │                      │
     │                                             │                      │
     │ MAUI almacena tokens en KeyChain/SecureStore│                      │
     │                                             │                      │
```

### Escena 3: Usando el Sistema (Petición Protegida)

```
┌──────────┐           ┌──────────┐           ┌──────────┐           ┌─────────┐
│   MAUI   │           │ Gateway  │           │  IFit    │           │Keycloak │
│          │           │          │           │          │           │         │
└────┬─────┘           └────┬─────┘           └────┬─────┘           └────┬────┘
     │                      │                      │                      │
     │ Usuario toca "Ver mis rutinas"              │                      │
     │                                             │                      │
     │ GET /routines/user/42                       │                      │
     │ Authorization: Bearer <access_token>        │                      │
     ├─────────────────────────────────────────────┤                      │
     │     (StripPrefix: /ifit/api/v1 → /)         │                      │
     │                                             │                      │
     │                                             │ Spring Security:     │
     │                                             │ • Extrae token del   │
     │                                             │   header             │
     │                                             │                      │
     │                                             │ JwtDecoder:          │
     │                                             │ • Valida firma       │
     │                                             │   (clave pública)    │
     │                                             │ • Verifica exp, iss  │
     │                                             │ • Extrae claims      │
     │                                             │ (sin llamar Keycloak)│
     │                                             │                      │
     │                                             │ JwtAuthenticationConverter:
     │                                             │ • Extrae sub (uuid)  │
     │                                             │ • Extrae roles       │
     │                                             │ • Construye           │
     │                                             │   Authentication     │
     │                                             │                      │
     │                                             │ RoutineController:   │
     │                                             │ • Valida autorización│
     │                                             │ • Verifica propiedad │
     │                                             │ • Consulta BD        │
     │                                             │                      │
     │◄─────────────────────────────────────────────┤                      │
     │ 200 OK                                      │                      │
     │ [{id: 1, name: "Pecho",                     │                      │
     │   exercises: [...]}]                        │                      │
     │                                             │                      │
     │ MAUI muestra rutinas en pantalla            │                      │
     │                                             │                      │
```

### Escena 4: Token Expirado → Refresh

```
┌──────────┐           ┌──────────┐           ┌──────────┐           ┌─────────┐
│   MAUI   │           │ Gateway  │           │  IFit    │           │Keycloak │
│          │           │          │           │          │           │         │
└────┬─────┘           └────┬─────┘           └────┬─────┘           └────┬────┘
     │                      │                      │                      │
     │ GET /routines                               │                      │
     │ Authorization: Bearer <access_token>        │                      │
     │ (token expirado hace 2 minutos)             │                      │
     ├─────────────────────────────────────────────┤                      │
     │                                             │ Spring Security:     │
     │                                             │ "Token exp: 1234     │
     │                                             │  es < ahora (5678)"  │
     │◄─────────────────────────────────────────────┤                      │
     │ 401 Unauthorized                            │                      │
     │                                             │                      │
     │ MAUI detecta 401 → Ejecuta refresh          │                      │
     │                                             │                      │
     │ POST /auth/refresh                          │                      │
     │ {refreshToken: <refresh_token>}             │                      │
     ├─────────────────────────────────────────────┤                      │
     │                                             │ POST /token          │
     │                                             │ grant_type=          │
     │                                             │ refresh_token        │
     │                                             │ refresh_token=<...>  │
     │                                             ├─────────────────────►│
     │                                             │                      │
     │                                             │                      │ Keycloak:
     │                                             │                      │ • Valida refresh_token
     │                                             │                      │ • Emite nuevo JWT
     │                                             │                      │
     │                                             │◄─────────────────────┤
     │                                             │ {access_token,       │
     │                                             │  refresh_token}      │
     │                                             │                      │
     │◄─────────────────────────────────────────────┤                      │
     │ 200 OK                                      │                      │
     │ {accessToken, refreshToken}                 │                      │
     │                                             │                      │
     │ MAUI guarda nuevos tokens                   │                      │
     │                                             │                      │
     │ GET /routines (REINTENTADO)                 │                      │
     │ Authorization: Bearer <nuevo_access_token> │                      │
     ├─────────────────────────────────────────────┤                      │
     │                                             │ ✓ Token válido       │
     │◄─────────────────────────────────────────────┤                      │
     │ 200 OK [...]                                │                      │
     │                                             │                      │
```

## 6. Diagrama de Arquitectura Ampliado

```
                    ┌──────────────────────┐
                    │   MAUI Cliente       │
                    │  (.NET MAUI App)     │
                    │                      │
                    │  Almacena tokens:    │
                    │  • accessToken (5m)  │
                    │  • refreshToken (24h)│
                    └──────────┬───────────┘
                               │
                               │ GET /ifit/api/v1/routines
                               │ Authorization: Bearer <token>
                               │
                    ┌──────────▼───────────┐
                    │   API Gateway        │
                    │  Spring Cloud Gateway│
                    │  Puerto: 8080        │
                    │                      │
                    │ • StripPrefix /ifit/api/v1
                    │ • Retransmite JWT    │
                    │ • Rate limiting      │
                    └──────────┬───────────┘
                               │
                               │ GET /routines
                               │ Authorization: Bearer <token>
                               │
                    ┌──────────▼───────────────────┐
                    │   IFit (Resource Server)     │
                    │   Spring Boot 3.5.7          │
                    │   Puerto: 8081               │
                    │                              │
                    │  ┌────────────────────────┐  │
                    │  │ Spring Security        │  │
                    │  │ • Extrae token         │  │
                    │  │ • JwtDecoder valida    │  │
                    │  │   (sin Keycloak)       │  │
                    │  │ • Construye            │  │
                    │  │   Authentication       │  │
                    │  └────────────┬───────────┘  │
                    │               │              │
                    │  ┌────────────▼────────────┐ │
                    │  │ Controller (@RestController)
                    │  │ RoutineController       │ │
                    │  │ • Valida acceso         │ │
                    │  │ • Extrae userId        │ │
                    │  └────────────┬────────────┘ │
                    │               │              │
                    │  ┌────────────▼────────────┐ │
                    │  │ Business Logic          │ │
                    │  │ RoutineService          │ │
                    │  │ • Consulta BD           │ │
                    │  │ • Aplica reglas         │ │
                    │  └────────────┬────────────┘ │
                    │               │              │
                    │  ┌────────────▼────────────┐ │
                    │  │ Database               │ │
                    │  │ (MySQL)                │ │
                    │  │ • routine              │ │
                    │  │ • routine_day          │ │
                    │  │ • routine_exercise     │ │
                    │  └────────────────────────┘ │
                    │                              │
                    │  ┌────────────────────────┐  │
                    │  │ Claves públicas        │  │
                    │  │ de Keycloak            │  │
                    │  │ (cached en JwtDecoder) │  │
                    │  └────────────────────────┘  │
                    └──────────────┬───────────────┘
                                   │
                    ┌──────────────┴───────────────┐
                    │                              │
      ┌─────────────▼──────────────┐  ┌──────────▼──────────┐
      │  Keycloak                  │  │  (solo en register  │
      │  (OAuth2 + OpenID Connect) │  │   y logout)         │
      │  Puerto: 9090              │  │                     │
      │                            │  │  POST /token        │
      │  • Realm: ifit-realm       │  │  DELETE /logout     │
      │  • Client:                 │  │  POST /admin/users  │
      │    springboot-ifit-client  │  │                     │
      │  • Users (credenciales)    │  └─────────────────────┘
      │  • Roles                   │
      │  • Keys (/certs endpoint)  │
      └────────────────────────────┘
```

## 7. Resumen de Ventajas: Resource Server vs Monolito

| Aspecto | Resource Server (IFit actual) | Servidor Monolito |
|---|---|---|
| **Validación de token** | Local, sin HTTP a Keycloak | Cada petición: HTTP a auth server |
| **Velocidad** | Sub-milisegundo (criptografía) | +50-100ms (red) |
| **Escalabilidad** | IFit crece sin afectar Keycloak | Cuello botella en auth server |
| **Separación de responsabilidades** | Identidad ↔ Lógica de negocio | Todo mezclado |
| **Estándar** | OAuth2 (compatible con muchos clients) | Propietario |
| **Revocación instantánea** | Tokens no se revocan al logout (caduca por tiempo) | Posible revocación inmediata |

## 8. Preguntas Frecuentes

### P: ¿Por qué Keycloak no almacena rutinas?
**R:** Keycloak es especializado en **identidad**. IFit es especializado en **lógica de negocio**. Separar ambas es más mantenible: puedes cambiar Keycloak sin afectar IFit, y viceversa.

### P: ¿Qué pasa si el access_token expira pero el refresh_token aún es válido?
**R:** El cliente (MAUI) recibe 401 Unauthorized. Su middleware intercepta, hace POST /auth/refresh, obtiene nuevo access_token, y reintenta la petición original. Todo automático.

### P: ¿Y si ambos tokens expiran?
**R:** El usuario debe login de nuevo. Esto es normal; después de ~24h sin actividad, el usuario re-autentica para seguridad.

### P: ¿Puede IFit emitir tokens propios?
**R:** **No debe**. IFit es Resource Server, no Authorization Server. Si emitiese tokens propios, sería inseguro (el cliente tendría dos tokens con distintas claves públicas). Keycloak es la fuente única de verdad.

### P: ¿Por qué no usar sesiones HTTP tradicionales?
**R:** Las sesiones HTTP (server-side) requieren estado compartido entre servidores. Con múltiples instancias de IFit, necesitarías una base de datos de sesiones. Tokens JWT son stateless: cualquier servidor puede validarlos sin coordinar.
