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
| Identidades | Keycloak 21.1.2 (admin-client) |
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

### Training — Rutinas y Generación IA con Ronnie

Este módulo orquesta la generación de rutinas personalizadas mediante el motor de IA (Ronnie) y gestiona el ciclo de vida completo de las rutinas de entrenamiento. Es el corazón del sistema: transforma respuestas del cuestionario en planes de entrenamiento tangibles.

#### 1. Arquitectura y Flujo General

```
Usuario completa cuestionario
    ↓
POST /routines/generate { userId, responseId, coachType, note }
    ↓
RoutineService obtiene resumen del cuestionario
    ↓
Construye prompt personalizado (perfil + respuestas + contexto coach)
    ↓
IFitAIClient llama POST /ronnie/generate-routine a Ronnie
    ↓
Ronnie (LangChain4j + Groq) genera rutina JSON
    ↓
Service reconcilia nombres de ejercicios con catálogo local
    ↓
Retorna RoutineResponseDto (transitorio, SIN persistencia en BD)
    ↓
Cliente puede guardar con POST /routines { days: [...] }
    ↓
Service persiste Routine + RoutineDay + RoutineExercise en MySQL
```

**Nota crítica:** `POST /routines/generate` NO guarda en BD; solo devuelve un preview. El cliente debe confirmar y guardar.

---

#### 2. Modelos/Entidades

##### Routine (Entidad Principal)

```java
public class Routine {
    Long id;                              // PK
    AppUser user;                         // FK - propietario
    String description;                   // Descripción de la rutina (nullable)
    Integer trainingDays;                 // Número total de días (1-7)
    Boolean isActive;                     // true = rutina actual del usuario (default: true)
    Boolean deleted;                      // Soft-delete flag (default: false)
    Integer currentDay;                   // Día actual en ejecución (default: 1)
    LocalDateTime createdAt;              // Timestamp de creación
    LocalDateTime updatedAt;              // Timestamp de última actualización
    List<RoutineDay> days;                // OneToMany con cascade=ALL, orphanRemoval=true
}
```

**Campos críticos:**
- `isActive`: Solo una rutina activa por usuario; al crear nueva se desactivan anteriores
- `deleted`: Soft-delete para cancelar sin eliminar historial
- `currentDay`: Seguimiento de progreso (1 → N → 1 cíclico)
- `days`: Relación cascada; al eliminar Routine se eliminan todos sus días y ejercicios

**Métodos:**
- `addDay(RoutineDay)` / `removeDay(RoutineDay)`: Gestionar relación bidireccional
- Constructor con user, description, trainingDays

---

##### RoutineDay (Días de la Rutina)

```java
public class RoutineDay {
    Long id;                              // PK
    Routine routine;                      // FK - rutina padre (lazy)
    Integer dayNumber;                    // 1 a trainingDays
    String dayName;                       // Nombre descriptivo (ej: "Tren Push")
    String description;                   // Descripción del día
    List<RoutineExercise> exercises;      // OneToMany con cascade=ALL, orphanRemoval=true
}
```

**Relación con Routine:**
- OneToMany con FK routine_id
- Cascada completa: al eliminar día se eliminan ejercicios

---

##### RoutineExercise (Ejercicios)

```java
public class RoutineExercise {
    Long id;                              // PK
    RoutineDay routineDay;                // FK - día padre
    String exerciseName;                  // Nombre (reconciliado con catálogo)
    Long exerciseId;                      // FK opcional a exercise_catalog
    Integer sets;                         // Series (nullable)
    String reps;                          // Repeticiones: "8-12", "AMRAP", etc. (nullable)
    Integer restSeconds;                  // Descanso entre series (nullable)
    String notes;                         // Notas/recomendaciones (nullable)
    Integer orderIndex;                   // Orden dentro del día (nullable)
}
```

**Campos críticos:**
- `exerciseId`: Puede ser NULL si ejercicio no se encuentra en catálogo local
- `exerciseName`: Nombre canónico tras reconciliación (si se encuentra); original si no
- Todos los campos excepto `exerciseName` son nullable

---

##### CoachType (Enum de Coaches de IA)

```java
enum CoachType {
    MASTER {
        displayName = "Master"
        systemContext = null              // ⚠️ DEPRECATED - no usar
    },
    
    RONNIE {
        displayName = "Ronnie"
        systemContext = """
        Eres Ronnie, especializado en hipertrofia y fuerza muscular.
        Diseña rutinas con ejercicios de musculación clásica:
        press de banca, sentadilla, peso muerto, remo, dominadas...
        Alto volumen adaptado al nivel del usuario.
        El catálogo disponible es la única fuente válida de ejercicios.
        """
    },
    
    ELIUD {
        displayName = "Eliud"
        systemContext = """
        Eres Eliud, especializado en running, cardio y resistencia.
        Diseña rutinas con ejercicios funcionales para corredores:
        sentadillas, zancadas, puente de glúteos, core, trabajo aeróbico...
        Progresión gradual sin sobrecargar articulaciones.
        El catálogo disponible es la única fuente válida.
        """
    },
    
    SERENA {
        displayName = "Serena"
        systemContext = """
        Eres Serena, especializada en fitness femenino y bienestar.
        Diseña rutinas full body accesibles con énfasis en glúteos y core:
        sentadillas, puente de glúteos, plancha, bird-dog, estiramientos...
        Sesiones de 30-45 minutos, empáticas y positivas.
        El catálogo disponible es la única fuente válida.
        """
    },
    
    KAEL {
        displayName = "Kael"
        systemContext = """
        Eres Kael, especialista en calistenia y fuerza sin equipamiento.
        Diseña rutinas con ejercicios de peso corporal:
        flexiones, fondos, dominadas, sentadillas, plancha, HIIT...
        Entrenamientos realizables en casa o parque.
        El catálogo disponible es la única fuente válida.
        """
    }
}
```

