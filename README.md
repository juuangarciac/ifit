<div align="center">

# iFit — Plataforma de Entrenamiento Personalizado con IA

**Sistema distribuido de microservicios con cuestionario adaptativo, generación de rutinas por LLM, chat con entrenadores virtuales y panel de administración back-office.**

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.7-6DB33F?style=flat-square&logo=springboot)
![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-2025.0.0-6DB33F?style=flat-square&logo=spring)
![Keycloak](https://img.shields.io/badge/Keycloak-23.0-4D4D4D?style=flat-square&logo=keycloak)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql)
![LangChain4j](https://img.shields.io/badge/LangChain4j-latest-orange?style=flat-square&logo=chainlink)
![Vaadin](https://img.shields.io/badge/Vaadin-24.8-00b4f0?style=flat-square&logo=vaadin)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)

</div>

---

## 📚 Cómo leer esta documentación

Este proyecto está documentado en capas de especialización, como una cebolla. **Empieza aquí** para entender cómo funcionan los módulos juntos. Luego consulta los READMEs específicos de cada módulo para detalles técnicos.

```
┌──────────────────────────────────────────────────────────────────────┐
│ CAPA 0: TÚ ERES AQUÍ (Introducción General)                          │
│ Cómo se relacionan todos los módulos, terminología, decisiones       │
│ Este archivo — README.md                                             │
└──────┬─────────────────────────────────────────────────────────────┘
       │
       ├─────────────────────────────────────────────────────────────┐
       │ CAPA 1: Módulos de Dominio (Lógica de Negocio + IA)        │
       ├─────────────────────────────────────────────────────────────┤
       │ • ifit/ — Gestión de usuarios, cuestionarios, rutinas,     │
       │   integración con Keycloak (autenticación)                  │
       │   → ifit/README.md                                          │
       │                                                              │
       │ • ronnie/ — Motor de IA para generar rutinas y chat,       │
       │   LangChain4j + modelos Groq                               │
       │   → ronnie/README.md                                        │
       └─────────────────────────────────────────────────────────────┘
       │
       ├─────────────────────────────────────────────────────────────┐
       │ CAPA 2: Infraestructura (Red, Descubrimiento, UI)          │
       ├─────────────────────────────────────────────────────────────┤
       │ • api-gateway/ — Punto de entrada único, validación de     │
       │   tokens JWT, enrutamiento reactivo (WebFlux)              │
       │   → api-gateway/README.md                                   │
       │                                                              │
       │ • eureka-server/ — Registro de servicios, permite que      │
       │   los módulos se encuentren por nombre lógico               │
       │   → eureka-server/README.md                                 │
       │                                                              │
       │ • admin-panel/ — Panel web en Vaadin para administrar      │
       │   usuarios, ejercicios, coaches, niveles, cuestionarios    │
       │   → admin-panel/README.md                                   │
       └─────────────────────────────────────────────────────────────┘
```

**Flujo de lectura recomendado:**
1. Lee esta introducción (entiende el sistema como un todo)
2. Mira "[Arquitectura del Sistema](#arquitectura-del-sistema)" para ver cómo fluye una petición
3. Luego lee el README específico del módulo que necesites profundizar

---

## 🎯 Descripción General

**iFit** es una plataforma inteligente de fitness que:

1. **Evalúa a los usuarios** mediante un cuestionario adaptativo de 65 preguntas (árbol de decisión dinámico)
2. **Genera rutinas personalizadas** usando inteligencia artificial (LLM) basadas en el perfil del usuario
3. **Ofrece chat interactivo** con 4 coaches especializados (musculación, bienestar, calistenia, running)
4. **Proporciona un panel administrativo** para gestionar usuarios, ejercicios y configuraciones

El sistema se distribuye en **5 microservicios** independientes que se comunican entre sí, con una única frontera de seguridad: el API Gateway.

### Casos de uso principales:

- **Usuario final (App MAUI)**:
  1. Registrarse e iniciar sesión (autenticación vía Keycloak)
  2. Responder el cuestionario de evaluación física
  3. Recibir una rutina generada por IA
  4. Chatear con entrenadores virtuales

- **Administrador (Panel Vaadin)**:
  1. Gestionar catálogo de ejercicios
  2. Crear y editar cuestionarios
  3. Supervisar usuarios
  4. Configurar coaches y niveles de experiencia

---

## 🏗️ Arquitectura del Sistema

### Componentes y cómo se relacionan

```
                      ┌──────────────────────────────────┐
                      │  Cliente (App .NET MAUI)         │
                      │  O Administrador (Vaadin)        │
                      └──────────────┬────────────────────┘
                                     │ HTTPS/JWT
                                     ▼
                      ┌──────────────────────────────────┐
                      │  API GATEWAY (Puerto 8080)       │
                      │  • Valida JWT tokens             │
                      │  • Enruta peticiones              │
                      │  • WebFlux (reactivo)             │
                      └──────────────┬────────────────────┘
                                     │
                   ┌─────────────────┼─────────────────┐
                   │                 │                 │
                   ▼                 ▼                 ▼
       ┌────────────────────┐  ┌─────────────┐  ┌──────────────────┐
       │  IFIT (8081)       │  │ RONNIE(8082)│  │ ADMIN PANEL(8090)│
       │  • Usuarios        │  │ • IA        │  │ • Gestión        │
       │  • Cuestionarios   │  │ • Chat      │  │ • Panel web      │
       │  • Rutinas         │  │ • Prompts   │  │ • Vaadin         │
       └──────────┬─────────┘  └──────┬──────┘  └──────────────────┘
                  │                   │
       ┌──────────┴───────────────────┴──────────────┐
       │  Consultan a Eureka (8761) para descubrirse │
       │  Registran su nombre lógico (IFIT, RONNIE) │
       └──────────────────────────────────────────────┘

                            ↕ Persistencia

       ┌──────────────────┐         ┌──────────────────┐
       │  MySQL IFIT      │         │  MySQL RONNIE    │
       │  • users         │         │  • message       │
       │  • questionnaire │         │    (chat history)│
       │  • routine       │         │                  │
       │  • exercise      │         │                  │
       └──────────────────┘         └──────────────────┘

       ┌──────────────────────────────────────────────┐
       │  KEYCLOAK (8761) — Autenticación            │
       │  • OAuth2 / OpenID Connect                   │
       │  • Emite JWT tokens firmados                 │
       │  • Realm: ifit-realm                         │
       └──────────────────────────────────────────────┘
```

### Flujo completo de una petición (paso a paso)

```
1. USUARIO SE AUTENTICA
   Cliente → Keycloak: POST /auth/login {email, password}
   Keycloak → Cliente: JWT token firmado con RSA

2. USUARIO COMPLETA EL CUESTIONARIO
   Cliente → Gateway: POST /ifit/api/v1/questionnaires/{userId}/start/{qId}
   Gateway valida JWT → reenvía a IFIT (con el token)
   IFIT guarda sesión → retorna primera pregunta

   (Cliente responde cada pregunta...)
   Cliente → Gateway: POST /ifit/api/v1/questionnaires/responses/{id}/answer
   Gateway → IFIT → valida y retorna siguiente pregunta

   (Ciclo hasta que no hay más preguntas)

3. GENERACIÓN DE RUTINA CON IA
   Cliente → Gateway: POST /ifit/api/v1/routines/generate
                      {userId, responseId, coachType}
   
   Gateway → IFIT: extrae respuestas del cuestionario
   IFIT → Ronnie: POST /ronnie/generate-routine 
                  {prompt con perfil + respuestas + catálogo de ejercicios}
   
   Ronnie (LangChain4j) → Groq API: procesa con LLM
   Groq → Ronnie: JSON estructurado de rutina
   Ronnie valida la rutina: si la mayoría de días vienen SIN ejercicios
                            (días sin > días con), regenera UNA vez
   
   Ronnie → IFIT: retorna rutina en JSON
   IFIT reconcilia nombres → retorna al Cliente

4. CHAT CON ENTRENADOR
   Cliente → Gateway: POST /ifit/api/v1/ronnie/chat
                      {memoryId, message, userId}
   
   Gateway → Ronnie: Ronnie carga historial de MySQL
   Ronnie (LangChain4j) → Groq: procesa con LLM + RAG
   Groq → Ronnie: respuesta del coach
   
   Ronnie persiste en MySQL → retorna al Cliente
```

---

## 🗂️ Estructura del Proyecto

```
ifit/
├── docker-compose.yml          # Infraestructura DOCKERIZADA (MySQL + Keycloak únicamente)
├── init-db.sql                 # Script de creación de esquemas (ejecutado automáticamente)
├── ifit-realm-export.json      # Configuración del realm de Keycloak (importar manualmente)
│
├── api-gateway/                # Punto de entrada único (Puerto 8080) — Ejecutar con ./mvnw
│   ├── README.md               → Detalles sobre WebFlux, OAuth2, rutas
│   └── src/main/resources/application.yaml
│
├── eureka-server/              # Registro de servicios (Puerto 8761) — Ejecutar con ./mvnw
│   ├── README.md               → Detalles sobre descubrimiento
│   └── src/main/resources/application.yml
│
├── ifit/                       # Lógica de negocio principal (Puerto 8081) — Ejecutar con ./mvnw
│   ├── README.md               → Módulos: Auth, Questionnaire, Training, etc.
│   ├── src/main/resources/application.properties
│   └── src/main/resources/data.sql  # Datos iniciales (importados automáticamente)
│
├── ronnie/                     # Motor de IA (Puerto 8082) — Ejecutar con ./mvnw
│   ├── README.md               → LangChain4j, prompts, coaches, memoria
│   ├── src/main/resources/application.properties
│   └── src/main/resources/data.sql  # Tipos de mensajes (importados automáticamente)
│
└── admin-panel/                # Panel administrativo (Puerto 8090) — Ejecutar con ./mvnw
    ├── README.md               → Vaadin, seguridad, vistas
    └── src/main/resources/application.properties
```

**Nota sobre Docker:**
- ✅ **Dockerizados**: MySQL (contenedor `ifit-mysql`) y Keycloak (contenedor `keycloak-ifit`)
- ❌ **NO dockerizados**: Todos los microservicios (Eureka, Gateway, IFIT, Ronnie, Admin Panel)
  - Se ejecutan directamente con `./mvnw spring-boot:run` en tu máquina
  - Esto facilita el desarrollo, debugging y hot-reload

---

## 🔑 Conceptos Clave

Estos términos aparecen frecuentemente en la documentación. Aquí está su explicación simple:

### Autenticación y Seguridad

**Keycloak** — Servidor de identidades (¿Quién eres?)
- Almacena usuarios y contraseñas de forma segura
- Emite tokens JWT (credenciales digitales)
- Implementa OAuth2 / OpenID Connect (estándares internacionales)
- El gateway y los microservicios **validan** estos tokens, no los crean

**JWT (JSON Web Token)** — Credencial digital
- Token firmado criptográficamente con RSA
- Contiene información del usuario (id, roles, email)
- Se envía en cada petición: `Authorization: Bearer {token}`
- No se puede falsificar sin la clave privada de Keycloak

**Doble validación** — El token se valida en dos lugares:
1. En el Gateway (frontera de seguridad)
2. En cada microservicio (defensa en profundidad)

### Microservicios y Comunicación

**Eureka** — Guía telefónica de servicios
- Cada microservicio al arrancar se registra: "Soy IFIT, estoy en puerto 8081"
- El Gateway consulta Eureka: "¿Dónde está IFIT?" → "En 8081"
- Permite que servicios se muevan sin cambiar configuración

**WebFlux** — Modelo reactivo (no bloqueante)
- El Gateway usa WebFlux en lugar de Servlet (más eficiente)
- Puede manejar miles de conexiones con pocos hilos
- Basado en Project Reactor y Netty

**REST / HTTP** — Protocolo de comunicación entre servicios
- IFIT llama a Ronnie mediante HTTP POST
- Ronnie llama a Groq API mediante HTTPS

### IA y Procesamiento

**LangChain4j** — Framework para aplicaciones con IA
- Simplifica la integración con LLMs (modelos de lenguaje)
- Implementado en Ronnie
- Gestiona prompts, memoria, RAG (búsqueda contextual)

**LLM (Large Language Model)** — Inteligencia artificial
- Groq API: modelos `llama-3.3-70b` (chat) y `openai/gpt-oss-120b` (rutinas)
- Procesa el prompt (instrucción) y devuelve respuestas

**RAG (Retrieval-Augmented Generation)** — IA contextualizada
- Combina un LLM con una base de datos de conocimiento
- Ronnie usa RAG en chat para dar respuestas específicas sobre fitness
- No usa RAG en rutinas (menos necesario, más control)

**Prompt** — Instrucción para la IA
- Ejemplo: "Eres Ronnie, especialista en musculación. Genera una rutina para..."
- El prompt incluye: perfil del usuario, respuestas del cuestionario, catálogo de ejercicios
- Temperatura (0.3 = determinístico, 0.5 = variado): controla creatividad

### Datos y Persistencia

**Soft delete** — No eliminar, marcar como "desactivo"
- `is_enabled = false` en lugar de `DELETE`
- Preserva el historial

**Transaccionalidad** — Atomicidad: todo o nada
- Si algo falla, se deshace todo (rollback)
- Ejemplo: si insertar usuario en MySQL falla, se revierte su creación en Keycloak

### Interfaz de Usuario

**Vaadin** — Framework para interfaces web en Java
- Panel administrativo sin HTML/JS manual
- Todo se escribe en Java
- Tema `ifit-admin` en variante oscura

---

## 🔄 Cómo Interactúan los Módulos

### Ciclo completo: Usuario nuevo

```
Usuario descarga App MAUI
  ↓
MÓDULO: api-gateway
  ├─ Valida que sea ruta pública (/auth/register)
  ├─ Permite sin JWT
  └─ Reenvia a IFIT

MÓDULO: ifit (AUTH)
  ├─ POST /auth/register recibe: {name, email, password}
  ├─ Llama a Keycloak: "Crea usuario"
  ├─ Keycloak devuelve OK
  ├─ IFIT inserta en su BD
  ├─ Envía email de verificación (código 6 dígitos)
  └─ Retorna: {userId, email}

Usuario recibe email y verifica
  ↓
MÓDULO: api-gateway + ifit (AUTH)
  ├─ POST /auth/verify {email, code}
  ├─ IFIT verifica código en BD
  ├─ Llama a Keycloak: "Emite token"
  ├─ Keycloak devuelve JWT firmado
  └─ Retorna: {accessToken, refreshToken}

Usuario contesta cuestionario
  ↓
MÓDULO: api-gateway + ifit (QUESTIONNAIRE)
  ├─ GET /questionnaires/coach/{id}/experience-level/{id}
  ├─ IFIT busca cuestionario por coach + nivel
  ├─ POST /questionnaires/{userId}/start/{qId}
  ├─ IFIT crea sesión de cuestionario
  └─ Retorna: pregunta 1 + opciones

  (Usuario responde cada pregunta...)
  ├─ POST /questionnaires/responses/{id}/answer
  ├─ IFIT guarda respuesta
  ├─ Retorna: siguiente pregunta (o fin si no hay más)

Usuario genera rutina
  ↓
MÓDULO: ifit (TRAINING) → ronnie (ROUTINE GENERATION)
  ├─ POST /routines/generate {userId, responseId, coachType}
  ├─ IFIT obtiene resumen del cuestionario
  ├─ IFIT construye prompt: perfil + respuestas + catálogo de ejercicios
  ├─ IFIT llama a Ronnie: POST /ronnie/generate-routine
  ├─ Ronnie (LangChain4j) → Groq LLM
  ├─ Groq procesa y retorna JSON estructurado
  ├─ Ronnie retorna a IFIT
  ├─ IFIT reconcilia nombres de ejercicios con catálogo
  └─ Retorna: rutina preview (sin guardar aún)

Usuario guarda rutina (opcional)
  ↓
MÓDULO: ifit (TRAINING)
  ├─ POST /routines {userId, description, days[]}
  ├─ IFIT guarda Routine + RoutineDay + RoutineExercise en BD
  └─ Retorna: rutina guardada con ID

Usuario chatea con coach
  ↓
MÓDULO: api-gateway + ronnie (CHAT)
  ├─ POST /ifit/api/v1/ronnie/chat {memoryId, message}
  ├─ Ronnie carga últimas 10 mensajes de MySQL (ventana deslizante)
  ├─ Ronnie (LangChain4j) + RAG → Groq LLM
  ├─ Groq devuelve respuesta
  ├─ Ronnie persiste en MySQL
  └─ Retorna: respuesta del coach
```

### Acceso administrativo

```
Admin abre panel Vaadin
  ↓
MÓDULO: admin-panel (LoginView)
  ├─ Admin ingresa credenciales
  ├─ Calls AuthService
  └─ RestClient hace POST al Gateway: /ifit/api/v1/auth/login

MÓDULO: api-gateway + ifit (AUTH)
  ├─ Valida contra Keycloak
  ├─ Verifica que admin tenga rol 'admin_client_role'
  └─ Retorna: JWT token

Admin gestiona cuestionarios
  ↓
MÓDULO: admin-panel (QuestionnairesView)
  ├─ GET /ifit/api/v1/questionnaires
  ├─ RestClient (con JWT en header) → Gateway
  ├─ Gateway valida JWT → reenvía a IFIT
  ├─ IFIT obtiene de BD → retorna lista
  └─ Vaadin renderiza tabla

Admin crea nuevo cuestionario
  ├─ POST /ifit/api/v1/questionnaires
  ├─ IFIT inserta en BD
  ├─ Retorna: cuestionario creado
  └─ Vaadin muestra confirmación
```

---

## 🛠️ Tecnologías Utilizadas

| Componente | Tecnología | Por qué |
|---|---|---|
| **Lenguaje** | Java 21 | Type-safe, productivo, buen ecosistema |
| **Framework Base** | Spring Boot 3.5.7 | Estándar de la industria, excelente documentación |
| **Orquestación en Contenedores** | Docker / Docker Compose | Reproducibilidad, facilita desarrollo y deploy |
| **Autenticación** | Keycloak 23.0 | OAuth2/OpenID estándar, gestión centralizada de usuarios |
| **Persistencia** | MySQL 8.0 + JPA/Hibernate | Relacional, ACID, confiable |
| **Punto de Entrada** | Spring Cloud Gateway | WebFlux reactivo, eficiente, único acceso |
| **Descubrimiento** | Spring Cloud Eureka | Service mesh descentralizado, registro dinámico |
| **IA** | LangChain4j + Groq | Integración sencilla con LLMs, prompts versionables |
| **Interfaz Admin** | Vaadin Flow 24.8 | Todo en Java, sin HTML/JS manual |

---

## ⚙️ Guía Completa de Configuración desde Cero

Esta es una guía **MEGA detallada** para arrancar iFit desde cero, incluyendo la configuración de Keycloak, bases de datos y todos los microservicios. Si algún día tienes que tirar de aquí y no recuerdas cómo hacerlo, sigue exactamente estos pasos.

### Requisitos previos

- **Docker Desktop** (para MySQL y Keycloak únicamente)
- **Java Development Kit (JDK) 21** o superior
- **Maven 3.9+** (o usa los Maven Wrappers: `./mvnw`)
- **Variable de entorno `GROQ_API_KEY`** (obtén en https://console.groq.com)

### PASO 1: Levantar la Infraestructura (MySQL + Keycloak)

```bash
# En la raíz del proyecto
docker-compose up -d
```

Esto levanta **dos contenedores ÚNICAMENTE**:
- **MySQL**: `localhost:3306` con usuario `root` / contraseña `root`
- **Keycloak**: `http://localhost:9090` con usuario `admin` / contraseña `admin`

**Verifica que estén corriendo:**
```bash
docker ps
# Deberías ver dos contenedores: mysql y keycloak
```

**Espera a que ambos estén listos** (~30-45 segundos). Si ves errores de conexión en los siguientes pasos, espera más.

### PASO 2: Importar el Realm de Keycloak y Obtener Credenciales

**A. Accede a Keycloak:**
1. Abre tu navegador: `http://localhost:9090`
2. Haz clic en "Administration Console"
3. Ingresa: usuario `admin`, contraseña `admin`

**B. Importa el realm `ifit-realm`:**
1. En la esquina superior izquierda, verás "Master" (realm actual)
2. Haz clic → "Create realm" (o busca opción de importar)
3. Alterna a "Import" si está disponible, o:
   - Ve a la sección "Realms" (izquierda)
   - Haz clic en el icono ⚙️ (importar)
   - Selecciona el archivo `ifit-realm-export.json` del repositorio
   - Haz clic en "Create"

**C. Verifica que se creó correctamente:**
- En la esquina superior izquierda debe aparecerte "ifit-realm"
- Ve a "Clients" (izquierda) y verás `springboot-ifit-client`

**D. Obtén las credenciales de Keycloak para los microservicios:**

Vamos a extraer el `client-secret` que necesitan IFIT y Ronnie:

1. **En Keycloak**, ve a: **Realm: ifit-realm** → **Clients** → `springboot-ifit-client`
2. Abre la pestaña **Credentials**
3. Copia el valor en **Client Secret** (es un string largo)
   - Este es tu `KEYCLOAK_CLIENT_SECRET`

4. Ten a mano estos valores:
   - `KEYCLOAK_CLIENT_ID`: `springboot-ifit-client`
   - `KEYCLOAK_CLIENT_SECRET`: (lo que copiaste arriba)
   - `KEYCLOAK_REALM`: `ifit-realm`
   - `KEYCLOAK_SERVER_URL`: `http://localhost:9090`

### PASO 3: Verificar que MySQL Creó las Bases de Datos

**A. Conéctate a MySQL:**
```bash
mysql -h localhost -u root -p
# Contraseña: root
```

**B. Verifica que existen las bases de datos:**
```sql
SHOW DATABASES;
-- Deberías ver: ifit, ronnie, y las demás por defecto (mysql, information_schema, etc.)
```

**C. Verifica las tablas (opcional ahora, se crearán al arrancar los servicios):**
```sql
USE ifit;
SHOW TABLES;
-- Estará vacío por ahora, las tablas se crean automáticamente con Hibernate
```

**D. Sal de MySQL:**
```sql
EXIT;
```

### PASO 4: Configurar Variables de Entorno

**A. Obtén tu API Key de Groq:**
1. Ve a https://console.groq.com
2. Inicia sesión (crea cuenta si no tienes)
3. Ve a "API Keys"
4. Copia una key activa (empieza con `gsk_`)

**B. Configura la variable de entorno:**

**En Linux/Mac:**
```bash
export GROQ_API_KEY="gsk_Tu_API_Key_Aqui"
```

**En Windows (PowerShell):**
```powershell
$env:GROQ_API_KEY = "gsk_Tu_API_Key_Aqui"
```

**En Windows (Command Prompt):**
```cmd
set GROQ_API_KEY=gsk_Tu_API_Key_Aqui
```

**Verifica que se configuró:**
```bash
# Linux/Mac
echo $GROQ_API_KEY

# Windows PowerShell
echo $env:GROQ_API_KEY

# Windows CMD
echo %GROQ_API_KEY%
```

### PASO 5: Arrancar los Microservicios (EN ORDEN)

**Abre 5 terminales independientes** y ejecuta estos comandos EN ESTE ORDEN EXACTO:

**Terminal 1 - EUREKA SERVER (espera 5 segundos antes de pasar a la siguiente):**
```bash
cd eureka-server
./mvnw spring-boot:run
# Deberías ver: "Started EurekaServerApplication"
```

**Terminal 2 - API GATEWAY (espera 5 segundos):**
```bash
cd api-gateway
./mvnw spring-boot:run
# Deberías ver: "Started ApiGatewayServiceApplication"
```

**Terminal 3 - IFIT (espera 5 segundos):**
```bash
cd ifit
./mvnw spring-boot:run
# Deberías ver: "Started IfitApplication"
# Verás líneas sobre Hibernate creando tablas: "Creating tables for key binding"
```

**Terminal 4 - RONNIE (requiere la variable GROQ_API_KEY):**
```bash
cd ronnie
./mvnw spring-boot:run
# Deberías ver: "Started RonnieApplication"
# Verá tablas siendo creadas también
```

**Terminal 5 - ADMIN PANEL (opcional):**
```bash
cd admin-panel
./mvnw spring-boot:run
# Deberías ver: "Started AdminPanelApplication"
# Se abrirá automáticamente un navegador en http://localhost:8090
```

### PASO 6: Verificar que Todo Está Corriendo

**A. Eureka Dashboard (para ver servicios registrados):**
- Abre: http://localhost:8761
- Deberías ver:
  - `APIGATEWAYSERVICE` en estado UP
  - `IFIT` en estado UP
  - `RONNIE` en estado UP

**B. Swagger de IFIT (documentación de API):**
- Abre: http://localhost:8081/swagger-ui.html
- Deberías ver todos los endpoints documentados

**C. Swagger de Ronnie (documentación de IA):**
- Abre: http://localhost:8082/swagger-ui.html

**D. Admin Panel (Vaadin):**
- Si ejecutaste Terminal 5, se abrió automáticamente en: http://localhost:8090
- Si no, abre manualmente

### PASO 7: Cargar Datos Iniciales (Cuestionarios, Coaches, Ejercicios)

**Los datos se cargan automáticamente PERO solo si es la primera vez:**

Cuando IFIT arranca, ejecuta automáticamente `src/main/resources/data.sql`, que inserta:
- Niveles de experiencia (Principiante, Intermedio, Avanzado)
- Coaches (Ronnie, Serena, Kael, Eliud)
- Preguntas del cuestionario (árbol de decisión completo)
- Opciones de respuesta
- Cuestionarios predefinidos

**Para Ronnie:**
Cuando Ronnie arranca, ejecuta automáticamente `src/main/resources/data.sql`, que inserta:
- Tipos de mensajes (user, ai, system)

**Si necesitas resetear los datos:**
```sql
-- Conéctate a MySQL
mysql -h localhost -u root -p

USE ifit;
-- Borra datos pero mantiene tablas
DELETE FROM user_answer;
DELETE FROM questionnaire_response;
DELETE FROM questionnaire;
DELETE FROM question_option;
DELETE FROM question;
-- etc. (cuidado, también borra usuarios)

-- Luego reinicia IFIT y se recargará data.sql
```

### PASO 8: Crear un Usuario de Prueba

**A. Usa el endpoint de registro:**
```bash
curl -X POST http://localhost:8080/ifit/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Juan García",
    "email": "test@example.com",
    "password": "Password123!"
  }'
```

**Respuesta esperada:**
```json
{
  "userId": 1,
  "email": "test@example.com",
  "message": "User registered successfully. Check your email for verification code."
}
```

**B. Recibirás un código de verificación en la consola (en desarrollo, los emails no se envían):**
- Revisa los logs de IFIT, busca un código como `123456`

**C. Verifica el email:**
```bash
curl -X POST http://localhost:8080/ifit/api/v1/auth/verify \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "verificationCode": "123456"
  }'
```

**Respuesta esperada:**
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "keycloakUserId": "uuid-aqui"
}
```

Guarda el `accessToken`, lo usarás en peticiones futuras.

### PASO 9: Testear el Flujo Completo

**A. Obtener lista de cuestionarios:**
```bash
curl -X GET http://localhost:8080/ifit/api/v1/questionnaires \
  -H "Authorization: Bearer TU_ACCESS_TOKEN"
```

**B. Iniciar un cuestionario:**
```bash
curl -X POST http://localhost:8080/ifit/api/v1/questionnaires/1/start/1 \
  -H "Authorization: Bearer TU_ACCESS_TOKEN"
```

**C. Responder una pregunta:**
```bash
curl -X POST http://localhost:8080/ifit/api/v1/questionnaires/responses/1/answer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TU_ACCESS_TOKEN" \
  -d '{
    "questionId": 1,
    "selectedOptionId": 1,
    "additionalText": null
  }'
```

### PASO 10: Troubleshooting

| Problema | Causa | Solución |
|---|---|---|
| `Connection refused: localhost:3306` | MySQL no está corriendo | `docker-compose ps` y `docker-compose up -d` |
| `Connection refused: localhost:9090` | Keycloak no está listo | Espera 30-45 seg más y recarga |
| `unauthorized_client` en login | El realm no importó bien | Reimporta `ifit-realm-export.json` en Keycloak |
| IFIT no ve a Ronnie | Eureka no se registró | Verifica que Eureka está en `localhost:8761` |
| `401 Unauthorized` en peticiones | Token inválido o expirado | Obtén uno nuevo con `/auth/login` |
| Ronnie lanza error `GROQ_API_KEY not found` | Variable de entorno no configurada | Configura `GROQ_API_KEY` antes de `./mvnw spring-boot:run` |
| Admin Panel no carga | Gateway no está activo | Verifica que API Gateway está en `localhost:8080` |

---

## 📋 Checklist de Arranque Rápido

Si necesitas arrancar rápido sin leer todo:

- [ ] 1. `docker-compose up -d` (MySQL + Keycloak)
- [ ] 2. Accede a `http://localhost:9090` → importa `ifit-realm-export.json`
- [ ] 3. En Keycloak, obtén `Client Secret` de `springboot-ifit-client`
- [ ] 4. Configura `export GROQ_API_KEY="gsk_..."`
- [ ] 5. Terminal 1: `cd eureka-server && ./mvnw spring-boot:run`
- [ ] 6. Terminal 2: `cd api-gateway && ./mvnw spring-boot:run`
- [ ] 7. Terminal 3: `cd ifit && ./mvnw spring-boot:run`
- [ ] 8. Terminal 4: `cd ronnie && ./mvnw spring-boot:run`
- [ ] 9. Terminal 5: `cd admin-panel && ./mvnw spring-boot:run` (opcional)
- [ ] 10. Verifica en `http://localhost:8761` que todos están en UP
- [ ] 11. Listo para usarlos


---

## 📖 Documentación de Cada Módulo

Cada módulo tiene su propio README con detalles técnicos profundos:

### 🧠 [ifit/README.md](ifit/README.md) — Lógica de Negocio

Cubre los módulos internos:
- **Auth** — Integración con Keycloak, flujos de registro, login, token refresh
- **Questionnaire** — Árbol de decisión adaptativo, sesiones de usuario, validaciones
- **Training** — Generación de rutinas, orquestación hacia Ronnie, persistencia
- **User** — Gestión de usuarios, niveles de experiencia
- **Coach** — Tipos de coaches (Ronnie, Serena, Kael, Eliud)
- **Exercises** — Catálogo de ejercicios
- **Notification** — Envío de emails

### 🤖 [ronnie/README.md](ronnie/README.md) — Motor de IA

Cubre:
- Configuración dual de modelos LLM (Llama 70B para chat, GPT-OSS 120B para rutinas)
- LangChain4j y estructuras de prompts
- Memoria conversacional persistente en MySQL
- Los 4 coaches especializados (Ronnie, Serena, Kael, Eliud)
- Endpoints de generación de rutinas y chat

### 🔀 [api-gateway/README.md](api-gateway/README.md) — Punto de Entrada

Cubre:
- WebFlux y modelo reactivo
- OAuth2 Resource Server
- Validación de JWT contra Keycloak
- StripPrefix y TokenRelay
- Rutas configuradas (IFIT-PUBLIC, RONNIE, IFIT-PRIVATE)
- Doble validación de tokens

> Incluye un apéndice completo: [ANEXO_CONCEPTOS_OAUTH2_WEBFLUX.md](api-gateway/ANEXO_CONCEPTOS_OAUTH2_WEBFLUX.md)

### 📍 [eureka-server/README.md](eureka-server/README.md) — Descubrimiento

Cubre:
- Service discovery descentralizado
- Registro dinámico de servicios
- Dashboard web
- Configuración del cliente en cada servicio

### 🎨 [admin-panel/README.md](admin-panel/README.md) — Interfaz Administrativa

Cubre:
- Vaadin Flow y componentes UI
- Vistas (usuarios, ejercicios, coaches, cuestionarios, rutinas)
- Integración con Gateway mediante RestClient
- Seguridad y sesión

---

## 📖 Documentación de las APIs (OpenAPI / Swagger)

Los microservicios **iFit** y **Ronnie** exponen su documentación interactiva con
Swagger UI (springdoc-openapi). Desde ahí puedes ver y probar todos los endpoints.

> Sustituye `localhost` por la IP o dominio del servidor si accedes en remoto
> (p. ej. `http://TU_IP:8081/swagger-ui.html`). En el Admin Panel tienes un
> módulo **Documentación** que genera estos enlaces automáticamente con el host
> correcto.

| Servicio | Swagger UI | OpenAPI JSON |
|----------|------------|--------------|
| **iFit** (lógica de negocio) | http://localhost:8081/swagger-ui.html | http://localhost:8081/v3/api-docs |
| **Ronnie** (motor de IA) | http://localhost:8082/swagger-ui.html | http://localhost:8082/v3/api-docs |

Otras consolas útiles (no son OpenAPI, pero ayudan a operar el sistema):

| Consola | URL | Para qué |
|---------|-----|----------|
| **Eureka** | http://localhost:8761 | Ver microservicios registrados |
| **Keycloak Admin** | http://localhost:9090 | Gestionar realms, clientes y usuarios |
| **Admin Panel** | http://localhost:8090 | Interfaz de administración (Vaadin) |

> El **API Gateway** (8080) y el **Admin Panel** (8090) no exponen OpenAPI propio:
> el Gateway solo enruta y el Admin Panel es una UI Vaadin que consume el Gateway.

---

## 🚀 Decisiones Arquitectónicas Clave

### Por qué 5 microservicios y no uno monolito

- **Escalabilidad**: Ronnie (IA) puede escalarse independientemente
- **Resiliencia**: Si Ronnie falla, IFIT sigue funcionando
- **Equipos**: Diferentes equipos pueden trabajar en Ronnie, IFIT y admin-panel sin conflictos
- **Despliegue**: Actualizar admin-panel no requiere redeploy de IFIT

### Por qué WebFlux en el Gateway y Servlet en los demás

- **Gateway**: Miles de conexiones concurrentes esperando respuestas de upstream → WebFlux reactivo es más eficiente
- **IFIT/Ronnie**: Lógica sincrónica de negocio → Servlet (Spring Boot Web) es más sencillo de razonar

### Por qué Keycloak centralizado

- **Única fuente de verdad** para identidades
- **Estándar OAuth2** — interoperable con otros sistemas
- **No reinventar la rueda** — seguridad auditada por expertos

### Por qué dos modelos LLM (Llama + GPT-OSS)

- **Chat**: Llama 70B es más rápido y conversacional (temperatura 0.5)
- **Rutinas**: GPT-OSS 120B es más determinístico y respeta el JSON (temperatura 0.3)
- **Coste**: Menos dinero que usar GPT-4 para todo

### Por qué RAG solo en chat

- **Chat**: Necesita contexto sobre fitness → RAG ayuda
- **Rutinas**: Puede ser más determinístico → RAG añade complejidad innecesaria

---

## 📊 Flujo de Datos

```
Usuario Final (App MAUI)
  │
  ├─→ Autentica → Keycloak emite JWT
  │
  ├─→ Completa cuestionario (65 preguntas)
  │   IFIT.BD ← respuestas guardadas
  │
  ├─→ Genera rutina
  │   IFIT obtiene perfil
  │   IFIT construye prompt
  │   IFIT → Ronnie → Groq LLM → JSON rutina
  │   IFIT.BD ← rutina guardada
  │
  └─→ Chatea con coach
      Ronnie carga historial de BD
      Ronnie → Groq LLM (+ RAG contextual)
      Ronnie.BD ← nuevo mensaje guardado

Administrador (Panel Vaadin)
  │
  ├─→ Autentica → Keycloak valida rol admin
  │
  ├─→ Gestiona usuarios
  │   IFIT.BD ← usuarios manipulados
  │
  ├─→ Gestiona cuestionarios
  │   IFIT.BD ← cuestionarios editados
  │
  ├─→ Gestiona ejercicios
  │   IFIT.BD ← catálogo actualizado
  │
  └─→ Gestiona coaches
      IFIT.BD ← coaches activados/desactivados
```

---

## 🔍 Troubleshooting Rápido

| Problema | Causa Probable | Solución |
|---|---|---|
| "401 Unauthorized" al hacer login | Keycloak no está corriendo | `docker-compose ps` — asegura que keycloak esté en `Up` |
| Gateway no ve IFIT/Ronnie | Eureka no está corriendo | Arranca eureka-server primero |
| "Connection refused" desde Gateway a IFIT | Orden de arranque | Espera a que IFIT se registre en Eureka (~10 seg) |
| Rutinas con ejercicios inventados | Modelo LLM incorrecto | Verifica `groq.routine-model-name=openai/gpt-oss-120b` en Ronnie |
| Token vencido | JWT expirado | Usuario debe hacer login de nuevo (obtener nuevo token) |
| Admin panel no carga | Gateway no está corriendo | Arranca: Gateway → IFIT → Admin Panel |

---

---

## 📝 Notas Técnicas sobre Scripts SQL y Carga de Datos

### Estructura de SQL en iFit

El proyecto usa **Hibernate con `ddl-auto=update`**, lo que significa:

1. **Creación de tablas**: Ocurre automáticamente cuando arranca IFIT o Ronnie
   - `CREATE TABLE` solo ejecuta si la tabla no existe
   - Cambios a esquemas existentes se adaptan automáticamente

2. **Carga de datos iniciales**: Ocurre automáticamente después de crear tablas
   - Archivo: `ifit/src/main/resources/data.sql`
   - Se ejecuta UNA SOLA VEZ (usa `ON DUPLICATE KEY UPDATE` para evitar duplicados)
   - Inserta: niveles, coaches, preguntas, opciones, cuestionarios

3. **Para Ronnie**: Similar a IFIT
   - Archivo: `ronnie/src/main/resources/data.sql`
   - Inserta: tipos de mensajes (user, ai, system)

### Archivos SQL Importantes

| Archivo | Propósito | Ejecutado por | Cuándo |
|---|---|---|---|
| `init-db.sql` | Crear bases de datos `ifit` y `ronnie` | Docker (mysql init-script) | Al hacer `docker-compose up` |
| `ifit/src/main/resources/data.sql` | Insertar datos iniciales (coaches, cuestionarios, etc.) | Hibernate/Spring | Al arrancar IFIT la primera vez |
| `ronnie/src/main/resources/data.sql` | Insertar tipos de mensajes | Hibernate/Spring | Al arrancar Ronnie la primera vez |

### Si Necesitas Resetear Datos

**Opción 1: Reseteo suave (mantiene estructura):**
```sql
-- Conéctate a MySQL
mysql -h localhost -u root -p

-- Borra datos pero mantiene tablas
USE ifit;
TRUNCATE TABLE user_answer;
TRUNCATE TABLE questionnaire_response;
TRUNCATE TABLE routine_exercise;
TRUNCATE TABLE routine_day;
TRUNCATE TABLE routine;
TRUNCATE TABLE questionnaire;
TRUNCATE TABLE question_option;
TRUNCATE TABLE question;
-- ... (trunca más si necesitas)

-- Luego reinicia IFIT y se recargará data.sql automáticamente
```

**Opción 2: Reseteo completo (borra todo, incluyendo usuarios):**
```bash
# Detén los contenedores
docker-compose down

# Borra los volúmenes de MySQL (CUIDADO: borra TODO)
docker volume rm ifit_mysql_data  # Ajusta según tu docker-compose.yml

# Levanta nuevamente
docker-compose up -d

# Luego arranca los microservicios, se crearán tablas y datos desde 0
```

### Flujo de Inicialización Cuando Arranca IFIT

1. Spring Boot inicia
2. Hibernate conecta a MySQL
3. `spring.jpa.hibernate.ddl-auto=update` → crea/actualiza tablas
4. `spring.sql.init.mode=always` → ejecuta `data.sql`
5. Todas las inserciones usan `ON DUPLICATE KEY UPDATE`
   - Si la fila YA EXISTE (por PK), la ACTUALIZA
   - Si NO EXISTE, la INSERTA
   - **Resultado**: Es seguro ejecutar varias veces sin duplicados

### Dónde Están los Scripts SQL

```
ifit/
├── src/main/resources/
│   ├── data.sql                         # Datos de IFIT (coaches, cuestionarios, etc.)
│   ├── application.properties           # Config de IFIT
│   └── schema.sql                       # (Opcional, para definiciones personalizadas)

ronnie/
├── src/main/resources/
│   ├── data.sql                         # Datos de Ronnie (tipos de mensaje)
│   └── application.properties           # Config de Ronnie

# En raíz del proyecto
├── init-db.sql                          # Crear BBDD (ejecutado por docker-compose)
└── ifit-realm-export.json               # Configuración de Keycloak (importar manualmente)
```

### Verificar que los Datos Se Cargaron Correctamente

```bash
# Conéctate a MySQL
mysql -h localhost -u root -p
# Contraseña: root

# Verifica IFIT
USE ifit;
SELECT COUNT(*) FROM coachmodeltype;        # Deberías ver: 5 (Ronnie, Serena, Kael, Eliud, Default)
SELECT COUNT(*) FROM experiencelevel;       # Deberías ver: 3 (Principiante, Intermedio, Avanzado)
SELECT COUNT(*) FROM question;              # Deberías ver: 65 (todas las preguntas del árbol)
SELECT COUNT(*) FROM questionnaire;         # Deberías ver: 9 (cuestionarios predefinidos)

# Verifica Ronnie
USE ronnie;
SELECT COUNT(*) FROM message_type;          # Deberías ver: 3 (user, ai, system)
```

---

## 📞 Autor

**Juan García Candón**  
Universidad de Cádiz — Escuela Superior de Ingeniería  
Trabajo Final de Grado (TFG), 2024–2025

**Última actualización**: Junio 2025  
**Versión**: 3.2 (Con guía mega detallada de setup desde cero)
