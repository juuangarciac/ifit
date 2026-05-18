# Ronnie — Microservicio de IA

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/)
[![LangChain4j](https://img.shields.io/badge/LangChain4j-latest-orange.svg)](https://github.com/langchain4j/langchain4j)
[![Groq](https://img.shields.io/badge/LLM-Groq%20(llama--3.3--70b)-purple.svg)](https://groq.com/)

> Motor de inteligencia artificial del sistema iFit. Gestiona cinco coaches especializados que generan rutinas de entrenamiento personalizadas y mantienen conversaciones contextuales con el usuario.

---

## Tabla de Contenidos

- [Descripción General](#descripción-general)
- [Arquitectura del Microservicio](#arquitectura-del-microservicio)
- [Stack Tecnológico](#stack-tecnológico)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [LangChain4j — Arquitectura de IA](#langchain4j--arquitectura-de-ia)
  - [¿Qué es @AiService?](#qué-es-aiservice)
  - [Modelos LLM configurados](#modelos-llm-configurados)
  - [Memoria conversacional persistente](#memoria-conversacional-persistente)
  - [ChatContext — propagación de identidad por hilo](#chatcontext--propagación-de-identidad-por-hilo)
  - [RAG — Personalidad de los coaches de chat](#rag--personalidad-de-los-coaches-de-chat)
  - [Catálogo de ejercicios](#catálogo-de-ejercicios)
- [Los Cinco Coaches](#los-cinco-coaches)
  - [Master](#master)
  - [Ronnie](#ronnie)
  - [Serena](#serena)
  - [Kael](#kael)
  - [Eliud](#eliud)
- [Módulo Message — Historial y memoria](#módulo-message--historial-y-memoria)
- [Base de Datos](#base-de-datos)
- [Configuración](#configuración)
- [Puesta en Marcha](#puesta-en-marcha)
- [Referencia de Endpoints](#referencia-de-endpoints)

---

## Descripción General

Ronnie es el microservicio de IA de la plataforma iFit. Su función principal es recibir el prompt construido por IFit a partir de las respuestas del cuestionario de evaluación y devolver una **rutina de entrenamiento estructurada en JSON** generada por un modelo de lenguaje grande (LLM).

Además, expone endpoints de **chat libre** con cada coach, lo que permite al usuario mantener conversaciones contextuales sobre fitness, técnica y nutrición.

Internamente combina tres tecnologías de IA:

| Tecnología | Uso |
|---|---|
| **LangChain4j** | Framework de abstracción sobre LLMs. Gestiona @AiService, memoria, RAG y prompts. |
| **Groq** | Proveedor LLM en la nube. API compatible con OpenAI. Modelo: `llama-3.3-70b-versatile`. |
| **Ollama** | Bean alternativo configurado pero no activo en producción (modelo `qwen2.5:14b` local). |

---

## Arquitectura del Microservicio

```
IFit (localhost:8081)
       │
       │ POST /{coach}/generate-routine
       │ Body: { memoryId, message (prompt), userId }
       ▼
┌─────────────────────────────────────────────────────────────┐
│                         RONNIE :8082                         │
│                                                             │
│  Controller                                                 │
│  /{coach}/chat               /{coach}/generate-routine      │
│       │                               │                     │
│  @AiService (chat)           @AiService (routine)           │
│  ┌────────────────┐          ┌────────────────────────────┐ │
│  │ groqChatModel  │          │  groqJsonChatModel         │ │
│  │ + RAG          │          │  + @SystemMessage (reglas) │ │
│  │ (personality)  │          │  + catalog @V              │ │
│  └────────────────┘          └────────────────────────────┘ │
│       │                               │                     │
│  ChatMemoryProvider (MySQL) ──────────┘                     │
│  PersistentChatMemoryStore                                  │
└─────────────────────────────────────────────────────────────┘
       │
       ▼
Groq API (https://api.groq.com/openai/v1)
llama-3.3-70b-versatile
```

---

## Stack Tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.x |
| IA / LLM | LangChain4j (langchain4j-spring-boot-starter, langchain4j-easy-rag, langchain4j-open-ai, langchain4j-ollama) |
| Proveedor LLM activo | Groq (API OpenAI-compatible) — modelo `llama-3.3-70b-versatile` |
| Proveedor LLM alternativo | Ollama (local, bean configurado pero no wired) |
| Persistencia | Spring Data JPA / Hibernate |
| Base de datos | MySQL 8.0+ (base de datos `ronnie`) |
| Documentación | SpringDoc OpenAPI 3 (Swagger UI) |
| Build | Maven |
| Service Discovery | Spring Cloud (Eureka client) |

---

## Estructura del Proyecto

```
ronnie/
├── src/main/java/com/ifit/ronnie/
│   ├── RonnieApplication.java
│   ├── configuration/
│   │   ├── GroqModelConfiguration.java        # Beans LLM (groqChatLanguageModel, groqJsonChatLanguageModel)
│   │   ├── OllamaModelConfiguration.java      # Bean LLM alternativo (ollamaModel)
│   │   ├── EmbeddingStoreContentConfiguration.java  # RAG + ChatMemoryProvider
│   │   ├── PersistentChatMemoryStore.java     # Implementación de ChatMemoryStore sobre MySQL
│   │   ├── ChatContext.java                   # InheritableThreadLocal con userId + coachName
│   │   ├── JwtUtils.java                      # Extrae userId del JWT de la petición HTTP
│   │   └── OpenApiConfig.java                 # Configuración Swagger
│   └── modules/
│       ├── coach/
│       │   ├── dto/                           # RoutineResponseDto, RoutineDayDto, RoutineExerciseDto
│       │   ├── master/
│       │   │   ├── Master.java                # @AiService — genera rutinas (solo JSON, sin chat)
│       │   │   └── MasterController.java      # POST /master/generate-routine
│       │   ├── ronnie/
│       │   │   ├── RonnieService.java         # @AiService — chat libre con RAG
│       │   │   ├── RonnieRoutineService.java  # @AiService — genera rutinas JSON
│       │   │   └── RonnieController.java      # POST /ronnie/chat, /ronnie/generate-routine
│       │   ├── serena/
│       │   │   ├── SerenaService.java
│       │   │   ├── SerenaRoutineService.java
│       │   │   └── SerenaController.java
│       │   ├── kael/
│       │   │   ├── KaelService.java
│       │   │   ├── KaelRoutineService.java
│       │   │   └── KaelController.java
│       │   └── eliud/
│       │       ├── EliudService.java
│       │       ├── EliudRoutineService.java
│       │       └── EliudController.java
│       └── message/
│           ├── controller/
│           │   ├── MessageController.java     # GET /messages/**
│           │   └── dto/                       # MessageDto, MessageResponseDto, MaxMemoryIdDto
│           ├── mapper/ChatMessageMapper.java
│           ├── model/
│           │   ├── Message.java               # Entidad JPA (memoryId, userId, coachName, message, type)
│           │   └── MessageType.java           # Entidad: user / ai / system
│           ├── repository/
│           │   ├── MessageRepository.java
│           │   └── MessageTypeRepository.java
│           └── service/MessageService.java
├── src/main/resources/
│   ├── application.properties
│   ├── data.sql                                # Inicialización de message_type (dev)
│   ├── excercises.csv                          # CSV original del catálogo (fuente)
│   └── langchain4j/assistants-personality/
│       ├── exercises.txt                       # Catálogo JSON de ejercicios (warmup/beginner/…)
│       ├── ronnie.txt                          # Personalidad del coach Ronnie (RAG)
│       ├── serena.txt                          # Personalidad de Serena (RAG)
│       ├── kael.txt                            # Personalidad de Kael (RAG)
│       └── eliud.txt                           # Personalidad de Eliud (RAG)
└── pom.xml
```

---

## LangChain4j — Arquitectura de IA

### ¿Qué es @AiService?

`@AiService` es la abstracción central de LangChain4j. Permite declarar una **interfaz Java** y que LangChain4j la implemente automáticamente en tiempo de arranque, conectándola a los beans LLM, memoria y RAG configurados.

```java
@AiService(
    wiringMode = AiServiceWiringMode.EXPLICIT,   // Referencia beans por nombre
    chatModel = "groqJsonChatLanguageModel",      // Bean del LLM a usar
    chatMemoryProvider = "messageWindowChatMemory" // Bean de memoria conversacional
)
public interface RonnieRoutineService {

    @SystemMessage("... instrucciones estáticas del coach ...")
    @UserMessage("Genera la rutina basándote en: {questionnaireData}")
    RoutineResponseDto generateRoutine(
        @MemoryId int memoryId,
        @V("questionnaireData") String questionnaireData,
        @V("exerciseCatalog") String exerciseCatalog
    );
}
```

Cuando IFit llama a `ronnieRoutineService.generateRoutine(...)`, LangChain4j:
1. Recupera el historial de la memoria identificada por `memoryId`.
2. Construye el mensaje de sistema con `@SystemMessage` (más las variables `@V`).
3. Construye el mensaje de usuario con `@UserMessage` (sustituye `{questionnaireData}`).
4. Envía la secuencia completa al LLM.
5. Deserializa la respuesta JSON al tipo de retorno `RoutineResponseDto`.
6. Persiste los mensajes nuevos en la memoria.

`wiringMode = EXPLICIT` es el modo requerido en Spring Boot para referenciar beans por nombre de string en lugar de dejar que LangChain4j los detecte automáticamente.

---

### Modelos LLM configurados

`GroqModelConfiguration` define dos beans que usan la misma API de Groq pero con parámetros distintos:

| Bean | Temperatura | Formato | Uso |
|---|---|---|---|
| `groqChatLanguageModel` | 0.5 | Texto libre | Chat conversacional con el usuario. |
| `groqJsonChatLanguageModel` | 0.3 | `json_object` | Generación de rutinas estructuradas. |

La temperatura más baja en `groqJsonChatLanguageModel` reduce la variabilidad del modelo y mejora la fiabilidad del JSON generado. `responseFormat("json_object")` obliga al modelo a devolver siempre JSON válido.

Ambos usan el proveedor **Groq** a través de `OpenAiChatModel.builder()`, ya que Groq implementa la misma API REST que OpenAI. El modelo activo es `llama-3.3-70b-versatile` (Meta LLaMA 3.3 de 70B parámetros), que en Groq se ejecuta sobre hardware especializado (LPUs) con latencia muy baja.

`OllamaModelConfiguration` define el bean `ollamaModel` para un modelo local Ollama (`qwen2.5:14b`). Está configurado pero actualmente **no está wired** a ningún `@AiService`; sirve como alternativa offline.

---

### Memoria conversacional persistente

Ronnie implementa memoria conversacional completa sobre MySQL mediante dos capas:

**1. `PersistentChatMemoryStore`** (`@Component`) — implementa la interfaz `ChatMemoryStore` de LangChain4j:
- `getMessages(memoryId)` → lee los mensajes de la tabla `message` ordenados cronológicamente y los convierte a objetos `ChatMessage` de LangChain4j (`UserMessage`, `AiMessage`, `SystemMessage`).
- `updateMessages(memoryId, messages)` → persiste el **último mensaje** de la lista (solo el nuevo, no toda la historia) en la tabla `message`, incluyendo `userId` y `coachName` del `ChatContext`.
- `deleteMessages(memoryId)` → elimina todos los mensajes de un `memoryId`.

**2. `MessageWindowChatMemory`** (`@Bean("messageWindowChatMemory")`) — ventana deslizante sobre el store:
- Limita el historial a los **últimos 10 mensajes** (`chat.memory.max-messages`).
- Usa `PersistentChatMemoryStore` como backend; si el historial tiene más de 10 mensajes, los más antiguos se descartan de la ventana (pero siguen en MySQL).
- Se crea una instancia distinta por `memoryId`, lo que aísla las conversaciones.

El `memoryId` es un entero que IFit gestiona mediante el endpoint `GET /messages/max-memory-id`. Cada vez que se genera una nueva rutina, IFit solicita el máximo `memoryId` actual y asigna `maxId + 1` a la nueva sesión, garantizando unicidad.

---

### ChatContext — propagación de identidad por hilo

`ChatContext` usa `InheritableThreadLocal` para propagar el `userId` (Keycloak UUID del usuario) y el `coachName` a través del hilo de ejecución del request HTTP, sin necesidad de pasarlos como parámetros a cada método interno.

```java
// En el controller — inicio del request
try {
    ChatContext.set(messageDto.userId(), "ronnie");
    // ... llamada al @AiService
} finally {
    ChatContext.clear();  // SIEMPRE en finally para evitar fugas de memoria
}
```

El `PersistentChatMemoryStore` usa `ChatContext.getUserId()` y `ChatContext.getCoachName()` al persistir cada mensaje, asociando así la conversación al usuario y al coach concreto sin contaminar los parámetros del `@AiService`.

El bloque `try/finally` es obligatorio: si `clear()` no se ejecuta, el `ThreadLocal` puede quedar poblado en el hilo del pool de Tomcat y contaminar requests posteriores.

---

### RAG — Personalidad de los coaches de chat

Los endpoints de chat libre (`/ronnie/chat`, `/serena/chat`, etc.) usan **Retrieval-Augmented Generation (RAG)** para dar personalidad al coach. RAG enriquece el prompt con información recuperada de una base de conocimiento antes de enviarlo al LLM.

El flujo en Ronnie es:

```
Arranque de la aplicación:
  1. FileSystemDocumentLoader.loadDocument("ronnie.txt") → Document
  2. InMemoryEmbeddingStore<TextSegment>.ingest(document) → embeddings en RAM
  3. EmbeddingStoreContentRetriever.from(store) → bean "ronnieEmbeddingStoreContentRetriever"

Por cada mensaje de chat:
  4. LangChain4j ejecuta el retriever con el mensaje del usuario como query
  5. Busca el fragmento más relevante del documento de personalidad
  6. Lo inyecta automáticamente como contexto adicional en el prompt al LLM
```

Cada coach tiene su propio archivo de personalidad:

| Archivo | Contenido |
|---|---|
| `ronnie.txt` | Personalidad directa y motivadora, especialidad en hipertrofia, frases características ("¡Vamos, tú puedes!"). |
| `serena.txt` | Tono empático y accesible, especialidad en bienestar y fitness femenino. |
| `kael.txt` | Tono técnico y preciso, especialidad en calistenia y street workout. |
| `eliud.txt` | Tono sereno y constante, especialidad en running y resistencia aeróbica. |

**Nota importante**: los `*RoutineService` (generación de rutinas) **no usan RAG**. La personalidad y las reglas de construcción de la rutina están codificadas directamente en el `@SystemMessage` de cada interfaz, y el catálogo de ejercicios se pasa como parámetro `@V("exerciseCatalog")`.

---

### Catálogo de ejercicios

`exercises.txt` es un fichero JSON con el catálogo completo de ejercicios disponibles, dividido en secciones:

| Sección | Contenido |
|---|---|
| `warmup` | Ejercicios de calentamiento (Marcha en el sitio, Círculos de brazos, etc.). |
| `beginner` | Ejercicios para principiantes con cargas ligeras y máquinas guiadas. |
| `intermediate` | Ejercicios compuestos con barra y mancuernas. |
| `advanced` | Variantes técnicas de alta intensidad (sentadilla con pausa, dominadas con lastre, etc.). |
| `stretching` | Estiramientos post-entreno por grupo muscular. |

Cada ejercicio incluye: `exerciseName`, `sets`, `reps`, `restSeconds`, `notes`, `orderIndex`.

Los `@SystemMessage` de todos los coaches de rutinas incluyen la regla de oro:

> *ÚNICAMENTE puedes usar ejercicios de este catálogo. Copia el campo `exerciseName` CARÁCTER POR CARÁCTER.*

Esto garantiza que los nombres de ejercicios en la rutina generada coincidan exactamente con los del catálogo de IFit, que a su vez los expone al frontend a través de la API de ejercicios.

---

## Los Cinco Coaches

### Master

**Archivo**: [Master.java](src/main/java/com/ifit/ronnie/modules/coach/master/Master.java)  
**Endpoint**: `POST /master/generate-routine`

Master es el **planificador generalista**. A diferencia de los otros coaches, no tiene un endpoint de chat: su única función es generar rutinas estructuradas.

Su `@SystemMessage` es el más completo del sistema. Define:
- El rol de planificador (y cómo adoptar el rol de otro coach si el mensaje incluye un bloque "ROL DEL ENTRENADOR").
- La regla de catálogo (fuente única, copiado carácter a carácter).
- La estructura obligatoria de cada día: calentamiento (2-3 ejercicios warmup) + bloque principal (5-8 ejercicios) + estiramientos (2-3 de stretching).
- La distribución de días según frecuencia: Full Body (1-2 días), Push/Pull/Legs (3 días), Upper/Lower (4-5 días), Muscle Split (6-7 días).
- Las reglas de calidad: sin repetición de ejercicio en el mismo día, máximo 2 días distintos por ejercicio, todo en español.
- La regla de datos omitidos: si aparece `[No respondida]`, usar valor por defecto sin mencionarlo.

Usa `groqJsonChatLanguageModel` (temperatura 0.3, formato JSON).  
**No tiene** `contentRetriever` — sin RAG.

---

### Ronnie

**Coach de**: Hipertrofia y fuerza muscular.  
**Inspiración**: Ronnie Coleman, 8× Mr. Olympia.  
**Tono**: Directo, motivador, entusiasta. Frases: *"¡Vamos, tú puedes!"*, *"Everybody wanna be a bodybuilder!"*

| @AiService | Bean chatModel | contentRetriever |
|---|---|---|
| `RonnieService` (chat) | `groqChatLanguageModel` | `ronnieEmbeddingStoreContentRetriever` |
| `RonnieRoutineService` (rutinas) | `groqJsonChatLanguageModel` | — |

El `@SystemMessage` de `RonnieRoutineService` especifica ejercicios a priorizar por nivel:
- **Beginner**: curl con mancuernas, press de hombros, jalón al pecho, sentadilla con peso corporal.
- **Intermediate**: sentadilla con barra, press de banca, peso muerto, remo con barra.
- **Advanced**: sentadilla con pausa, peso muerto sumo, dominadas con lastre, fondos con lastre.

Distribución por días: grupos musculares (pecho, espalda, piernas, hombros, brazos).

---

### Serena

**Coach de**: Fitness femenino, tonificación, bienestar integral.  
**Tono**: Empático, positivo, sin presión. Sesiones de 30-45 minutos sin equipamiento complejo.  
**Énfasis en**: Glúteos, core, movilidad, full body accesible.

| @AiService | Bean chatModel | contentRetriever |
|---|---|---|
| `SerenaService` (chat) | `groqChatLanguageModel` | `serenaEmbeddingStoreContentRetriever` |
| `SerenaRoutineService` (rutinas) | `groqJsonChatLanguageModel` | — |

El `@SystemMessage` incluye preguntas orientadoras adicionales sobre el nivel de estrés y motivación profunda del usuario (únicas en el cuestionario de Serena).

---

### Kael

**Coach de**: Calistenia, street workout y fuerza funcional sin equipamiento.  
**Tono**: Técnico, claro, directo. Puede entrenar en casa o en un parque.  
**Énfasis en**: Flexiones, fondos, dominadas, plancha, hollow hold, HIIT.

| @AiService | Bean chatModel | contentRetriever |
|---|---|---|
| `KaelService` (chat) | `groqChatLanguageModel` | `kaelEmbeddingStoreContentRetriever` |
| `KaelRoutineService` (rutinas) | `groqJsonChatLanguageModel` | — |

El cuestionario de Kael incluye preguntas específicas de habilidad: cuántas dominadas y fondos puede hacer el usuario, y qué habilidad tiene como objetivo (muscle-up, handstand, etc.).

---

### Eliud

**Coach de**: Running, cardio y rendimiento aeróbico.  
**Inspiración**: Eliud Kipchoge.  
**Tono**: Sereno, constante, con progresión gradual.  
**Énfasis en**: Sentadillas, zancadas, puente de glúteos, core, trabajo de pierna funcional, cardio aeróbico.

| @AiService | Bean chatModel | contentRetriever |
|---|---|---|
| `EliudService` (chat) | `groqChatLanguageModel` | `eliudEmbeddingStoreContentRetriever` |
| `EliudRoutineService` (rutinas) | `groqJsonChatLanguageModel` | — |

El cuestionario de Eliud incluye preguntas sobre objetivo de carrera (5K, 10K, maratón), ritmo actual y experiencia corriendo.

---

## Módulo Message — Historial y memoria

La entidad `Message` es el registro de cada mensaje del historial conversacional.

### Campos de la entidad

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | BIGINT PK | Autoincremental. |
| `memoryId` | VARCHAR | Identificador numérico de la conversación (String en BD). |
| `userId` | VARCHAR | Keycloak UUID del usuario (de `ChatContext`). |
| `coachName` | VARCHAR | Nombre del coach ("ronnie", "serena", etc.) (de `ChatContext`). |
| `message` | TEXT (LOB) | Contenido del mensaje. |
| `messageType` | FK → message_type | Tipo: `user`, `ai` o `system`. |
| `createdAt` | DATETIME | Timestamp de creación. |

### Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/messages/max-memory-id` | Devuelve el siguiente `memoryId` disponible (máximo actual + 1). Usado por IFit antes de cada generación de rutina. |
| GET | `/messages/user/{userId}/coach/{coachName}` | Historial completo de un usuario con un coach concreto, ordenado cronológicamente. Devuelve 404 si no hay mensajes. |

### Datos iniciales

`data.sql` vacía las tablas `message` y `message_type` en cada arranque e inserta los tres tipos de mensaje:

```sql
INSERT INTO message_type (name) VALUES ('user');
INSERT INTO message_type (name) VALUES ('ai');
INSERT INTO message_type (name) VALUES ('system');
```

> **Nota**: este script es intencionalmente destructivo para facilitar el desarrollo. En producción debería eliminarse o condicionarse.

---

## Base de Datos

Ronnie tiene su propia base de datos MySQL (`ronnie`), completamente independiente de la de IFit.

### Tablas

| Tabla | Descripción |
|---|---|
| `message` | Historial conversacional persistente. Cada fila es un turno de la conversación. |
| `message_type` | Catálogo de tipos: `user`, `ai`, `system`. |

### Relación con la memoria de LangChain4j

```
LangChain4j                             MySQL
────────────────────────────────────    ──────────────
ChatMemoryStore.getMessages(id)    →    SELECT * FROM message WHERE memory_id = ?
ChatMemoryStore.updateMessages(…)  →    INSERT INTO message (...)
ChatMemoryStore.deleteMessages(id) →    DELETE FROM message WHERE memory_id = ?
```

`MessageWindowChatMemory(maxMessages=10)` actúa como caché en RAM sobre este store: en cada request carga los últimos 10 mensajes de MySQL, los usa como contexto del LLM y persiste solo el nuevo mensaje al final.

---

## Configuración

Archivo: `src/main/resources/application.properties`

```properties
# Aplicación
spring.application.name=ronnie
server.port=8082

# Base de datos
spring.datasource.url=jdbc:mysql://localhost:3306/ronnie
spring.datasource.username=root
spring.datasource.password=root
spring.sql.init.mode=always
spring.jpa.hibernate.ddl-auto=update

# Memoria conversacional
chat.memory.max-messages=10
chat.memory.max-tokens=500

# Groq (LLM activo)
groq.base-url=https://api.groq.com/openai/v1
groq.api-key=${GROQ_API_KEY}          # Usar variable de entorno; nunca hardcoded
groq.model-name=llama-3.3-70b-versatile
groq.timeout=1200                      # Segundos; generación de rutinas puede tardar

# Ollama (LLM alternativo, sin wiring activo)
ollama.base-url=http://localhost:11434
ollama.model-name=qwen2.5:14b
ollama.timeout=1800

# Eureka
eureka.instance.hostname=ifit-aimodels-service
eureka.instance.prefer-ip-address=true
eureka.instance.instance-id=${spring.application.name}:${server.port}
```

> La API key de Groq debe estar en la variable de entorno `GROQ_API_KEY`, no en el fichero de propiedades, para evitar que se incluya en el control de versiones.

---

## Puesta en Marcha

### Prerrequisitos

- Java 21+
- Maven 3.9+
- MySQL 8.0+ con base de datos `ronnie` creada
- Variable de entorno `GROQ_API_KEY` con una clave válida de [console.groq.com](https://console.groq.com)

### Crear la base de datos

```sql
CREATE DATABASE ronnie CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

`data.sql` crea automáticamente las tablas `message` y `message_type` en el primer arranque.

### Compilar y arrancar

```bash
cd ronnie
mvn clean package -DskipTests
mvn spring-boot:run
```

La API queda disponible en: `http://localhost:8082`

### Documentación interactiva (Swagger UI)

```
http://localhost:8082/swagger-ui.html
```

---

## Referencia de Endpoints

Base URL directa: `http://localhost:8082`  
Base URL vía gateway: `http://localhost:8080/ifit/api/v1/` (rutas de imágenes y catálogo)

### Generación de Rutinas

Todos los endpoints de rutina reciben el mismo `MessageDto`:

```json
{
  "memoryId": 42,
  "message": "ROL DEL ENTRENADOR\n...\n- ¿Objetivo principal?\n  Respuesta: Ganar masa muscular\n...",
  "userId": "keycloak-uuid-del-usuario"
}
```

Y devuelven `RoutineResponseDto`:

```json
{
  "message": "¡Vamos, tú puedes! Este plan de hipertrofia...",
  "description": "Rutina Push/Pull/Legs de 3 días para ganar masa muscular...",
  "trainingDays": 3,
  "days": [
    {
      "dayNumber": 1,
      "dayName": "Día 1 — Push",
      "description": "Trabajamos pecho, hombros y tríceps...",
      "exercises": [
        {
          "exerciseName": "Press de pecho con mancuernas en banco",
          "sets": 3,
          "reps": "10-12",
          "restSeconds": 90,
          "notes": "Baja controlado, 2 segundos de bajada.",
          "orderIndex": 1
        }
      ]
    }
  ]
}
```

| Método | Ruta | Coach | Descripción |
|---|---|---|---|
| POST | `/master/generate-routine` | Master | Planificador generalista. Adopta el rol del coach si el prompt incluye "ROL DEL ENTRENADOR". |
| POST | `/ronnie/generate-routine` | Ronnie | Rutina de hipertrofia y fuerza, distribución por grupos musculares. |
| POST | `/serena/generate-routine` | Serena | Rutina de bienestar y tonificación, sesiones de 30-45 min. |
| POST | `/kael/generate-routine` | Kael | Rutina de calistenia y peso corporal, apta para casa o parque. |
| POST | `/eliud/generate-routine` | Eliud | Rutina de cardio, running y resistencia aeróbica. |

### Chat Libre

Los endpoints de chat reciben y devuelven `MessageDto`:

```json
{ "memoryId": 42, "message": "¿Cómo mejoro mi press de banca?" }
```

| Método | Ruta | Coach | Descripción |
|---|---|---|---|
| POST | `/ronnie/chat` | Ronnie | Conversación sobre musculación, técnica e hipertrofia. |
| POST | `/serena/chat` | Serena | Conversación sobre bienestar, fitness y hábitos saludables. |
| POST | `/kael/chat` | Kael | Conversación sobre calistenia, street workout y fuerza funcional. |
| POST | `/eliud/chat` | Eliud | Conversación sobre running, cardio y rendimiento aeróbico. |

### Historial y Memoria

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/messages/max-memory-id` | Devuelve el siguiente `memoryId` disponible. Llamado por IFit antes de generar cada rutina. |
| GET | `/messages/user/{userId}/coach/{coachName}` | Historial de un usuario con un coach (404 si vacío). |

---

## Autor

**Juan García Candón**  
Universidad de Cádiz — Escuela Superior de Ingeniería  
Trabajo Final de Grado (TFG), 2024–2025