**Métodos:**
- `getDisplayName()`: Nombre para logs
- `getSystemContext()`: Inyectado en prompt (null para MASTER)
- `getEndpointPath()`: Retorna `"/" + displayName.toLowerCase() + "/generate-routine"`

**Endpoints HTTP:**
- RONNIE → `/ronnie/generate-routine`
- ELIUD → `/eliud/generate-routine`
- SERENA → `/serena/generate-routine`
- KAEL → `/kael/generate-routine`
- MASTER → `/master/generate-routine` (deprecated)

---

#### 3. DTOs (Data Transfer Objects)

##### Request DTOs

**GenerateRoutineRequestDto** (POST /routines/generate)
```java
@NotNull Long userId
@NotNull Long responseId                 // ID de sesión de cuestionario completada
CoachType coachType                      // default: MASTER
String note                              // Nota especial del usuario (nullable)
```

**CreateRoutineRequestDto** (POST /routines - creación manual)
```java
@NotNull Long userId
@Size(max=1000) String description       // Descripción libre
@NotNull @Min(1) Integer trainingDays    // Número de días
@NotEmpty @Valid List<RoutineDayDto> days // Estructura completa
```

**UpdateRoutineRequestDto** (PUT /routines/{id})
```java
@Size(max=1000) String description       // nullable - solo si se proporciona
@Min(1) Integer trainingDays             // nullable
Boolean isActive                         // nullable
@Valid List<RoutineDayDto> days          // nullable - reemplaza si proporcionado
```

**RonnieMessageDto** (Payload HTTP a Ronnie)
```java
int memoryId                             // ID de conversación en LangChain4j
String message                           // El prompt construido
String userId                            // keycloakUserId del usuario
```

---

##### Response DTOs

**RoutineResponseDto** (retornado por todos los endpoints)
```java
Long id                                  // nullable si es transitorio (de /generate)
Long userId
String message                           // Hardcoded: "Bienvenido a tu nueva rutina..."
String description
Integer trainingDays
Boolean isActive
Boolean deleted
Integer currentDay                       // Día actual en ejecución
LocalDateTime createdAt
LocalDateTime updatedAt
List<RoutineDayDto> days
```

**Nota:** El `message` es **siempre** el mismo texto motivacional inyectado por `RoutineMapper`, NO viene de la IA.

**RoutineDayDto**
```java
Long id
Integer dayNumber                        // 1 a trainingDays
String dayName                           // "Día 1 - Push", etc.
String description
List<RoutineExerciseDto> exercises
```

**RoutineExerciseDto**
```java
Long id
String exerciseName                      // Nombre final (canónico si reconciliado)
Long exerciseId                          // nullable - link al catálogo
Integer sets
String reps                              // "8-12", "AMRAP", etc.
Integer restSeconds
String notes
Integer orderIndex
```

---

##### DTOs de Ronnie (Client)

**IFitAIRoutineResponseDto** (respuesta de `/ronnie/generate-routine`)
```java
String message                           // Texto motivacional de la IA
String description                       // Resumen de la rutina generada
Integer trainingDays
List<IFitAIRoutineDayDto> days
```

**IFitAIRoutineDayDto**
```java
Integer dayNumber
String dayName
String description
List<IFitAIRoutineExerciseDto> exercises
```

**IFitAIRoutineExerciseDto** (nombres BRUTOS de IA)
```java
String exerciseName                      // Ej: "Flexiones de pecho"
Integer sets
String reps
Integer restSeconds
String notes
Integer orderIndex
// Nota: sin exerciseId; se asigna después en normalizeExerciseNames()
```

**IFitAIMaxMemoryIdResponseDto**
```java
Integer maxMemoryId
```

---

#### 4. Flujo Detallado: Generación de Rutina con IA

##### A. Request HTTP

```
POST /routines/generate
Content-Type: application/json

{
  "userId": 123,
  "responseId": 456,
  "coachType": "RONNIE",
  "note": "Quiero enfatizar en brazos"
}
```

##### B. RoutineController → RoutineService

```java
@PostMapping("/generate")
@Transactional
RoutineResponseDto generateRoutine(@RequestBody GenerateRoutineRequestDto requestDto) {
    return routineService.generateRoutine(
        requestDto.userId(),
        requestDto.responseId(),
        requestDto.coachType(),
        requestDto.note()
    );
}
```

##### C. RoutineService.generateRoutine() - Fase 1: Validación y Obtención de Datos

```java
@Transactional
public RoutineResponseDto generateRoutine(
    Long userId, Long responseId, CoachType coachType, String note) {
    
    // 1. Validación
    if (userId == null) throw new IllegalArgumentException("userId is required");
    if (responseId == null) throw new IllegalArgumentException("responseId is required");
    
    // 2. Obtener usuario
    AppUser user = appUserRepository.findById(userId)
        .orElseThrow(() -> new UserIdNotFoundException("User not found"));
    
    // 3. Normalizar coach (si null → MASTER)
    CoachType resolvedCoach = coachType != null ? coachType : CoachType.MASTER;
    
    // 4. Obtener resumen del cuestionario
    QuestionnaireResponseSummaryDto summary = 
        questionnaireService.getResponseSummary(responseId);
    // Retorna: {
    //   responseId, userId, userName, questionnaireId, questionnaireName,
    //   answers: [ {questionText, selectedOption, additionalText, ...}, ... ],
    //   isCompleted, startedAt, completedAt
    // }
```

