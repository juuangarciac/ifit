<div align="center">

# iFit — API de Gestión de Rutinas con IA

**Plataforma de entrenamiento personalizado basada en microservicios, cuestionarios adaptativos e inteligencia artificial**

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.4-6DB33F?style=flat-square&logo=springboot)
![Keycloak](https://img.shields.io/badge/Keycloak-23.0-4D4D4D?style=flat-square&logo=keycloak)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)

</div>

---

## Tabla de contenidos

- [Descripción general](#descripción-general)
- [Flujo principal de la aplicación](#flujo-principal-de-la-aplicación)
- [Arquitectura del sistema](#arquitectura-del-sistema)
- [Microservicios](#microservicios)
- [Coaches de IA disponibles](#coaches-de-ia-disponibles)
- [Tecnologías](#tecnologías)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Instalación y puesta en marcha](#instalación-y-puesta-en-marcha)
- [Variables de entorno y configuración](#variables-de-entorno-y-configuración)
- [Endpoints principales](#endpoints-principales)
- [Seguridad y autenticación](#seguridad-y-autenticación)

---

## Descripción general

iFit es una API REST construida sobre una arquitectura de microservicios que permite a los usuarios crear y hacer seguimiento de rutinas de entrenamiento, tanto de forma manual como mediante generación automática con inteligencia artificial.

El sistema combina un cuestionario adaptativo (árbol de decisiones), un motor de generación de rutinas basado en LLMs y un módulo conversacional que permite al usuario consultar a su entrenador virtual en cualquier momento.

---

## Flujo principal de la aplicación

1. **Registro y autenticación.** El usuario se registra en la plataforma y verifica su correo electrónico. La autenticación se gestiona a través de Keycloak (OAuth2 / JWT).

2. **Cuestionario adaptativo.** El usuario responde un cuestionario con estructura de árbol de decisiones: cada respuesta condiciona la siguiente pregunta, adaptándose al perfil real del usuario (objetivo, nivel de experiencia, equipamiento disponible, etc.).

3. **Selección de coach y generación de rutina.** Al empezar el cuestionario, el usuario elige un modelo de entrenador IA. El sistema construye un prompt enriquecido con los datos del cuestionario y la personalidad del coach, y lo envía al microservicio **Ronnie** para generar una rutina personalizada mediante un LLM (Groq / Ollama).

4. **Seguimiento de la rutina.** La rutina generada queda almacenada y el usuario puede consultar sus ejercicios, registrar el progreso y adaptar el plan.

5. **Chat con el entrenador.** El usuario puede abrir conversaciones con su coach IA para resolver dudas sobre técnica, progresión o nutrición. El historial de mensajes se persiste por sesión de memoria.

---

## Arquitectura del sistema

```
Cliente (App / Frontend)
        │
        ▼
┌────────────────────┐
│    API Gateway     │  :8080  — Punto de entrada único
│  (Spring Cloud GW) │         Valida JWT, enruta y aplica TokenRelay
└────────┬───────────┘
         │
         ├──── /auth/**  ──────────────►  iFit API  (ruta pública)
         ├──── /ronnie/** ─────────────►  Ronnie API (IA)
         ├──── /serena/**  ────────────►  Ronnie API (IA)
         ├──── /kael/**   ─────────────►  Ronnie API (IA)
         ├──── /eliud/**  ─────────────►  Ronnie API (IA)
         └──── /**  ──────────────────►  iFit API  (rutas protegidas)

┌──────────────────┐    ┌──────────────────┐
│   Eureka Server  │    │    Keycloak       │
│  (Service Disc.) │    │  OAuth2 / OIDC   │
│     :8761        │    │     :9090         │
└──────────────────┘    └──────────────────┘

┌──────────────────┐    ┌──────────────────┐
│   iFit API       │    │   Ronnie API     │
│  (Core service)  │    │  (IA service)    │
│     :8081        │    │     :8082        │
└────────┬─────────┘    └────────┬─────────┘
         └──────────────────────┘
                    │
              MySQL :3306
          (Base de datos compartida)
```

---

## Microservicios

### API Gateway (`api-gateway`) — Puerto 8080

Punto de entrada único a todo el sistema. Implementado con **Spring Cloud Gateway** en modo reactivo. Se encarga de:

- Validar los tokens JWT emitidos por Keycloak antes de que cualquier petición llegue a los servicios internos.
- Enrutar las solicitudes al microservicio correcto según el path de la URL.
- Propagar el token de acceso a los servicios internos mediante el filtro `TokenRelay`.
- Exponer una ruta pública para los endpoints de autenticación (`/auth/**`) que no requieren token previo.

**Rutas configuradas:**

| ID de ruta | Path | Servicio destino | Token requerido |
|---|---|---|---|
| `IFIT-PUBLIC` | `/ifit/api/v1/auth/**`, `/ifit/api/v1/exercise-images/**` | iFit API | No |
| `RONNIE` | `/ifit/api/v1/ronnie/**`, `/ifit/api/v1/serena/**`, `/ifit/api/v1/kael/**`, `/ifit/api/v1/eliud/**`, `/ifit/api/v1/messages/**` | Ronnie API | Sí |
| `IFIT-PRIVATE` | `/ifit/api/v1/**` | iFit API | Sí |

---

### Eureka Server (`eureka-server`) — Puerto 8761

Registro y descubrimiento de servicios. Todos los microservicios se registran en Eureka al arrancar, lo que permite al API Gateway resolver los nombres lógicos (`lb://IFIT`, `lb://RONNIE`) sin necesidad de IPs estáticas.

---

### iFit API (`ifit`) — Puerto 8081

Microservicio principal de negocio. Gestiona todos los datos de usuarios, cuestionarios, rutinas y ejercicios. Sus módulos principales son:

**Autenticación (`auth`):** Registro de usuarios, login, logout, verificación de email y refresco de tokens. Se comunica con Keycloak para la creación de usuarios y la gestión de tokens JWT.

**Usuarios (`user`):** CRUD de perfiles de usuario y niveles de experiencia (`ExperienceLevel`). Cada usuario tiene asociado un nivel que condiciona la generación de rutinas.

**Cuestionario (`questionnaire`):** Motor de árbol de decisiones. Almacena preguntas, opciones de respuesta y las respuestas registradas por cada usuario. La estructura permite que cada opción de respuesta derive en una pregunta diferente, adaptando el cuestionario al perfil del usuario.

**Rutinas (`training`):** Gestión de rutinas de entrenamiento (creación manual y generada por IA), días de entrenamiento y ejercicios asignados. Incluye el cliente HTTP (`IFitAIClient`) que se comunica con el microservicio Ronnie para la generación automática.

**Ejercicios (`exercises`):** Catálogo de ejercicios disponibles para la generación de rutinas. El LLM solo puede usar ejercicios de este catálogo.

**Coaches (`coach`):** Gestión de los tipos de modelo de entrenador (`CoachModelType`) disponibles en la plataforma.

**Notificaciones (`notification`):** Servicio de envío de emails (verificación de cuenta, avisos).

---

### Ronnie API (`ronnie`) — Puerto 8082

Microservicio de inteligencia artificial. Gestiona la comunicación con los modelos LLM y el almacenamiento del historial de conversaciones. Implementado con **LangChain4j**.

**Generación de rutinas:** El endpoint `/master/generate-routine` recibe el prompt construido en la iFit API (datos del cuestionario + catálogo de ejercicios + personalidad del coach) y devuelve una rutina estructurada en JSON mediante el AI Service `Master`.

**Chat con coaches:** Cada coach (Ronnie, Serena, Kael, Eliud) dispone de su propio controller y AI Service con memoria persistente por conversación. El usuario puede mantener conversaciones continuas con su entrenador, y los mensajes se almacenan en MySQL para preservar el contexto entre sesiones.

**Modelos soportados:**
- **Groq** (por defecto en producción): API compatible con OpenAI, usando `llama3` u otros modelos soportados.
- **Ollama** (alternativa local): para despliegue completamente offline.

---

## Coaches de IA disponibles

Cada coach aporta su personalidad, estilo y especialidad al prompt enviado al LLM, orientando la rutina generada hacia su área de expertise:

| Coach | Especialidad | Estilo |
|---|---|---|
| **Master** | Entrenador general certificado | Neutro, estructurado, completo |
| **Ronnie** | Hipertrofia y fuerza muscular | Directo, motivador, alto volumen |
| **Eliud** | Running, cardio y rendimiento aeróbico | Sereno, progresivo, sin sobrecargar articulaciones |
| **Serena** | Fitness femenino, tonificación y bienestar | Empático, positivo, sesiones de 30–45 min |
| **Kael** | Calistenia y fuerza funcional sin equipamiento | Técnico, claro, entrenamientos en casa/parque |

---

## Tecnologías

| Capa | Tecnología |
|---|---|
| Framework principal | Spring Boot 3.4.4 |
| Arquitectura | Spring Cloud Gateway, Eureka (Netflix OSS) |
| Seguridad | Keycloak 23.0 (OAuth2 / OpenID Connect), Spring Security, JWT |
| IA / LLM | LangChain4j, Groq API, Ollama |
| Persistencia | Spring Data JPA, MySQL 8.0 |
| Documentación API | SpringDoc OpenAPI 3 (Swagger UI) |
| Emails | Spring Mail |
| Contenedores | Docker, Docker Compose |
| Build | Maven (Maven Wrapper incluido) |
| Java | Java 21 |

---

## Estructura del proyecto

```
ifit/
├── docker-compose.yml          # Orquestación de todos los servicios
├── api-gateway/                # Spring Cloud Gateway — Puerto 8080
│   └── src/main/
│       ├── java/.../config/
│       │   └── SecurityConfig.java
│       └── resources/
│           └── application.yaml
├── eureka-server/              # Service Discovery — Puerto 8761
├── ifit/                       # Microservicio principal — Puerto 8081
│   └── src/main/java/.../
│       ├── modules/
│       │   ├── auth/           # Login, registro, Keycloak
│       │   ├── user/           # Usuarios y niveles de experiencia
│       │   ├── questionnaire/  # Cuestionario adaptativo
│       │   ├── training/       # Rutinas y cliente IA
│       │   ├── exercises/      # Catálogo de ejercicios
│       │   ├── coach/          # Tipos de coach
│       │   └── notification/   # Emails
│       └── exception/          # Manejo global de excepciones
└── ronnie/                     # Microservicio IA — Puerto 8082
    └── src/main/java/.../
        ├── configuration/      # Groq, Ollama, memoria persistente
        └── modules/
            ├── coach/
            │   ├── master/     # AI Service generación de rutinas
            │   ├── ronnie/     # Coach Ronnie (chat)
            │   ├── serena/     # Coach Serena (chat)
            │   ├── kael/       # Coach Kael (chat)
            │   └── eliud/      # Coach Eliud (chat)
            └── message/        # Historial de conversaciones
```

---

## Instalación y puesta en marcha

### Requisitos previos

- Docker y Docker Compose instalados.
- Java 21 y Maven (opcional, si se quiere ejecutar fuera de Docker).
- Una API Key de Groq (o Ollama instalado localmente para el modo offline).

### Arranque rápido con Docker Compose

El `docker-compose.yml` incluido levanta la infraestructura base (MySQL, Keycloak, Eureka, API Gateway). Los microservicios `ifit` y `ronnie` están comentados por defecto para permitir ejecutarlos en local durante el desarrollo.

```bash
# 1. Clonar el repositorio
git clone <url-del-repositorio>
cd ifit

# 2. Levantar la infraestructura
docker-compose up -d

# Servicios que arrancará:
#   MySQL      → localhost:3306
#   Keycloak   → localhost:9090
#   Eureka     → localhost:8761
#   API Gateway → localhost:8080
```

### Ejecución en local (desarrollo)

```bash
# iFit API
cd ifit
./mvnw spring-boot:run

# Ronnie API (en otro terminal)
cd ronnie
./mvnw spring-boot:run
```

### Configuración de Keycloak

1. Acceder al panel de administración en `http://localhost:9090` (usuario: `admin`, contraseña: `admin`).
2. Crear un nuevo realm llamado `ifit-realm`.
3. Crear un cliente con el ID de cliente configurado en `application.properties`.
4. Configurar los roles necesarios para usuarios y administradores.

---

## Variables de entorno y configuración

### iFit API (`application-docker.properties`)

| Variable | Descripción |
|---|---|
| `SPRING_DATASOURCE_URL` | URL de conexión a MySQL |
| `SPRING_DATASOURCE_USERNAME` | Usuario de base de datos |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de base de datos |
| `KEYCLOAK_TOKEN-URL` | URL del endpoint de tokens de Keycloak |
| `KEYCLOAK_ADMIN-URL` | URL de la API de administración de Keycloak |
| `RONNIE_SERVICE_URL` | URL base del microservicio Ronnie |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | URL del servidor Eureka |

### Ronnie API

| Variable | Descripción |
|---|---|
| `GROQ_BASE-URL` | URL base de la API de Groq |
| `GROQ_API-KEY` | API Key de Groq |
| `GROQ_MODEL-NAME` | Nombre del modelo a usar (ej. `llama3-70b-8192`) |
| `OLLAMA_BASE-URL` | URL base de Ollama (modo local) |
| `SPRING_DATASOURCE_URL` | URL de conexión a MySQL |

---

## Endpoints principales

La documentación completa de la API está disponible en **Swagger UI** una vez arrancados los servicios:

- **iFit API:** `http://localhost:8081/swagger-ui.html`
- **Ronnie API:** `http://localhost:8082/swagger-ui.html`

### Autenticación

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/auth/login` | Iniciar sesión, devuelve JWT |
| `POST` | `/auth/register` | Registrar nuevo usuario |
| `POST` | `/auth/refresh` | Renovar token de acceso |
| `POST` | `/auth/logout` | Cerrar sesión e invalidar token |
| `POST` | `/auth/verify` | Verificar email con código |

### Cuestionario

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/questionnaires` | Listar cuestionarios disponibles |
| `POST` | `/questionnaires/{id}/answer` | Enviar respuesta a una pregunta |
| `GET` | `/questionnaires/responses/{responseId}` | Consultar respuestas de un cuestionario |

### Rutinas

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/routines/generate` | Generar rutina con IA a partir de un cuestionario |
| `POST` | `/routines` | Crear rutina manualmente |
| `GET` | `/routines/user/{userId}` | Listar rutinas de un usuario |
| `GET` | `/routines/{routineId}` | Obtener detalle de una rutina |
| `PUT` | `/routines/{routineId}` | Actualizar rutina |
| `DELETE` | `/routines/{routineId}` | Eliminar rutina |

### Chat con coaches (Ronnie API)

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/ronnie/chat` | Enviar mensaje al coach Ronnie |
| `POST` | `/serena/chat` | Enviar mensaje a la coach Serena |
| `POST` | `/kael/chat` | Enviar mensaje al coach Kael |
| `POST` | `/eliud/chat` | Enviar mensaje al coach Eliud |
| `GET` | `/messages/{memoryId}` | Obtener historial de conversación |

---

## Seguridad y autenticación

El sistema usa **Keycloak** como servidor de identidad OAuth2 / OpenID Connect. El flujo de autenticación es el siguiente:

1. El cliente envía credenciales al endpoint `/auth/login` de la iFit API.
2. La iFit API valida las credenciales contra Keycloak y devuelve el `access_token` y `refresh_token`.
3. El cliente incluye el `access_token` como cabecera `Authorization: Bearer <token>` en cada petición al API Gateway.
4. El API Gateway valida la firma del JWT contra el JWKS endpoint de Keycloak (`/realms/ifit-realm/protocol/openid-connect/certs`) y, si es válido, propaga el token a los microservicios internos.
5. El `refresh_token` se usa para obtener un nuevo `access_token` antes de que este expire, sin necesidad de volver a introducir las credenciales.

Los endpoints de registro y login son públicos. Todos los demás requieren un JWT válido.
