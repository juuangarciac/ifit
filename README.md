<div align="center">

# iFit — Plataforma de Entrenamiento Personalizado con IA

**Arquitectura de microservicios con cuestionario adaptativo, generación de rutinas por LLM y chat con entrenadores virtuales**

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.4-6DB33F?style=flat-square&logo=springboot)
![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-2025.0.0-6DB33F?style=flat-square&logo=spring)
![Keycloak](https://img.shields.io/badge/Keycloak-23.0-4D4D4D?style=flat-square&logo=keycloak)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)

</div>

---

## Tabla de contenidos

- [Descripción general](#descripción-general)
- [Arquitectura del sistema](#arquitectura-del-sistema)
  - [Mapa de servicios](#mapa-de-servicios)
  - [Comunicación entre servicios](#comunicación-entre-servicios)
  - [Flujo completo de una petición](#flujo-completo-de-una-petición)
- [Microservicios](#microservicios)
  - [API Gateway — Puerto 8080](#api-gateway--puerto-8080)
  - [Eureka Server — Puerto 8761](#eureka-server--puerto-8761)
  - [iFit API — Puerto 8081](#ifit-api--puerto-8081)
  - [Ronnie API — Puerto 8082](#ronnie-api--puerto-8082)
- [Flujo principal de la aplicación](#flujo-principal-de-la-aplicación)
- [Coaches de IA disponibles](#coaches-de-ia-disponibles)
- [Seguridad y autenticación](#seguridad-y-autenticación)
  - [Keycloak y el realm ifit-realm](#keycloak-y-el-realm-ifit-realm)
  - [Doble validación JWT](#doble-validación-jwt)
  - [Flujo de autenticación completo](#flujo-de-autenticación-completo)
- [Base de datos](#base-de-datos)
- [Tecnologías](#tecnologías)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Instalación y puesta en marcha](#instalación-y-puesta-en-marcha)
  - [Arranque con Docker Compose](#arranque-con-docker-compose)
  - [Ejecución en local](#ejecución-en-local)
  - [Orden de arranque recomendado](#orden-de-arranque-recomendado)
- [Variables de entorno](#variables-de-entorno)
- [Endpoints principales](#endpoints-principales)

---

## Descripción general

iFit es una plataforma de entrenamiento personalizado construida sobre una arquitectura de cuatro microservicios. Los usuarios crean rutinas de entrenamiento de forma manual o mediante generación automática con inteligencia artificial, y pueden mantener conversaciones continuas con su entrenador virtual.

El sistema combina tres piezas fundamentales:

- Un **cuestionario adaptativo** (árbol de decisiones de 65 preguntas) que captura el perfil del usuario: objetivo, nivel de experiencia, equipamiento disponible, condiciones físicas, preferencias de entrenamiento.
- Un **motor de generación de rutinas** que construye un prompt enriquecido con los datos del cuestionario y lo envía al microservicio de IA, que devuelve una rutina estructurada en JSON usando un LLM.
- Un **módulo conversacional** que permite al usuario dialogar con su entrenador virtual. El historial de conversaciones se persiste en MySQL para mantener el contexto entre sesiones.

---

## Arquitectura del sistema

### Mapa de servicios

```
                         ┌──────────────────────────────┐
                         │   Cliente (App .NET MAUI)    │
                         └──────────────┬───────────────┘
                                        │ HTTP :8080
                                        ▼
                         ┌──────────────────────────────┐
                         │        API GATEWAY           │
                         │    Spring Cloud Gateway      │  Puerto 8080
                         │    (WebFlux — reactivo)      │
                         └──────────────┬───────────────┘
                Valida JWT              │  Distribuye según path
          contra JWKS Keycloak         │
                                        ├─── /ifit/api/v1/auth/**          (sin auth)
                                        ├─── /ifit/api/v1/exercise-images/** (sin auth)
                                        │            ▼
                                        │      iFit API :8081
                                        │
                                        ├─── /ifit/api/v1/ronnie/**
                                        ├─── /ifit/api/v1/serena/**
                                        ├─── /ifit/api/v1/kael/**          (con auth)
                                        ├─── /ifit/api/v1/eliud/**         (+ TokenRelay)
                                        ├─── /ifit/api/v1/messages/**
                                        │            ▼
                                        │      Ronnie API :8082
                                        │
                                        └─── /ifit/api/v1/**               (con auth)
                                                     ▼                    (+ TokenRelay)
                                               iFit API :8081

┌─────────────────────┐         ┌──────────────────────────────────────────┐
│   EUREKA SERVER     │         │              KEYCLOAK                    │
│   Puerto 8761       │◄────────│  Puerto 9090 — realm: ifit-realm         │
│  Service Discovery  │  todos  │  OAuth2/OIDC, JWT RS256                  │
│  (standalone)       │  se     │  accessToken: 300 s                      │
└─────────────────────┘ regist. │  Client: springboot-ifit-client          │
         ▲                      └──────────────────────────────────────────┘
         │ lb://IFIT / lb://RONNIE         ▲
         │ (resolución dinámica)           │ POST /token + Admin REST API
                                           │
          ┌────────────────────────────────┼───────────────────────────────┐
          │           iFit API             │              Ronnie API       │
          │           Puerto 8081          │              Puerto 8082      │
          │   Spring Boot + JPA            │   Spring Boot + LangChain4j  │
          │   MySQL DB: `ifit`             │   MySQL DB: `ronnie`         │
          └────────────────────┬───────────┴─────────────┬────────────────┘
                               │   RestTemplate           │
                               │  POST /{coach}/          │
                               │  generate-routine        │
                               └──────────────────────────┘
                                        │
                          ┌─────────────┴──────────────┐
                          │          Groq API           │
                          │  llama-3.3-70b-versatile    │
                          │  (chat + rutinas JSON)      │
                          └─────────────────────────────┘
```

### Comunicación entre servicios

| Origen | Destino | Protocolo | Propósito |
|---|---|---|---|
| API Gateway | iFit API | HTTP (`lb://IFIT` vía Eureka) | Enrutamiento de peticiones privadas y públicas de auth |
| API Gateway | Ronnie API | HTTP (`lb://RONNIE` vía Eureka) | Enrutamiento de peticiones de chat y generación |
| API Gateway | Keycloak | HTTPS (JWKS endpoint) | Descarga de clave pública RS256 para validar JWT |
| iFit API | Keycloak | HTTP (REST Admin API) | Registro de usuarios, obtención de tokens, logout |
| iFit API | Ronnie API | HTTP (RestTemplate) | Generación de rutinas con IA |
| iFit API | Eureka | HTTP | Registro y descubrimiento de servicios |
| Ronnie API | Eureka | HTTP | Registro y descubrimiento de servicios |
| Ronnie API | Groq API | HTTPS | Inferencia LLM para chat y rutinas |

### Flujo completo de una petición

El siguiente ejemplo ilustra el camino completo de una petición autenticada (generar rutina):

```
1. Cliente envía:
   POST /ifit/api/v1/routines/generate
   Authorization: Bearer <JWT>

2. API Gateway (puerto 8080):
   - Verifica firma RS256 del JWT contra JWKS de Keycloak
   - Verifica iss, exp, iat
   - El path coincide con IFIT-PRIVATE → StripPrefix=3 elimina /ifit/api/v1
   - TokenRelay copia el Bearer token en la petición interna
   - Resuelve lb://IFIT → IP real de iFit API vía Eureka

3. iFit API (puerto 8081) recibe:
   POST /routines/generate
   Authorization: Bearer <JWT>
   - Spring Security valida el JWT de nuevo (Resource Server)
   - RoutineService.generateRoutine() consulta las respuestas del cuestionario
   - Construye el prompt con datos del usuario + catálogo de ejercicios
   - IFitAIClient llama a Ronnie:
     POST http://ronnie:8082/master/generate-routine
     { memoryId, prompt, keycloakUserId }

4. Ronnie API (puerto 8082):
   - Master @AiService recibe el prompt
   - LangChain4j construye el contexto con groqJsonChatLanguageModel
   - Groq (llama-3.3-70b-versatile, temperature 0.3, json_object) genera la rutina
   - Devuelve JSON estructurado { routineName, days: [...] }

5. iFit API persiste la rutina en MySQL (DB: ifit)
6. Responde al cliente con la rutina generada
```

---

## Microservicios

### API Gateway — Puerto 8080

Punto de entrada único a todo el sistema. Implementado con **Spring Cloud Gateway** en modo reactivo (WebFlux/Netty). Ningún microservicio interno es accesible directamente desde el exterior.

**Rutas configuradas** (evaluadas en orden de prioridad):

| ID de ruta | Paths | Destino | Auth | TokenRelay |
|---|---|---|---|---|
| `IFIT-PUBLIC` | `/ifit/api/v1/auth/**`, `/ifit/api/v1/exercise-images/**` | iFit API | No | No |
| `RONNIE` | `/ifit/api/v1/{ronnie,serena,kael,eliud,messages}/**` | Ronnie API | Sí | Sí |
| `IFIT-PRIVATE` | `/ifit/api/v1/**` (resto) | iFit API | Sí | Sí |

Todas las rutas aplican `StripPrefix=3`, que elimina `/ifit/api/v1` del path antes de reenviar al servicio destino. Los microservicios solo ven sus propias rutas (`/users`, `/routines`, `/ronnie/chat`, etc.) y no conocen el prefijo público.

> Documentación detallada: [api-gateway/README.md](api-gateway/README.md)

---

### Eureka Server — Puerto 8761

Registro y descubrimiento de servicios (Netflix OSS, standalone). Todos los microservicios se registran al arrancar con su `spring.application.name`:

| Servicio | Nombre en Eureka |
|---|---|
| API Gateway | `ApiGatewayService` |
| iFit API | `IFIT` |
| Ronnie API | `ronnie` |

El API Gateway usa el esquema `lb://IFIT` y `lb://RONNIE` para resolver dinámicamente la IP y puerto reales sin URLs estáticas codificadas. Si un servicio escala a múltiples instancias, Eureka devuelve todas y el gateway balancea la carga.

---

### iFit API — Puerto 8081

Microservicio principal de negocio. Persiste en la base de datos `ifit` (MySQL).

| Módulo | Responsabilidad |
|---|---|
| `auth` | Registro, login, logout, verificación de email, refresco de tokens |
| `user` | CRUD de perfiles y niveles de experiencia (`ExperienceLevel`) |
| `questionnaire` | Motor de árbol de decisiones (65 preguntas, 5 subtrees por coach) |
| `training` | Rutinas, días de entrenamiento, ejercicios; cliente HTTP a Ronnie |
| `exercises` | Catálogo de ejercicios disponibles para el LLM |
| `coach` | Tipos de coach disponibles (`CoachModelType`) |
| `notification` | Emails transaccionales (verificación de cuenta) |

El módulo `training` contiene `IFitAIClient`, un cliente RestTemplate que llama a Ronnie en `/{coachType}/generate-routine` para la generación automática. `CoachType` es un enum (MASTER, RONNIE, SERENA, ELIUD, KAEL) que define el endpoint destino.

> Documentación detallada: [ifit/README.md](ifit/README.md)

---

### Ronnie API — Puerto 8082

Microservicio de inteligencia artificial. Implementado con **LangChain4j**. Persiste el historial de conversaciones en la base de datos `ronnie` (MySQL).

| Módulo | Responsabilidad |
|---|---|
| `master` | Genera rutinas en JSON a partir del prompt construido por iFit |
| `ronnie` | Chat con el coach Ronnie (hipertrofia y fuerza) |
| `serena` | Chat con la coach Serena (fitness femenino y bienestar) |
| `kael` | Chat con el coach Kael (calistenia y fuerza funcional) |
| `eliud` | Chat con el coach Eliud (running y cardio) |
| `message` | Historial de conversaciones persistido por sesión de memoria |

Cada coach es un `@AiService` de LangChain4j (`wiringMode=EXPLICIT`) respaldado por:
- Un `groqChatLanguageModel` (temperature 0.5) para conversación fluida.
- Un `groqJsonChatLanguageModel` (temperature 0.3, `json_object`) exclusivo de `Master` para generación estructurada.
- Un `PersistentChatMemoryStore` sobre MySQL para memoria entre sesiones.
- Un `EmbeddingStoreContentRetriever` (RAG) que inyecta la personalidad del coach desde un fichero `.txt` en cada mensaje.

> Documentación detallada: [ronnie/README.md](ronnie/README.md)

---

## Flujo principal de la aplicación

1. **Registro y verificación.** El usuario se registra. iFit crea la cuenta en Keycloak y en MySQL de forma transaccional. Keycloak envía un código de verificación de 6 dígitos al email del usuario.

2. **Login.** iFit llama al endpoint `grant_type=password` de Keycloak y devuelve el `access_token` (JWT RS256, 300 s de vida) y `refresh_token` al cliente.

3. **Cuestionario adaptativo.** El usuario responde el cuestionario. Hay 65 preguntas organizadas en 5 sub-árboles: preguntas generales (Q1-Q13) comunes a todos los coaches, y un bloque específico por coach (Ronnie Q14-Q26, Serena Q27-Q39, Kael Q40-Q52, Eliud Q53-Q65). Cada opción determina la siguiente pregunta mediante `next_question_id`. Las preguntas sensibles (peso, lesiones) incluyen una opción "Prefiero no responder" que el sistema serializa como `[No respondida]` en el prompt.

4. **Generación de rutina.** iFit construye el prompt con los datos del cuestionario + catálogo completo de ejercicios (`exercises.txt`) y llama a Ronnie vía RestTemplate. Ronnie devuelve la rutina en JSON. iFit la persiste y devuelve al cliente.

5. **Chat con el entrenador.** El usuario puede chatear con cualquiera de los 4 coaches. El historial se mantiene por `memoryId` (identificador de conversación) y persiste entre sesiones en MySQL. El contexto de personalidad se inyecta en cada mensaje mediante RAG desde ficheros `.txt`.

---

## Coaches de IA disponibles

| Coach | Especialidad | Modelo de chat | Temperatura |
|---|---|---|---|
| **Master** | Entrenador general (solo generación de rutinas) | `groqJsonChatLanguageModel` | 0.3 |
| **Ronnie** | Hipertrofia y fuerza muscular, alto volumen | `groqChatLanguageModel` | 0.5 |
| **Eliud** | Running, cardio y rendimiento aeróbico | `groqChatLanguageModel` | 0.5 |
| **Serena** | Fitness femenino, tonificación y bienestar | `groqChatLanguageModel` | 0.5 |
| **Kael** | Calistenia y fuerza funcional sin equipamiento | `groqChatLanguageModel` | 0.5 |

Todos usan el modelo `llama-3.3-70b-versatile` de Groq. Master usa `responseFormat("json_object")` para garantizar salida estructurada; los coaches de chat usan texto libre.

---

## Seguridad y autenticación

### Keycloak y el realm ifit-realm

El servidor de identidad es **Keycloak 23.0**, configurado con el realm `ifit-realm`:

| Parámetro | Valor |
|---|---|
| Realm | `ifit-realm` |
| Algoritmo de firma | RS256 |
| Vida del access token | 300 segundos (5 minutos) |
| Client ID | `springboot-ifit-client` |
| Client type | Confidential |
| Direct Access Grants | Habilitado (`grant_type=password`) |
| Roles | `user`, `admin` |

El realm puede importarse directamente desde el fichero `ifit-realm-export.json` incluido en la raíz del proyecto, lo que evita la configuración manual.

El JWKS endpoint de Keycloak es:
```
http://localhost:9090/realms/ifit-realm/protocol/openid-connect/certs
```

Spring Security lo descarga al arrancar y cachea la clave pública para validar tokens sin consultar Keycloak en cada petición.

### Doble validación JWT

El JWT se valida de forma independiente en dos puntos del sistema:

```
1. API Gateway (Spring Cloud Gateway + SecurityConfig)
   - Verifica firma RS256 contra JWKS de Keycloak
   - Verifica iss, exp, iat
   - Bloquea peticiones con token inválido/ausente (excepto rutas públicas)

2. Microservicio destino (iFit o Ronnie)
   - iFit: Spring Security como OAuth2 Resource Server
   - Ronnie: JwtUtils.extractUserId() lee el claim 'sub' del Bearer token
   - Cada servicio puede aplicar @PreAuthorize con sus propias reglas
```

Esta arquitectura de defensa en profundidad garantiza que aunque un atacante bypassease el gateway, los microservicios internos seguirían rechazando tokens inválidos.

### Flujo de autenticación completo

```
Registro:
  POST /ifit/api/v1/auth/register (público)
    → iFit crea usuario en Keycloak (Admin REST API)
    → iFit guarda perfil en MySQL (DB: ifit)
    → Si Keycloak falla → rollback en MySQL (@Transactional)
    → Keycloak envía email con código de 6 dígitos

  POST /ifit/api/v1/auth/verify (público)
    → iFit valida el código y activa la cuenta en Keycloak

Login:
  POST /ifit/api/v1/auth/login (público)
    → iFit llama a Keycloak: grant_type=password
    → Keycloak devuelve { access_token, refresh_token, expires_in }
    → iFit lo reenvía al cliente (sin almacenar tokens)

Peticiones autenticadas:
  Authorization: Bearer <access_token>
    → Gateway valida → TokenRelay → microservicio destino
    → Microservicio valida de nuevo → procesa la petición

Refresco:
  POST /ifit/api/v1/auth/refresh
    → iFit llama a Keycloak con el refresh_token
    → Devuelve nuevo access_token al cliente

Logout:
  POST /ifit/api/v1/auth/logout
    → iFit llama a Keycloak para invalidar la sesión
```

---

## Base de datos

El proyecto utiliza **dos bases de datos MySQL independientes**, creadas automáticamente por `init-db.sql` al levantar el contenedor de MySQL:

| Base de datos | Servicio propietario | Contenido |
|---|---|---|
| `ifit` | iFit API | Usuarios, cuestionarios, respuestas, rutinas, ejercicios, coaches |
| `ronnie` | Ronnie API | Historial de mensajes, tipos de mensaje (`user`, `ai`, `system`) |

Cada microservicio accede exclusivamente a su propia base de datos. No hay acceso cruzado ni esquema compartido.

**Notas de inicialización:**
- iFit: `spring.sql.init.mode=always` ejecuta `data.sql` en cada arranque, que contiene los INSERTs del catálogo de preguntas y ejercicios. Los INSERTs usan `ON DUPLICATE KEY UPDATE` para ser idempotentes.
- Ronnie: `data.sql` borra y recrea los tipos de mensaje (`user`, `ai`, `system`) en cada arranque. Solo afecta a la tabla de tipos, no al historial de conversaciones.

---

## Tecnologías

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.4.4 |
| Arquitectura | Spring Cloud 2025.0.0 (Gateway, Eureka/Netflix OSS) |
| Reactivo | Spring WebFlux, Project Reactor, Netty |
| Seguridad | Keycloak 23.0 (OAuth2/OIDC), Spring Security, JWT RS256 |
| IA / LLM | LangChain4j, Groq API (`llama-3.3-70b-versatile`), Ollama (local) |
| RAG | LangChain4j InMemoryEmbeddingStore, EmbeddingStoreContentRetriever |
| Persistencia | Spring Data JPA, Hibernate, MySQL 8.0 |
| HTTP interno | RestTemplate (IFit → Ronnie) |
| Documentación API | SpringDoc OpenAPI 3 (Swagger UI) |
| Emails | Spring Mail |
| Contenedores | Docker, Docker Compose |
| Build | Maven (Maven Wrapper incluido) |

---

## Estructura del proyecto

```
ifit/
├── docker-compose.yml          # Solo MySQL (3306) y Keycloak (9090) activos por defecto
├── init-db.sql                 # Crea las bases de datos `ifit` y `ronnie`
├── ifit-realm-export.json      # Exportación del realm Keycloak — importar para evitar config manual
│
├── api-gateway/                # Spring Cloud Gateway (WebFlux reactivo) — Puerto 8080
│   └── src/main/
│       ├── java/.../config/
│       │   └── SecurityConfig.java      # @EnableWebFluxSecurity, OAuth2 Resource Server
│       └── resources/
│           └── application.yaml         # Rutas IFIT-PUBLIC, RONNIE, IFIT-PRIVATE
│
├── eureka-server/              # Netflix Eureka standalone — Puerto 8761
│   └── src/main/resources/
│       └── application.yml
│
├── ifit/                       # Microservicio principal — Puerto 8081
│   └── src/main/java/.../
│       └── modules/
│           ├── auth/           # Login, registro, verificación, Keycloak REST
│           ├── user/           # Perfiles y ExperienceLevel
│           ├── questionnaire/  # Árbol de 65 preguntas con next_question_id
│           ├── training/       # Rutinas, IFitAIClient, CoachType enum
│           ├── exercises/      # Catálogo de ejercicios
│           ├── coach/          # CoachModelType
│           └── notification/   # Emails transaccionales
│
└── ronnie/                     # Microservicio IA — Puerto 8082
    └── src/main/
        ├── java/.../
        │   ├── configuration/
        │   │   ├── GroqModelConfiguration.java          # groqChatLanguageModel + groqJsonChatLanguageModel
        │   │   ├── EmbeddingStoreContentConfiguration.java  # RAG + PersistentChatMemoryStore
        │   │   └── ChatContext.java                     # InheritableThreadLocal userId/coachName
        │   └── modules/
        │       ├── coach/
        │       │   ├── master/     # @AiService para generación de rutinas en JSON
        │       │   ├── ronnie/     # @AiService + RAG para chat
        │       │   ├── serena/     # @AiService + RAG para chat
        │       │   ├── kael/       # @AiService + RAG para chat
        │       │   └── eliud/      # @AiService + RAG para chat
        │       └── message/        # Persistencia del historial de conversaciones
        └── resources/
            └── langchain4j/
                └── assistants-personality/
                    ├── exercises.txt     # Catálogo JSON de ejercicios para el LLM
                    ├── ronnie.txt        # Personalidad del coach Ronnie (RAG)
                    ├── serena.txt        # Personalidad de Serena (RAG)
                    ├── kael.txt          # Personalidad de Kael (RAG)
                    └── eliud.txt         # Personalidad de Eliud (RAG)
```

---

## Instalación y puesta en marcha

### Requisitos previos

- Docker y Docker Compose
- Java 21 y Maven 3.9+ (para ejecutar los microservicios en local)
- API Key de Groq (o Ollama instalado localmente para el modo offline)

### Arranque con Docker Compose

El `docker-compose.yml` levanta únicamente **MySQL** y **Keycloak** por defecto. El resto de servicios están comentados para permitir ejecutarlos en local durante el desarrollo.

```bash
# 1. Clonar el repositorio
git clone <url-del-repositorio>
cd ifit

# 2. Levantar la infraestructura base
docker-compose up -d

# Servicios activos:
#   MySQL    → localhost:3306  (crea automáticamente las DBs `ifit` y `ronnie`)
#   Keycloak → localhost:9090  (admin: admin / admin)
```

**Configurar Keycloak** (una sola vez): En el panel de administración (`http://localhost:9090`), importar el fichero `ifit-realm-export.json` para crear el realm `ifit-realm` con toda la configuración preestablecida (roles, cliente, algoritmo RS256, tiempos de vida de tokens).

### Ejecución en local

```bash
# Terminal 1 — Eureka Server
cd eureka-server
./mvnw spring-boot:run

# Terminal 2 — API Gateway
cd api-gateway
./mvnw spring-boot:run

# Terminal 3 — iFit API
cd ifit
./mvnw spring-boot:run

# Terminal 4 — Ronnie API (requiere GROQ_API_KEY en el entorno o en application.properties)
cd ronnie
./mvnw spring-boot:run
```

### Orden de arranque recomendado

El orden importa porque cada servicio depende de los anteriores para registrarse correctamente:

```
1. MySQL          → localhost:3306   (DB ifit + DB ronnie se crean al arrancar)
2. Keycloak       → localhost:9090   (debe estar activo antes de que iFit valide tokens)
3. Eureka Server  → localhost:8761   (debe estar activo antes de que los demás se registren)
4. API Gateway    → localhost:8080   (necesita Eureka para resolver lb:// y Keycloak para JWKS)
5. iFit API       → localhost:8081   (se registra en Eureka; necesita Keycloak y MySQL)
6. Ronnie API     → localhost:8082   (se registra en Eureka; necesita MySQL y Groq API Key)
```

> El gateway puede arrancar antes que iFit y Ronnie — Eureka actualizará el registro cuando estén disponibles. Pero Keycloak y Eureka deben estar activos antes de que el gateway inicie.

---

## Variables de entorno

### iFit API

| Variable | Descripción | Ejemplo |
|---|---|---|
| `SPRING_DATASOURCE_URL` | URL de conexión a MySQL | `jdbc:mysql://localhost:3306/ifit` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de base de datos | `root` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de base de datos | `root` |
| `KEYCLOAK_TOKEN-URL` | Endpoint de tokens de Keycloak | `http://localhost:9090/realms/ifit-realm/protocol/openid-connect/token` |
| `KEYCLOAK_ADMIN-URL` | URL de la API de administración | `http://localhost:9090` |
| `RONNIE_SERVICE_URL` | URL base del microservicio Ronnie | `http://localhost:8082` |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | URL del servidor Eureka | `http://localhost:8761/eureka/` |

### Ronnie API

| Variable | Descripción | Ejemplo |
|---|---|---|
| `GROQ_API_KEY` | API Key de Groq — **nunca hardcodear en ficheros versionados** | `gsk_...` |
| `GROQ_BASE-URL` | URL base de la API de Groq | `https://api.groq.com/openai/v1` |
| `GROQ_MODEL-NAME` | Modelo de lenguaje a usar | `llama-3.3-70b-versatile` |
| `OLLAMA_BASE-URL` | URL base de Ollama (modo local) | `http://localhost:11434` |
| `SPRING_DATASOURCE_URL` | URL de conexión a MySQL | `jdbc:mysql://localhost:3306/ronnie` |

---

## Endpoints principales

La documentación completa de la API está disponible en **Swagger UI** una vez arrancados los servicios:

- **iFit API:** `http://localhost:8081/swagger-ui.html`
- **Ronnie API:** `http://localhost:8082/swagger-ui.html`

Todos los endpoints se exponen al cliente con el prefijo `/ifit/api/v1/` a través del API Gateway (`http://localhost:8080/ifit/api/v1/...`).

### Autenticación (públicos, sin token)

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/auth/register` | Registro de nuevo usuario |
| `POST` | `/auth/verify` | Verificar email con código de 6 dígitos |
| `POST` | `/auth/login` | Login — devuelve access_token y refresh_token |
| `POST` | `/auth/refresh` | Renovar access_token con refresh_token |
| `POST` | `/auth/logout` | Cerrar sesión e invalidar la sesión en Keycloak |

### Cuestionario

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/questionnaires` | Listar cuestionarios disponibles |
| `POST` | `/questionnaires/{id}/start` | Iniciar cuestionario (elige coach) |
| `POST` | `/questionnaires/responses/{responseId}/answer` | Responder una pregunta |
| `GET` | `/questionnaires/responses/{responseId}` | Consultar respuestas registradas |

### Rutinas

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/routines/generate` | Generar rutina con IA a partir de un cuestionario |
| `POST` | `/routines` | Crear rutina manualmente |
| `GET` | `/routines/user/{userId}` | Listar rutinas de un usuario |
| `GET` | `/routines/{routineId}` | Obtener detalle de una rutina |
| `PUT` | `/routines/{routineId}` | Actualizar rutina |
| `DELETE` | `/routines/{routineId}` | Eliminar rutina |

### Chat con coaches (enrutado a Ronnie API)

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/ronnie/chat` | Enviar mensaje al coach Ronnie |
| `POST` | `/serena/chat` | Enviar mensaje a la coach Serena |
| `POST` | `/kael/chat` | Enviar mensaje al coach Kael |
| `POST` | `/eliud/chat` | Enviar mensaje al coach Eliud |
| `GET` | `/messages/{memoryId}` | Obtener historial de una conversación |
| `GET` | `/messages/max-memory-id` | Obtener el ID de conversación más reciente |

---

## Autor

**Juan García Candón**  
Universidad de Cádiz — Escuela Superior de Ingeniería  
Trabajo Final de Grado (TFG), 2024–2025