##### D. RoutineService.generateRoutine() - Fase 2: Construcción del Prompt

```java
    // 5. Construir prompt
    String prompt = buildRoutinePrompt(user, summary, note);
    
    // ESTRUCTURA DEL PROMPT GENERADO:
    /*
    PERFIL DEL USUARIO:
    Nombre: Juan Garcia
    Nivel de experiencia: Principiante
    Catálogo a usar: BEGINNER
    
    Cuestionario: Ronnie - Fuerza para Principiantes
    Descripción: Rutina de fuerza para usuarios nuevos...
    
    RESPUESTAS AL CUESTIONARIO:
    - ¿Cuál es tu objetivo principal?
      Respuesta: Ganar músculo
    
    - ¿Tienes lesiones o limitaciones?
      Respuesta: [No respondida]
    
    - ¿Frecuencia de entrenamiento?
      Respuesta: 3-4 veces por semana
      Detalle: Prefiero lunes, miércoles, viernes
    
    NOTA ESPECIAL DEL USUARIO:
    Quiero enfatizar en brazos
    */
```

**Mapeo de niveles:**
- "Principiante" → "BEGINNER"
- "Intermedio" → "INTERMEDIATE"
- "Avanzado" → "ADVANCED"

**Manejo de "Prefiero no responder":**
- Si `selectedOption.text == "Prefiero no responder"` → reemplaza por `"[No respondida]"`

##### E. RoutineService.generateRoutine() - Fase 3: Llamada a Ronnie

```java
    // 6. Obtener memoryId
    int memoryId = aiClient.getMaxMemoryId().getMaxMemoryId();
    // maxMemoryId = 10, entonces: memoryId = 11
    
    // 7. Llamar a Ronnie
    RoutineResponseDto routineResponseDto = aiClient.generateRoutine(
        memoryId,                    // 11
        prompt,                      // string construido
        user.getKeycloakUserId(),   // "uuid-user-id"
        resolvedCoach                // CoachType.RONNIE
    );
    // HTTP: POST http://localhost:8082/ronnie/generate-routine
    // Body: { memoryId: 11, message: prompt, userId: "uuid-..." }
    // Response: IFitAIRoutineResponseDto
```

**HTTP Request a Ronnie:**
```
POST http://localhost:8082/ronnie/generate-routine
Content-Type: application/json
Authorization: Bearer {jwt-token}

{
  "memoryId": 11,
  "message": "[prompt completo de 500-2000 chars]",
  "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

**HTTP Response de Ronnie:**
```json
{
  "message": "¡Vamos a hacer que tú también seas un campeón!...",
  "description": "Rutina Push-Pull-Legs especializada...",
  "trainingDays": 3,
  "days": [
    {
      "dayNumber": 1,
      "dayName": "Día 1 - Push",
      "description": "Enfocado en pecho, hombros, tríceps...",
      "exercises": [
        {
          "exerciseName": "Flexiones de pecho",
          "sets": 3,
          "reps": "8-12",
          "restSeconds": 90,
          "notes": "Mantén espalda recta",
          "orderIndex": 0
        },
        ...
      ]
    },
    ...
  ]
}
```

##### F. RoutineService.generateRoutine() - Fase 4: Normalización de Ejercicios

```java
    // 8. Reconciliación de nombres de ejercicios
    normalizeExerciseNames(routineResponseDto);
    
    // PSEUDOCÓDIGO:
    for (RoutineDayDto day : routineResponseDto.getDays()) {
        for (RoutineExerciseDto exercise : day.getExercises()) {
            String rawName = exercise.getExerciseName();  // "Flexiones de pecho"
            
            Optional<ExerciseResolution> match = 
                exerciseNameResolver.resolve(rawName);
            
            if (match.isPresent()) {
                String canonical = match.get().getCanonicalName();  // "Push-up de pecho"
                Long catalogId = match.get().getId();               // 42
                
                exercise.setExerciseName(canonical);
                exercise.setExerciseId(catalogId);
                
                logger.info("Normalized: '{}' → '{}' (id: {})", 
                    rawName, canonical, catalogId);
            } else {
                logger.warn("Not found in catalog: '{}'", rawName);
                // exercise.setExerciseName() mantiene original
                // exercise.setExerciseId() queda null
            }
        }
    }
    
    // LOG FINAL:
    logger.info("Catalog reconciliation: 12/13 exercises linked to catalog");
```

##### G. RoutineService.generateRoutine() - Fase 5: Retorno (SIN Persistencia)

```java
    // 9. Retornar (NO se guarda en BD aquí)
    routineResponseDto.setUserId(userId);
    return routineResponseDto;
    
    // RESPUESTA AL CLIENTE:
    // {
    //   "userId": 123,
    //   "message": "Bienvenido a tu nueva rutina...",  ← Hardcoded en mapper
    //   "description": "Rutina Push-Pull-Legs...",
    //   "trainingDays": 3,
    //   "isActive": false,
    //   "deleted": false,
    //   "currentDay": null,
    //   "createdAt": null,
    //   "updatedAt": null,
    //   "days": [ { "dayNumber": 1, "dayName": "Día 1 - Push", ... }, ... ]
    // }
}
```

**Nota crítica:** `id`, `createdAt` son `null` porque aún no se ha persistido en BD.

---

#### 5. Persistencia Manual (Cliente Debe Guardar)

Si el cliente acepta la rutina generada, debe hacer:

```
POST /routines
Content-Type: application/json

