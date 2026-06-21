# Ronnie — Microservicio de Generación de Rutinas IA

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-green.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/)
[![LangChain4j](https://img.shields.io/badge/LangChain4j-latest-orange.svg)](https://github.com/langchain4j/langchain4j)
[![Groq](https://img.shields.io/badge/LLM-Groq%20(2%20modelos)-purple.svg)](https://groq.com/)
[![OAuth2](https://img.shields.io/badge/OAuth2-Resource%20Server%20(Keycloak)-blue.svg)](https://www.keycloak.org)

Motor de IA especializado que genera rutinas de entrenamiento personalizadas y sostiene conversaciones contextuales con cuatro coaches especializados. Actúa como **Resource Server OAuth2** contra Keycloak, con un esquema de **dos modelos LLM** para optimizar costo vs precisión.

> [!NOTE]
> **Arquitectura — Procesamiento NLP + Persistencia MySQL**  
> Este documento cubre: integración LangChain4j, generación JSON estructurada, autenticación OAuth2/JWT, memoria conversacional persistente y configuración dual de modelos. Ver [README general](../../README.md) para la orquestación completa del sistema.

---

## Tabla de Contenidos

1. [Visión General](#visión-general)
2. [Seguridad — OAuth2 Resource Server](#seguridad--oauth2-resource-server)
3. [Arquitectura de Modelos LLM](#arquitectura-de-modelos-llm)
4. [Estructura de Respuesta de Rutina](#estructura-de-respuesta-de-rutina)
5. [Memoria Conversacional](#memoria-conversacional)
6. [Stack Tecnológico](#stack-tecnológico)
7. [Estructura del Proyecto](#estructura-del-proyecto)
8. [Los Cuatro Coaches Especializados](#los-cuatro-coaches-especializados)
9. [Configuración](#configuración)
10. [Endpoints](#endpoints)

---

## Visión General

Ronnie es el motor de IA de iFit. Su rol: recibir un prompt con datos del cuestionario de evaluación del usuario y devolver una **rutina estructurada en JSON** + endpoints de chat libre con coaches especializados.

**Flujo principal:**
```
IFit (8081) 
  → POST /ronnie/generate-routine {memoryId, questionnaireData, userId}
    → Ronnie (8082) autentica JWT contra Keycloak
      → Selecciona modelo LLM según tarea (chat vs rutina)
      → Consulta historial de MySQL (memoria persistente)
      → Genera JSON o texto
      → Persiste en base de datos
  → Devuelve RoutineResponseDto {message, description, days[]}
```

**Diferenciación por tarea:**
- **Generación de rutinas**: modelo `openai/gpt-oss-120b` (especializado, preciso, JSON forzado)
- **Chat libre**: modelo `llama-3.3-70b-versatile` (ligero, conversacional, con RAG de personalidad)

---

## Seguridad — OAuth2 Resource Server

Ronnie **no gestiona autenticación**, la delega a Keycloak a través del API Gateway.

**Flujo:**
1. Cliente autentica en Keycloak, obtiene JWT token
2. API Gateway valida y firma el token con Keycloak
3. API Gateway incluye `Authorization: Bearer <JWT>` (TokenRelay) antes de reenviar a Ronnie
4. Ronnie valida el JWT localmente contra `/realms/ifit-realm/protocol/openid-connect/certs` (Keycloak)
5. Si válido, extrae `sub` (UUID de usuario) del JWT y lo almacena en `ChatContext`

**Configuración (SecurityConfig.java):**
- CSRF deshabilitado (stateless)
- Todos los endpoints requieren JWT válido (excepto `/swagger-ui/**`, `/v3/api-docs/**`)
- Session policy: STATELESS (sin cookies, sin estado en servidor)
- `@Bean SecurityFilterChain` con `oauth2ResourceServer().jwt()`

**Endpoints públicos (sin auth):**
- `GET /swagger-ui.html`, `/v3/api-docs/**`
- `GET /actuator/health`

---

## Arquitectura de Modelos LLM

La clave de eficiencia: **dos modelos especializados en Groq**, uno por tarea.

### Dual Model Strategy

| Modelo | Tarea | Temperatura | Formato | Timeout | Ventaja |
|---|---|---|---|---|---|
| `llama-3.3-70b-versatile` | Chat conversacional (RAG) | 0.5 | Texto plano | 120s | Ligero, respuestas variadas y naturales |
| `openai/gpt-oss-120b` | Generación de rutinas JSON | 0.3 | `json_object` (forzado) | 1200s | Razonador largo-contexto, fidelidad 100% al catálogo |

**Por qué dos modelos:**
- **Chat**: Llama 70B es rápido (frases motivacionales cortas). Temperatura 0.5 = variedad en respuestas.
- **Rutinas**: GPT-OSS 120B es más lento pero garantiza JSON válido y copia exacta de nombres de ejercicio (sin alucinaciones). Temperatura 0.3 = determinístico.

**Configuración:**
```properties
# application.properties
groq.model-name=llama-3.3-70b-versatile              # Chat
groq.routine-model-name=openai/gpt-oss-120b          # Rutinas (fallback al model-name si no definido)
groq.timeout=1200                                     # En segundos (rutinas tardan más)
```

**En código:**
- `GroqModelConfiguration.java` → dos beans: `@Bean("groqLlama70b")` y `@Bean("groqGptOssJson")`
- Cada `@AiService` referencia su bean por nombre: `chatModel = "groqLlama70b"` o `"groqGptOssJson"`

---

## Estructura de Respuesta de Rutina

`RoutineResponseDto` es el contrato de salida de todos los endpoints `**/generate-routine`:

```java
record RoutineResponseDto(
    String message,                    // 3-5 frases de motivación/resumen del coach
    String description,                // Descripción general de la rutina (3-4 frases)
    Integer trainingDays,              // Número de días de entrenamiento
    List<RoutineDayDto> days           // Array de días con ejercicios
) {}
```

**Cada `RoutineDayDto`:**
```java
record RoutineDayDto(
    Integer dayNumber,                 // 1, 2, 3...
    String dayName,                    // "Día 1 — Push", "Lunes — Pecho"
    String description,                // Enfoque muscular + calentamiento + estiramientos
    List<RoutineExerciseDto> exercises // Array de ejercicios del día
) {}
```

**Cada `RoutineExerciseDto`:**
```java
record RoutineExerciseDto(
    String exerciseName,               // EXACTO del catálogo (sin variaciones)
    Integer sets,                      // 3, 4, etc.
    String reps,                       // "8-12", "10-15"
    Integer restSeconds,               // 90, 180, etc.
    String notes,                      // Consejo técnico breve
    Integer orderIndex                 // Orden dentro del día
) {}
```

**Garantía de fidelidad:** El `@SystemMessage` de cada coach ordena copiar `exerciseName` **carácter por carácter** del catálogo. El modelo `groqGptOssJson` con temperatura 0.3 y formato JSON forzado casi elimina alucinaciones.

---

## Memoria Conversacional

Ronnie persiste historial en MySQL bajo dos capas:

1. **Persistencia física**: tabla `message` en MySQL (`ronnie.db`)
   - Campos: `id`, `memoryId`, `userId`, `coachName`, `message`, `messageType`, `createdAt`
   - `PersistentChatMemoryStore` mapea esta tabla a la interfaz `ChatMemoryStore` de LangChain4j

2. **Ventana deslizante en RAM**: `MessageWindowChatMemory(maxMessages=10)`
   - Carga los últimos 10 mensajes de MySQL por `memoryId`
   - Los usa como contexto del LLM
   - Persiste solo el nuevo mensaje al final

**Ciclo de vida:**
```
Request HTTP (JWT válido)
  ↓ ChatContext.set(userId, coachName)     // ThreadLocal con identidad
  ↓ LangChain4j carga últimos 10 msgs de MySQL
  ↓ LLM procesa: SystemMessage + UserMessage + historial
  ↓ LLM responde
  ↓ PersistentChatMemoryStore.updateMessages() → INSERT en MySQL
  ↓ ChatContext.clear() en finally        // Previene fugas de ThreadLocal
  ↓ Response JSON
```

**Gestión de `memoryId`:** Cada conversación necesita un ID único. IFit lo obtiene con `GET /messages/max-memory-id` → devuelve máximo actual + 1.

---

## Stack Tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.5.7 + Spring Security OAuth2 |
| IA | LangChain4j (spring-boot-starter, easy-rag, openai-client) |
| LLM | Groq (dos modelos: Llama 70B + GPT-OSS 120B) |
| Persistencia | Spring Data JPA / Hibernate |
| Base de datos | MySQL 8.0+ (`ronnie` schema) |
| Autenticación | Keycloak (validación de JWT local) |
| Service Discovery | Spring Cloud Eureka (client) |
| Documentación | SpringDoc OpenAPI 3 (Swagger)

---

## Estructura del Proyecto

```
ronnie/
├── src/main/java/com/ifit/ronnie/
│   ├── RonnieApplication.java
│   ├── configuration/
│   │   ├── GroqModelConfiguration.java              # Beans LLM (groqLlama70b, groqGptOssJson)
│   │   ├── SecurityConfig.java                      # OAuth2 Resource Server (Keycloak)
│   │   ├── PersistentChatMemoryStore.java           # MySQL ↔ LangChain4j ChatMemoryStore
│   │   ├── EmbeddingStoreContentConfiguration.java  # RAG setup + coaches embeddings
│   │   ├── ChatContext.java                         # InheritableThreadLocal {userId, coachName}
│   │   ├── JwtUtils.java                            # Extrae userId del JWT
│   │   └── OpenApiConfig.java                       # Swagger/OpenAPI
│   ├── modules/coach/
│   │   ├── dto/                                     # RoutineResponseDto, RoutineDayDto, RoutineExerciseDto
│   │   ├── ronnie/
│   │   │   ├── RonnieService.java                   # @AiService chat (groqLlama70b + RAG)
│   │   │   ├── RonnieRoutineService.java            # @AiService routine (groqGptOssJson)
│   │   │   └── RonnieController.java                # POST /{ronnie}/chat, /{ronnie}/generate-routine
│   │   ├── serena/                                  # Idem estructura
│   │   ├── kael/                                    # Idem estructura
│   │   └── eliud/                                   # Idem estructura
│   └── modules/message/
│       ├── controller/MessageController.java        # GET /messages/max-memory-id, /user/{userId}/coach/{coachName}
│       ├── model/Message.java                       # Entity JPA: memoryId, userId, coachName, message, type
│       ├── repository/MessageRepository.java        # JPA queries on Message table
│       └── service/MessageService.java              # Business logic
├── src/main/resources/
│   ├── application.properties                       # Groq, MySQL, Keycloak, Eureka
│   ├── data.sql                                     # INSERT message_type (dev)
│   └── langchain4j/assistants-personality/
│       ├── exercises.txt                            # Catálogo JSON (warmup/beginner/intermediate/advanced/stretching)
│       ├── ronnie.txt                               # Prompt context + RAG (chat only)
│       ├── serena.txt
│       ├── kael.txt
│       └── eliud.txt
└── pom.xml
```

**Componentes clave:**
- **GroqModelConfiguration**: dos beans, uno por tarea. Referenciados en `@AiService(chatModel = "...")`
- **PersistentChatMemoryStore**: puente MySQL ↔ LangChain4j. Carga/guarda mensajes por `memoryId`
- **ChatContext**: ThreadLocal que propagate userId y coachName sin parámetros; usado por PersistentChatMemoryStore
- **Message entity**: tabla MySQL, única fuente de verdad del historial (10 mensajes en ventana deslizante)

---

## @AiService — Patrones LangChain4j

Ronnie usa `@AiService` de LangChain4j para implementar dos patrones por coach:

```java
// Patrón 1: Chat libre (RAG + conversación)
@AiService(chatModel = "groqLlama70b", 
           chatMemoryProvider = "messageWindowChatMemory",
           contentRetriever = "ronnieEmbeddingStoreContentRetriever")
interface RonnieService {
    String chat(@MemoryId int memoryId, @UserMessage String msg);
}

// Patrón 2: Generación de rutinas (JSON puro, sin RAG)
@AiService(chatModel = "groqGptOssJson",
           chatMemoryProvider = "messageWindowChatMemory")
interface RonnieRoutineService {
    @SystemMessage("... reglas de generación ...")
    RoutineResponseDto generateRoutine(
        @MemoryId int memoryId,
        @V("questionnaireData") String data,
        @V("exerciseCatalog") String catalog
    );
}
```

**Cómo funciona:**
1. `@MemoryId` → LangChain4j carga últimos 10 mensajes de MySQL por ese ID
2. `@SystemMessage` + `@UserMessage` → se construyen y envían al LLM
3. Respuesta → deserializada al tipo de retorno (String o RoutineResponseDto)
4. `PersistentChatMemoryStore.updateMessages()` → inserta el nuevo mensaje en MySQL
5. RAG (si existe `contentRetriever`) → inyecta fragmentos de RAG automáticamente

---

## Los Cuatro Coaches Especializados

Cada coach tiene dos interfaces `@AiService`: una para chat (con RAG), otra para generar rutinas (sin RAG).

| Coach | Especialidad | Tono | Cuestionario único |
|---|---|---|---|
| **Ronnie** | Hipertrofia, fuerza muscular | Directo, entusiasta | Preferencia de equipamiento |
| **Serena** | Fitness femenino, tonificación | Empático, sin presión | Nivel estrés + motivación |
| **Kael** | Calistenia, street workout | Técnico, funcional | Dominadas/fondos que hace |
| **Eliud** | Running, cardio, resistencia | Sereno, progresivo | Objetivo carrera (5K/10K/maratón) |

**Catálogo de ejercicios:**
- `exercises.txt` → JSON con 5 niveles: `warmup`, `beginner`, `intermediate`, `advanced`, `stretching`
- Cada coach prioriza ciertos ejercicios según su especialidad en el `@SystemMessage`
- **Regla de oro**: copiar `exerciseName` carácter por carácter del catálogo (forzado por temperatura 0.3 + JSON)

**RAG (chat only):**
- `ronnie.txt`, `serena.txt`, `kael.txt`, `eliud.txt` → beans como `{coach}EmbeddingStoreContentRetriever`
- Inyectados automáticamente en chat; NO en rutinas (rutinas usan solo `@SystemMessage` codificado)

---

## Endpoints

**Base URL**: `http://localhost:8082` | Vía Gateway: `http://localhost:8080/ifit/api/v1/ronnie/**`

Todos los endpoints requieren JWT válido en header `Authorization: Bearer <token>` (excepto Swagger/health).

### Generación de Rutinas

Request body (todos):
```json
{
  "memoryId": 42,
  "message": "ROL DEL ENTRENADOR\n...",
  "userId": "keycloak-uuid-del-usuario"
}
```

Response: `RoutineResponseDto` (ver sección "Estructura de Respuesta de Rutina")

| Método | Ruta | Coach | Especialidad |
|---|---|---|---|
| POST | `/ronnie/generate-routine` | Ronnie | Hipertrofia (grupos musculares) |
| POST | `/serena/generate-routine` | Serena | Tonificación (30-45 min, accesible) |
| POST | `/kael/generate-routine` | Kael | Calistenia (peso corporal) |
| POST | `/eliud/generate-routine` | Eliud | Running + cardio + resistencia |

### Chat Libre

Request: `{ "memoryId": 42, "message": "¿Cómo mejoro mi press?", "userId": "..." }`  
Response: `{ "message": "<respuesta del coach>" }`

| Método | Ruta | Coach |
|---|---|---|
| POST | `/ronnie/chat` | Conversación sobre musculación |
| POST | `/serena/chat` | Conversación sobre bienestar |
| POST | `/kael/chat` | Conversación sobre calistenia |
| POST | `/eliud/chat` | Conversación sobre running |

### Historial

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/messages/max-memory-id` | Devuelve máximo memoryId actual + 1 (para nueva sesión) |
| GET | `/messages/user/{userId}/coach/{coachName}` | Historial completo de usuario + coach (404 si vacío) |

### Públicos (sin auth)

| Método | Ruta |
|---|---|
| GET | `/swagger-ui.html`, `/v3/api-docs/**` |
| GET | `/actuator/health` |

---

## Configuración

**Archivo**: `src/main/resources/application.properties`

```properties
# ── Aplicación ──────────────────────────────────────────
spring.application.name=ronnie
server.port=8082

# ── MySQL ───────────────────────────────────────────────
spring.datasource.url=jdbc:mysql://localhost:3306/ronnie
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:root}
spring.jpa.hibernate.ddl-auto=create-drop
spring.sql.init.mode=always

# ── Keycloak · OAuth2 Resource Server ──────────────────
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9090/realms/ifit-realm
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:9090/realms/ifit-realm/protocol/openid-connect/certs

# ── Groq · LLM ──────────────────────────────────────────
groq.base-url=https://api.groq.com/openai/v1
groq.api-key=${GROQ_API_KEY}                    # Variable de entorno (NO hardcoded)
groq.model-name=llama-3.3-70b-versatile         # Chat
groq.routine-model-name=openai/gpt-oss-120b     # Rutinas (fallback: groq.model-name)
groq.timeout=1200                               # Segundos

# ── Memoria conversacional ───────────────────────────────
chat.memory.max-messages=10
chat.memory.max-tokens=500

# ── Eureka (Service Discovery) ──────────────────────────
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.prefer-ip-address=true
eureka.instance.ip-address=127.0.0.1
eureka.instance.instance-id=${spring.application.name}:${server.port}
```

**Notas:**
- `GROQ_API_KEY`: obtén en [console.groq.com](https://console.groq.com), exporta como variable de entorno
- `groq.timeout=1200`: generación de rutinas puede tardar 3-4 minutos con modelos grandes
- `create-drop`: resetea MySQL en cada arranque (dev only); cambiar a `update` en producción

---

## Inicio Rápido

### Requisitos

- Java 21+, Maven 3.9+
- MySQL 8.0+
- Variable de entorno `GROQ_API_KEY=sk-...`
- Keycloak corriendo en `localhost:9090`

### Setup

```bash
# 1. Crear base de datos
mysql -u root -p -e "CREATE DATABASE ronnie CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. Variables de entorno
export GROQ_API_KEY=sk-...  # Obtener de https://console.groq.com

# 3. Compilar y arrancar
cd ronnie
mvn clean package -DskipTests
mvn spring-boot:run
```

### Verificación

```bash
# Swagger UI
curl http://localhost:8082/swagger-ui.html

# Health check
curl http://localhost:8082/actuator/health

# Max memory ID (sin auth, solo demo)
curl -H "Authorization: Bearer <JWT>" http://localhost:8082/messages/max-memory-id
```

---

## Resumen de Decisiones de Diseño

| Decisión | Razón | Impacto |
|---|---|---|
| **Dos modelos LLM** | Optimizar costo vs precisión | Chat ligero (Llama 70B), rutinas precisas (GPT-OSS 120B) |
| **MySQL persistente** | No perder historial entre requests | Ventana deslizante de 10 msgs en RAM sobre DB duradora |
| **ChatContext (ThreadLocal)** | Propagar userId sin contaminar parámetros | Código limpio, mejor encapsulación en PersistentChatMemoryStore |
| **RAG solo en chat** | Evitar inflación de prompts en rutinas | Prompts de rutinas codificados directamente, JSON determinístico |
| **Temperatura 0.3 rutinas** | Reducir alucinaciones de nombres | Fidelidad 100% al catálogo |
| **OAuth2 Resource Server** | Delegar auth a Keycloak | Ronnie stateless, compatible con API Gateway |

---

## Troubleshooting

**JWT inválido / 401 Unauthorized:**
- Verificar que el token sea válido: `jwt.io`
- Verificar que Keycloak esté corriendo en `http://localhost:9090`
- Verificar que el realm sea `ifit-realm`

**Rutinas generadas con ejercicios inventados:**
- Comprobar que `groq.routine-model-name=openai/gpt-oss-120b` está en `application.properties`
- Comprobar que `groq.timeout=1200` (no muy corto)
- Verificar que `exerciseCatalog` se pasa en la request (inyectado por Controller)

**Chat devuelve respuestas fuera de tema:**
- Verificar que `contentRetriever = "{coach}EmbeddingStoreContentRetriever"` está en la interfaz
- Verificar que el archivo `{coach}.txt` existe en `langchain4j/assistants-personality/`
- Revisar el `@SystemMessage` de la interfaz de chat

**Base de datos 404:**
- Ejecutar `mysql -u root -p -e "CREATE DATABASE ronnie CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"`
- Verificar `spring.datasource.url` en properties

---

## Referencias

- [LangChain4j Docs](https://github.com/langchain4j/langchain4j)
- [Groq API](https://groq.com/)
- [Spring Security OAuth2](https://spring.io/projects/spring-security)
- [Keycloak](https://www.keycloak.org/)

---

**Juan García Candón** | TFG 2024–2025 | Universidad de Cádiz