{
  "userId": 123,
  "description": "Rutina Push-Pull-Legs personalizada",
  "trainingDays": 3,
  "days": [
    {
      "dayNumber": 1,
      "dayName": "Día 1 - Push",
      "description": "...",
      "exercises": [
        {
          "exerciseName": "Push-up de pecho",
          "exerciseId": 42,
          "sets": 3,
          "reps": "8-12",
          "restSeconds": 90,
          "notes": "...",
          "orderIndex": 0
        },
        ...
      ]
    },
    ...
  ]
}
```

**RoutineService.createRoutine():**
```java
@Transactional
public RoutineResponseDto createRoutine(CreateRoutineRequestDto requestDto) {
    // 1. Validar usuario existe
    AppUser user = appUserRepository.findById(requestDto.userId())
        .orElseThrow(() -> new UserIdNotFoundException("User not found"));
    
    // 2. Desactivar rutinas anteriores del usuario
    List<Routine> activeRoutines = routineRepository
        .findByUserIdAndIsActiveAndDeletedFalse(user.getId(), true);
    activeRoutines.forEach(r -> r.setIsActive(false));
    routineRepository.saveAll(activeRoutines);
    
    // 3. Crear Routine
    Routine routine = new Routine();
    routine.setUser(user);
    routine.setDescription(requestDto.description());
    routine.setTrainingDays(requestDto.trainingDays());
    routine.setIsActive(true);
    routine.setDeleted(false);
    routine.setCurrentDay(1);
    routine.setCreatedAt(LocalDateTime.now());
    
    // 4. Crear RoutineDay + RoutineExercise (cascada)
    for (RoutineDayDto dayDto : requestDto.days()) {
        RoutineDay day = new RoutineDay();
        day.setRoutine(routine);
        day.setDayNumber(dayDto.dayNumber());
        day.setDayName(dayDto.dayName());
        day.setDescription(dayDto.description());
        
        for (RoutineExerciseDto exDto : dayDto.exercises()) {
            RoutineExercise exercise = new RoutineExercise();
            exercise.setRoutineDay(day);
            exercise.setExerciseName(exDto.exerciseName());
            exercise.setExerciseId(exDto.exerciseId());  // puede ser null
            exercise.setSets(exDto.sets());
            exercise.setReps(exDto.reps());
            exercise.setRestSeconds(exDto.restSeconds());
            exercise.setNotes(exDto.notes());
            exercise.setOrderIndex(exDto.orderIndex());
            
            day.addExercise(exercise);
        }
        
        routine.addDay(day);
    }
    
    // 5. Persistir (cascada guardará todo)
    Routine saved = routineRepository.save(routine);
    
    // 6. Mapear y retornar
    return routineMapper.toResponseDto(saved);  // Ahora sí tiene ID, createdAt, etc.
}
```

**Respuesta (201 Created):**
```json
{
  "id": 999,
  "userId": 123,
  "message": "Bienvenido a tu nueva rutina...",
  "description": "Rutina Push-Pull-Legs personalizada",
  "trainingDays": 3,
  "isActive": true,
  "deleted": false,
  "currentDay": 1,
  "createdAt": "2025-06-21T10:30:00",
  "updatedAt": null,
  "days": [...]
}
```

Ahora `id` y `createdAt` están poblados.

---

#### 6. Endpoints Completos (18 Total)

##### A. Creación

| Método | Endpoint | Request | Response | Auth | Descripción |
|---|---|---|---|---|---|
| **POST** | `/routines/generate` | GenerateRoutineRequestDto | RoutineResponseDto (transitorio) | None | Genera rutina con IA (NO persiste) |
| **POST** | `/routines` | CreateRoutineRequestDto | RoutineResponseDto (persistido) | None | Crea rutina manual o confirma generada |

##### B. Lectura

| Método | Endpoint | Parámetros | Response | Auth | Descripción |
|---|---|---|---|---|---|
| **GET** | `/routines` | None | List<RoutineResponseDto> | **admin_client_role** | Todas las rutinas (admin) |
| **GET** | `/routines/paginated` | page, size, sortBy, sortDir (query) | Page<RoutineResponseDto> | None | Con paginación |
| **GET** | `/routines/{id}` | id (path) | RoutineResponseDto | None | Por ID con fetchJoin (evita N+1) |
| **GET** | `/routines/user/{userId}` | userId (path) | List<RoutineResponseDto> | None | Todas del usuario (no deletadas) |
| **GET** | `/routines/user/{userId}/paginated` | userId, page, size, sortBy, sortDir | Page<RoutineResponseDto> | None | Paginadas del usuario |
| **GET** | `/routines/user/{userId}/active` | userId (path) | List<RoutineResponseDto> | None | Solo activas (isActive=true, deleted=false) |
| **GET** | `/routines/user/{userId}/count-active` | userId (path) | Long | None | Contador de activas |
| **GET** | `/routines/{routineId}/day/{day}` | routineId, day (path) | RoutineDayDto | None | Detalle de un día (para UI de progreso) |

##### C. Actualización

| Método | Endpoint | Request | Response | Auth | Descripción |
|---|---|---|---|---|---|
| **PUT** | `/routines/{id}` | UpdateRoutineRequestDto | RoutineResponseDto | **admin_client_role** | Actualiza (campos no null) |
| **PATCH** | `/routines/{id}/toggle-active` | isActive (query) | RoutineResponseDto | None | Activa/desactiva; desactiva otras del usuario si activa=true |
| **PATCH** | `/routines/{id}/cancel` | None | RoutineResponseDto | None | Soft-delete; lanza RoutineIsActiveException si isActive=true |

##### D. Progreso/Seguimiento

| Método | Endpoint | Request | Response | Auth | Descripción |
|---|---|---|---|---|---|
| **POST** | `/routines/{routineId}/day/{day}/complete` | routineId, day (path) | RoutineResponseDto | None | Marca día completado; avanza currentDay cíclicamente |
| **POST** | `/routines/{routineId}/complete` | routineId (path) | RoutineResponseDto | None | Marca rutina completada; desactiva (setActive=false) |

**Lógica de `day/complete`:**
```java
// Si day == maxDay: currentDay = 1 (reinicia ciclo)
// Si day < maxDay: currentDay = day + 1 (avanza)
```

##### E. Eliminación

| Método | Endpoint | Request | Response | Auth | Descripción |
|---|---|---|---|---|---|
| **DELETE** | `/routines/{id}` | id (path) | 204 No Content | None | Hard-delete (irreversible) |

---

#### 7. Mappers (Conversión Entity ↔ DTO)

Los mappers convierten entidades JPA a DTOs para serialización HTTP. Notar que el `message` es inyectado en mapper, no viene de IA:

```java
@Component
public class RoutineMapper {
    
    public RoutineResponseDto toResponseDto(Routine routine) {
        return new RoutineResponseDto(
            routine.getId(),
            routine.getUser().getId(),
            "Bienvenido a tu nueva rutina personalizada. Esta rutina ha sido diseñada "
            + "específicamente para ti, teniendo en cuenta tus objetivos, nivel de "
            + "experiencia y preferencias. Asegúrate de seguirla de manera consistente "
            + "para obtener los mejores resultados. ¡Vamos a por ello!",  // ← HARDCODED
            routine.getDescription(),
            routine.getTrainingDays(),
            routine.getIsActive(),
            routine.getDeleted(),
            routine.getCurrentDay(),
            routine.getCreatedAt(),
            routine.getUpdatedAt(),
            routine.getDays().stream()
                .map(routineDayMapper::toDto)
                .collect(Collectors.toList())
        );
    }
    
    public Page<RoutineResponseDto> toResponseDtoPage(Page<Routine> page) {
        return page.map(this::toResponseDto);
    }
}
```

---

#### 8. Validaciones y Excepciones

| Operación | Validación | Excepción |
|---|---|---|
| `generateRoutine()` | userId != null | IllegalArgumentException |
| `generateRoutine()` | responseId != null | IllegalArgumentException |
| `createRoutine()` | userId existe | UserIdNotFoundException |
| `findRoutineById()` | id existe | RoutineNotFoundException |
| `updateRoutine()` | id existe | RoutineNotFoundException |
| `toggleRoutineActive()` | id existe | RoutineNotFoundException |
| `softDeleteRoutine()` | id existe | RoutineNotFoundException |
| `softDeleteRoutine()` | isActive = false | **RoutineIsActiveException** (no puede cancelar activa) |
| `deleteRoutine()` | id existe | RoutineNotFoundException |

**RoutineIsActiveException:**
```
Mensaje: "La rutina con ID {id} es la rutina activa del usuario {userId} y no puede ser eliminada. 
Desactívala primero con PATCH /routines/{id}/toggle-active?isActive=false"
```

**Errores en IFitAIClient (HTTP a Ronnie):**
```
HttpClientErrorException (4xx) → RuntimeException("Error calling Ronnie service: ...")
HttpServerErrorException (5xx) → RuntimeException("Ronnie service error: ...")
Exception (any)                → RuntimeException("Failed to generate routine: ...")
```

---

#### 9. Consultas de Repositorio Optimizadas

**RoutineRepository:**
```java
// Con fetchJoin para evitar N+1 en serialización
@Query("SELECT DISTINCT r FROM Routine r "
       + "LEFT JOIN FETCH r.days d "
       + "LEFT JOIN FETCH d.exercises "
       + "WHERE r.id = :routineId AND r.deleted = false")
Optional<Routine> findByIdWithDaysAndExercises(Long routineId)

// Filtra por usuario y estado
List<Routine> findByUserIdAndDeletedFalse(Long userId)
List<Routine> findByUserIdAndIsActiveAndDeletedFalse(Long userId, boolean isActive)
long countByUserIdAndIsActiveAndDeletedFalse(Long userId, boolean isActive)
```

---

#### 10. Integración con Otros Módulos

**Questionnaire:**
```java
QuestionnaireService.getResponseSummary(responseId)
// → QuestionnaireResponseSummaryDto { answers, userId, startedAt, ... }
```

**User:**
```java
AppUserRepository.findById(userId)
// → AppUser { keycloakUserId, name, experienceLevel, ... }
```

**Exercises:**
```java
ExerciseNameResolver.resolve(exerciseName)
// → Optional<ExerciseResolution> { id, canonicalName }
// Usado en normalizeExerciseNames() para linkear catálogo
```

**Coach:**
```java
CoachType.getSystemContext()  // inyectado en prompt
CoachType.getEndpointPath()   // construye URL a Ronnie
```

---

#### 11. Ejemplos de Uso

##### Generar rutina con IA:

```bash
curl -X POST http://localhost:8081/routines/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {jwt-token}" \
  -d '{
    "userId": 123,
    "responseId": 456,
    "coachType": "RONNIE",
    "note": "Enfatizar brazos"
  }'

# Response: RoutineResponseDto (transitorio, sin id)
```

##### Guardar rutina generada:

```bash
curl -X POST http://localhost:8081/routines \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {jwt-token}" \
  -d '{
    "userId": 123,
    "description": "Rutina Ronnie Push-Pull-Legs",
    "trainingDays": 3,
    "days": [...]
  }'

# Response: RoutineResponseDto (persistido, con id)
```

##### Marcar día completado:

```bash
curl -X POST http://localhost:8081/routines/999/day/1/complete \
  -H "Authorization: Bearer {jwt-token}"

# currentDay avanza: 1 → 2
```

---

#### 12. Comparación: Diagrama Antes vs Después (en README)

| Aspecto | Documentación Anterior | Implementación Real |
|---|---|---|
| Persistencia de `/generate` | Sugiere que guarda | NO guarda; solo preview |
| DTOs | No menciona diferencias | GenerateRoutineRequestDto vs CreateRoutineRequestDto |
| Coach MASTER | "Deprecated, no usar" | Aún en enum, sin systemContext |
| Message de respuesta | No menciona origen | Hardcoded en mapper |
| Reconciliación ejercicios | "En construcción" | Totalmente implementada |
| Campos Routine | Menciona solo básicos | Incluye currentDay, deleted, description, timestamps |
| Endpoints | 14 documentados | 18 implementados |
| Soft-delete | No menciona | PATCH `/cancel` + campo `deleted` |
| Progreso | No documenta | `currentDay` cíclico; `/day/complete`; `/complete` |

---

#### Endpoints del módulo Training

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/routines/generate` | Genera rutina personalizada con IA (sin persistencia). |
| POST | `/routines` | Crea rutina manualmente o confirma generada. |
| GET | `/routines` | Lista todas las rutinas (admin). |
| GET | `/routines/paginated` | Lista paginada con ordenamiento. |
| GET | `/routines/{id}` | Obtiene rutina por ID. |
| GET | `/routines/user/{userId}` | Rutinas de un usuario. |
| GET | `/routines/user/{userId}/paginated` | Rutinas paginadas de un usuario. |
| GET | `/routines/user/{userId}/active` | Solo rutinas activas. |
| GET | `/routines/user/{userId}/count-active` | Número de rutinas activas. |
| GET | `/routines/{routineId}/day/{day}` | Detalle de un día específico. |
| PUT | `/routines/{id}` | Actualiza rutina (admin). |
| PATCH | `/routines/{id}/toggle-active` | Activa o desactiva rutina. |
| PATCH | `/routines/{id}/cancel` | Soft-delete (marca como eliminada). |
| POST | `/routines/{routineId}/day/{day}/complete` | Marca día completado (avanza progreso). |
| POST | `/routines/{routineId}/complete` | Marca rutina completada (desactiva). |
| DELETE | `/routines/{id}` | Elimina rutina permanentemente. |

---

### User — Gestión de Usuarios

El módulo de usuario gestiona la entidad `AppUser`, que es la representación local del usuario en la base de datos de IFit (separada del registro en Keycloak). Incluye asignación de roles, coaches y niveles de experiencia.

#### Entidad AppUser

```java
public class AppUser {
    Long id;                              // PK
    String name;                          // Nombre completo
    String email;                         // UNIQUE - username en Keycloak
    String password;                      // Hash BCrypt (copia local)
    String keycloakUserId;                // UUID del usuario en Keycloak
    Boolean isVerified;                   // Email verificado (default: false)
    String verificationCode;              // Código temporal de 6 dígitos
    LocalDateTime verificationCodeExpiresAt;  // Expiración (15 min)
    Boolean isRegistrationComplete;       // Onboarding completado (default: false)
    AppRole role;                         // FK → AppRole (USER o ADMIN)
    CoachModelType coachModelType;        // FK → Coach asignado (nullable)
    ExperienceLevel experienceLevel;      // FK → Nivel asignado (nullable)
    LocalDateTime createdAt;              // Timestamp auto
    LocalDateTime updatedAt;              // Timestamp auto
}
```

**Relaciones:**
- N:1 con AppRole (siempre presente)
- N:1 con CoachModelType (nullable - se asigna en onboarding)
- N:1 con ExperienceLevel (nullable - se asigna en onboarding)
- 1:N con Routine (un usuario puede tener múltiples rutinas)
- 1:N con QuestionnaireResponse (un usuario puede responder cuestionarios)

#### Endpoints de AppUserController

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| GET | `/users` | JWT | Lista todos los usuarios. |
| GET | `/users/paginated` | JWT | Lista paginada (page, size, sortBy, sortDir). |
| GET | `/users/{id}` | JWT | Por ID. |
| GET | `/users/email/{email}` | JWT | Por email (búsqueda). |
| GET | `/users/exists/email/{email}` | JWT | Verifica si email existe (boolean). |
| POST | `/users` | JWT | Crea usuario manualmente (admin). |
| PUT | `/users/{id}` | JWT | Actualiza datos del usuario. |
| PATCH | `/users/{userId}/assign-coach/{coachId}` | JWT | Asigna coach de IA. |
| PATCH | `/users/{userId}/assign-experience/{levelId}` | JWT | Asigna nivel de experiencia. |
| PATCH | `/users/{userId}/complete-registration` | JWT | Marca onboarding completado. |
| DELETE | `/users/{id}` | JWT | Elimina usuario. |

#### DTOs del módulo User

**AppUserResponseDto** (respuesta de lectura)
```java
Long id
String name
String email
String keycloakUserId
Boolean isVerified
Boolean isRegistrationComplete
String role                 // nombre del rol
String coachModelType       // nombre del coach (nullable)
String experienceLevel      // nombre del nivel (nullable)
LocalDateTime createdAt
LocalDateTime updatedAt
```

**CreateAppUserRequestDto** (POST /users)
```java
@NotBlank String name
@NotBlank @Email String email
@NotBlank String password
Long roleId                 // FK a AppRole
Long coachModelTypeId       // FK opcional
Long experienceLevelId      // FK opcional
```

**UpdateAppUserRequestDto** (PUT /users/{id})
```java
String name                 // nullable
String email                // nullable
Long roleId                 // nullable
Long coachModelTypeId       // nullable
Long experienceLevelId      // nullable
Boolean isRegistrationComplete  // nullable
```

#### Niveles de Experiencia (ExperienceLevel)

La entidad `ExperienceLevel` define los niveles que un usuario puede tener:

```java
public class ExperienceLevel {
    Long id;                              // PK
    String name;                          // UNIQUE (Principiante, Intermedio, Avanzado)
    String description;                   // Descripción
    LocalDateTime createdAt;              // Auto
}
```

**Valores típicos predefinidos:**
- ID 1: Principiante
- ID 2: Intermedio
- ID 3: Avanzado

#### Endpoints de ExperienceLevelController

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| GET | `/experience-levels` | JWT | Lista todos los niveles. |
| GET | `/experience-levels/{id}` | JWT | Por ID. |
| POST | `/experience-levels` | admin_client_role | Crea nivel. |
| PATCH | `/experience-levels/{id}` | admin_client_role | Actualiza nivel. |
| DELETE | `/experience-levels/{id}` | admin_client_role | Elimina nivel. |

#### Roles (AppRole)

La tabla `approle` contiene los roles del sistema:

```java
public class AppRole {
    Long id;                              // PK
    String name;                          // UNIQUE (USER, ADMIN)
    String description;
}
```

**Valores predefinidos:**
- ID 1: USER (rol estándar de usuario)
- ID 2: ADMIN (acceso a endpoints administrativos)

---

### Coach — Tipos de Coach

La tabla `coachmodeltype` almacena los coaches disponibles en la plataforma. Esta entidad es el registro maestro de coaches; el comportamiento real de cada coach se define en el microservicio Ronnie.

#### Entidad CoachModelType

```java
public class CoachModelType {
    Long id;                              // PK
    String name;                          // UNIQUE
    String description;                   // Descripción del coach
    String emojiCharacter;                // Emoji representativo (ej: "💪")
    Boolean enabled;                      // Soft-delete flag (default: true)
    LocalDateTime createdAt;              // Auto
    LocalDateTime updatedAt;              // Auto
}
```

**Valores Predefinidos:**
- Ronnie (💪): Hipertrofia y fuerza
- Eliud (🏃): Running y cardio
- Serena (🎯): Fitness femenino y bienestar
- Kael (🤸): Calistenia y street workout

#### Soft Delete

El endpoint `DELETE /coach-models/{id}` **no elimina** el registro. En su lugar:
- Pone `enabled = false`
- El coach desaparece de listados públicos (`GET /coach-models`)
- El coach sigue siendo accesible para usuarios que ya lo tienen asignado
- Puede rehabilitarse con `PATCH /coach-models/{id}/enable`

**Ventaja:** No rompe la integridad referencial de usuarios y rutinas que usaban ese coach.

#### Endpoints de CoachModelTypeController

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| GET | `/coach-models` | JWT | Lista coaches habilitados (enabled=true). |
| GET | `/coach-models/all` | admin_client_role | Lista todos (incluye deshabilitados). |
| GET | `/coach-models/{id}` | JWT | Por ID. |
| GET | `/coach-models/name/{name}` | JWT | Por nombre exacto. |
| POST | `/coach-models` | admin_client_role | Crea nuevo coach. |
| PUT | `/coach-models/{id}` | admin_client_role | Actualiza coach. |
| DELETE | `/coach-models/{id}` | admin_client_role | Deshabilita (soft delete, enabled=false). |
| PATCH | `/coach-models/{id}/enable` | admin_client_role | Habilita coach deshabilitado (enabled=true). |

#### DTOs

**CoachModelTypeDto** (respuesta)
```java
Long id
String name
String description
String emojiCharacter
Boolean enabled
LocalDateTime createdAt
LocalDateTime updatedAt
```

**CreateCoachModelTypeRequestDto** (POST)
```java
@NotBlank String name
@NotBlank String description
String emojiCharacter
```

**UpdateCoachModelTypeRequestDto** (PUT)
```java
String name                 // nullable
String description          // nullable
String emojiCharacter       // nullable
Boolean enabled             // nullable
```

---

### Exercises — Catálogo de Ejercicios

El catálogo de ejercicios es una copia de los ejercicios importados desde el microservicio Ronnie. Proporciona información de ejercicios con imágenes, categorías, equipamiento requerido y músculos trabajados.

#### Entidad Exercise

```java
public class Exercise {
    Long id;                              // PK
    String name;                          // Nombre único (ej: "Push-up de pecho")
    String description;                   // Descripción
    String instructions;                  // Instrucciones detalladas
    String level;                         // BEGINNER, INTERMEDIATE, ADVANCED
    String category;                      // STRENGTH, CARDIO, FLEXIBILITY, HIIT, etc.
    String equipment;                     // BODYWEIGHT, BARBELL, DUMBBELLS, CABLE, etc.
    List<String> muscleGroups;            // Músculos: ["pecho", "tríceps", "hombro"]
    String imageUrl;                      // URL de imagen
    LocalDateTime createdAt;              // Auto
}
```

#### Endpoints de ExerciseCatalogController

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| GET | `/exercises` | JWT | Lista paginada con filtros opcionales. |
| GET | `/exercises/{id}` | JWT | Detalle completo de un ejercicio. |

#### Parámetros de Búsqueda/Filtro

**Query parameters para GET /exercises:**

```
?page=0&size=20&sortBy=name&sortDir=ASC
&level=BEGINNER
&category=STRENGTH
&equipment=BARBELL
&muscle=pecho
```

| Parámetro | Tipo | Valores | Descripción |
|---|---|---|---|
| `page` | int | 0, 1, 2, ... | Página (default: 0) |
| `size` | int | 10, 20, 50 | Registros por página (default: 20) |
| `sortBy` | string | name, level, category | Campo de ordenamiento |
| `sortDir` | string | ASC, DESC | Dirección de ordenamiento |
| `level` | string | BEGINNER, INTERMEDIATE, ADVANCED | Filtra por nivel |
| `category` | string | STRENGTH, CARDIO, FLEXIBILITY, HIIT | Filtra por categoría |
| `equipment` | string | BODYWEIGHT, BARBELL, DUMBBELLS, CABLE, MACHINE, KETTLEBELL | Filtra por equipamiento |
| `muscle` | string | cualquier string | Búsqueda parcial en músculos (ej: "pecho") |

#### Response

**GET /exercises**
```json
{
  "content": [
    {
      "id": 42,
      "name": "Push-up de pecho",
      "description": "Ejercicio de calistenia básico...",
      "level": "BEGINNER",
      "category": "STRENGTH",
      "equipment": "BODYWEIGHT",
      "muscleGroups": ["pecho", "tríceps", "hombro"],
      "imageUrl": "/exercise-images/strength/pushup.jpg"
    },
    ...
  ],
  "totalElements": 245,
  "totalPages": 13,
  "currentPage": 0,
  "pageSize": 20
}
```

**GET /exercises/{id}**
```json
{
  "id": 42,
  "name": "Push-up de pecho",
  "description": "Ejercicio de calistenia básico para desarrollo de fuerza...",
  "instructions": "1. Acuéstate boca abajo...\n2. Coloca manos...\n3. Empuja...",
  "level": "BEGINNER",
  "category": "STRENGTH",
  "equipment": "BODYWEIGHT",
  "muscleGroups": ["pecho", "tríceps", "hombro anterior"],
  "imageUrl": "/exercise-images/strength/pushup.jpg"
}
```

#### Imágenes de Ejercicios

Las imágenes se sirven a través del gateway (Ronnie las proporciona):

```
GET /ifit/api/v1/exercise-images/{category}/{filename}.jpg

Ejemplos:
/exercise-images/strength/barbell-bench-press.jpg
/exercise-images/cardio/burpee.jpg
/exercise-images/flexibility/hamstring-stretch.jpg
```

**Nota:** Las imágenes se cachean en el cliente; cambios requieren invalidación de caché.

---

### Notification — Notificaciones por Email

El módulo de notificación gestiona el envío de emails transaccionales. El componente principal es `AppEmailService`, que usa **Spring Mail** con SMTP de Gmail.

#### Tipos de Email

**1. Verificación de Cuenta (Principal)**
- Se genera un código numérico de 6 dígitos
- Se almacena en BD con expiración de 15 minutos
- Se envía mediante plantilla Thymeleaf: `verificationmail.html`
- Se invoca automáticamente:
  - En `POST /auth/register` (nuevo usuario)
  - En `POST /auth/login` sin verificar (reenvío automático)
  - En `POST /auth/resend-verification` (solicitud del usuario)

**2. Ticket de Soporte**
- Formulario de soporte directo al cliente
- Plantilla: `supportticket.html`
- Endpoint público: `POST /appemail/support-ticket`

**3. Reset de Contraseña (Plantilla Preparada)**
- Plantilla: `resetpassword.html`
- Actualmente no se usa en endpoints (reservado para futura expansión)

#### Endpoints de AppEmailController

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| POST | `/appemail/support-ticket` | público | Envía ticket de soporte con nombre, email, asunto, mensaje |

**Request:** `SupportTicketRequestDto { name, email, subject, message }`  
**Response:** `EmailResponseDto { success, message }`

#### Métodos de AppEmailService

```java
// Envía código de verificación por email
sendVerificationEmail(AppUser user, String verificationCode)

// Envía respuesta a ticket de soporte
sendSupportTicketEmail(SupportTicketRequestDto ticket)

// Método genérico para otros emails
sendEmail(String to, String subject, String htmlContent)
```

#### Configuración SMTP

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME:adminifit96@gmail.com}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Credenciales:** Se usan variables de entorno. La contraseña es **contraseña de aplicación de Gmail**, no la contraseña de cuenta.

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

Se usa el parámetro `@AuthenticationPrincipal Jwt jwt` en controllers para acceder al token JWT:

```java
@GetMapping("/my-profile")
public UserProfileDto getMyProfile(@AuthenticationPrincipal Jwt jwt) {
    String keycloakUserId = jwt.getClaimAsString("preferred_username");
    // keycloakUserId = "juan@example.com"
    
    String userEmail = jwt.getClaimAsString("email");
    // userEmail = "juan@example.com"
}
```

El `preferred_username` (o `sub` en algunos casos) es **crítico** porque vincula:
- Usuario en Keycloak (identidad)
- Usuario en BD local (perfil, coach, experiencia)
- Rutinas en BD (entrenamientos)
- Sesión en Ronnie (historial de conversación con LLM)

**Nota:** No existe clase `JwtUtils` central; cada controlador accede directamente a los claims del JWT.

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
