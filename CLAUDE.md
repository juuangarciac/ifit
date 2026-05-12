# IFit — Plataforma Inteligente de Entrenamiento Personal con IA

IFit es una plataforma de fitness basada en una arquitectura de microservicios cuyo objetivo es ofrecer entrenamientos personalizados mediante Inteligencia Artificial. El proyecto está desarrollado como Trabajo de Fin de Grado (TFG) en la Universidad de Cádiz y combina tecnologías modernas del ecosistema Spring, OAuth2, modelos LLM y arquitecturas distribuidas.

La idea principal del sistema es que un usuario pueda registrarse, responder cuestionarios sobre sus objetivos físicos y recibir rutinas adaptadas automáticamente gracias a modelos de IA especializados según el tipo de entrenamiento.

---

# Arquitectura General

La plataforma está formada por cinco servicios principales:

```text
Cliente → API Gateway (:8080) → Eureka Server
                                ├── IFit (:8081)
                                └── Ronnie (:8082)

Infraestructura:
- MySQL (:3306)
- Keycloak (:9090)
- Ollama (:11434, opcional)
```

## Componentes Principales

| Servicio | Puerto | Función |
|---|---|---|
| API Gateway | 8080 | Punto de entrada del sistema |
| Eureka Server | 8761 | Descubrimiento de microservicios |
| IFit | 8081 | Lógica principal de negocio |
| Ronnie | 8082 | Generación de rutinas mediante IA |
| Keycloak | 9090 | Autenticación y autorización |
| MySQL | 3306 | Persistencia de datos |

---

# Flujo de Peticiones

Todas las solicitudes del cliente pasan primero por el API Gateway.

El Gateway:

- Valida los JWT de OAuth2.
- Permite ciertas rutas públicas sin autenticación:
  - `/ifit/api/v1/auth/**`
  - `/ifit/api/v1/exercise-images/**`
- Reenvía automáticamente el token JWT a los microservicios.
- Utiliza Eureka para localizar instancias activas de los servicios.
- Elimina parte del prefijo de las rutas antes de redirigirlas (`StripPrefix=3`).

Esto permite desacoplar completamente el frontend de la ubicación real de cada microservicio.

---

# Servicio IFit (Core Backend)

El microservicio `ifit` contiene toda la lógica principal de negocio de la aplicación.

## Responsabilidades

- Gestión de usuarios
- Registro e inicio de sesión
- Integración con Keycloak
- Gestión de cuestionarios
- Gestión de rutinas
- Catálogo de ejercicios
- Comunicación con la IA
- Sistema de notificaciones por email

## Estructura Modular

```text
modules/
├── auth/
├── user/
├── questionnaire/
├── training/
├── exercises/
├── ai/
└── notification/
```

---

## Módulo Auth

Gestiona:

- Login
- Registro
- Refresh Token
- Logout

### Funcionamiento

El sistema no autentica usuarios directamente. Toda la autenticación se delega a Keycloak.

`AuthenticationService` se comunica con Keycloak mediante `RestTemplate` utilizando:

```text
/realms/ifit-realm/protocol/openid-connect/token
```

También existe un mecanismo de rollback:

- Si el usuario se crea en Keycloak pero falla el guardado en MySQL,
- El sistema elimina automáticamente el usuario de Keycloak para mantener consistencia.

Esto evita usuarios “huérfanos”.

---

## Módulo User

Gestiona:

- CRUD de usuarios
- Sincronización con Keycloak
- Roles y permisos

El sistema utiliza:

```text
ROLE_USER
ROLE_ADMIN
```

Estos roles se extraen desde el claim:

```text
resource_access.springboot-ifit-client.roles
```

del JWT emitido por Keycloak.

---

## Módulo Questionnaire

Uno de los núcleos inteligentes del proyecto.

Permite crear cuestionarios dinámicos sobre:

- Objetivos físicos
- Nivel de experiencia
- Frecuencia de entrenamiento
- Lesiones
- Preferencias deportivas
- Disponibilidad semanal
- Tipo de entrenamiento deseado

Las respuestas se almacenan y posteriormente se utilizan para construir prompts personalizados enviados al modelo de IA.

Aquí es donde IFit empieza realmente a diferenciarse de una app fitness típica.

---

## Módulo Training

Gestiona:

- Creación de rutinas
- Consulta de rutinas
- Historial de entrenamiento
- Comunicación con Ronnie

La clase principal:

```text
IFitAIClient
```

envía peticiones al servicio de IA:

```text
POST /{coachName}/generate-routine
```

Dependiendo del tipo de entrenamiento, el sistema selecciona un coach especializado.

---

## Módulo Exercises

Contiene el catálogo de ejercicios.

La entidad principal:

```text
ExerciseCatalog
```

incluye:

- Nombre del ejercicio
- Grupo muscular
- Instrucciones
- Imágenes
- Equipamiento
- Nivel de dificultad

Algunos campos se almacenan como listas JSON mediante:

```text
StringListConverter
```

---

## Módulo Notification

Sistema de emails utilizando:

- Spring Mail
- Thymeleaf

Se usa para:

- Verificación
- Confirmaciones
- Recuperación de cuenta
- Notificaciones futuras

---

# Servicio Ronnie — Inteligencia Artificial

Ronnie es el cerebro de generación de rutinas.

Este microservicio utiliza:

- LangChain4j
- Modelos LLM
- Ingeniería de prompts
- Memoria conversacional

## Arquitectura Interna

```text
modules/
├── coach/
│   ├── master/
│   ├── ronnie/
│   ├── eliud/
│   ├── serena/
│   ├── kael/
│   └── dto/
└── message/
```

---

# Coaches Especializados

Cada “coach” representa un experto virtual distinto.

| Coach | Especialidad |
|---|---|
| Ronnie | Hipertrofia |
| Eliud | Running/Cardio |
| Serena | Fitness femenino |
| Kael | Calistenia |

Esto permite adaptar:

- el tono,
- las recomendaciones,
- la estructura de las rutinas,
- y las prioridades del entrenamiento.

No es simplemente cambiar nombres. Cada coach tiene un contexto de sistema distinto.

---

# Funcionamiento de la IA

## Flujo completo

### 1. El usuario responde cuestionarios

Ejemplo:

- “Quiero ganar masa muscular”
- “Entreno 4 días”
- “Soy intermedio”

---

### 2. IFit construye el prompt

El backend transforma las respuestas en un prompt estructurado.

---

### 3. IFit llama a Ronnie

```text
IFitAIClient.generateRoutine(...)
```

---

### 4. Ronnie inyecta contexto

El sistema añade:

- personalidad del coach,
- catálogo de ejercicios,
- restricciones,
- objetivos,
- historial conversacional.

---

### 5. LangChain4j llama al LLM

El modelo utilizado por defecto es:

```text
llama-3.3-70b-versatile
```

a través de Groq API.

Opcionalmente puede usarse Ollama localmente.

---

### 6. El LLM devuelve JSON estructurado

La salida no es texto libre.

El modelo debe generar:

```text
RoutineResponseDTO
```

con:

- días,
- ejercicios,
- series,
- repeticiones,
- descansos,
- recomendaciones.

Esto es muy importante porque obliga al modelo a producir respuestas consistentes y parseables.

---

# Memoria Conversacional

Ronnie implementa memoria contextual mediante:

```text
messageWindowChatMemory
```

Esto permite:

- continuidad entre conversaciones,
- recordar contexto previo,
- mejorar recomendaciones progresivamente.

Por ejemplo:

> “La semana pasada tuve dolor de hombro”

El modelo puede tener eso en cuenta en futuras rutinas.

---

# Seguridad

La seguridad está basada en OAuth2 + JWT.

## Keycloak

Actúa como:

- servidor de autenticación,
- gestión de usuarios,
- gestión de roles,
- emisión de tokens.

---

## API Gateway

Valida JWTs en el borde del sistema.

---

## IFit

Vuelve a validar tokens internamente para aplicar autorización por roles.

Esto añade una segunda capa de seguridad.

---

# Tecnologías Utilizadas

## Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Cloud Gateway
- Spring Cloud Netflix Eureka
- Spring Data JPA
- Hibernate
- Maven

---

## Inteligencia Artificial

- LangChain4j
- Groq API
- Llama 3.3 70B
- Ollama

---

## Infraestructura

- Docker
- Docker Compose
- MySQL
- Keycloak

---

# Diseño Arquitectónico

El proyecto sigue principios modernos:

## Microservicios

Cada servicio tiene responsabilidades independientes.

Ventajas:

- escalabilidad,
- mantenibilidad,
- despliegue independiente,
- tolerancia a fallos.

---

## Arquitectura Modular

Dentro de cada microservicio existe separación clara por dominio.

Esto facilita:

- testing,
- mantenimiento,
- evolución futura.

---

## DTOs con Records

El proyecto utiliza Java Records:

```java
record LoginResponseDto(...)
```

Ventajas:

- menos boilerplate,
- inmutabilidad,
- código más limpio.

---

# Posibles Futuras Mejoras

IFit tiene muchísimo potencial de evolución.

## Algunas ideas potentes:

### Seguimiento inteligente del progreso

La IA podría reajustar rutinas automáticamente según:

- pesos levantados,
- fatiga,
- adherencia,
- rendimiento.

---

### Integración con wearables

- Apple Watch
- Garmin
- Fitbit
- Polar

---

### Nutrición inteligente

Generación automática de:

- dietas,
- macros,
- recomendaciones nutricionales.

---

### IA multimodal

Analizar:

- vídeos,
- postura,
- técnica de ejercicios.

---

### Sistema social

- rankings,
- retos,
- comunidad fitness,
- entrenamientos compartidos.

---

# Valor Técnico del Proyecto

Este proyecto no es una CRUD típica.

Combina:

- arquitectura distribuida,
- seguridad enterprise,
- IA generativa,
- memoria conversacional,
- integración OAuth2,
- ingeniería de prompts,
- diseño modular.

A nivel de TFG, tiene una complejidad bastante seria porque junta backend moderno, cloud patterns y LLMs en un mismo ecosistema.

Y lo más interesante: la arquitectura está preparada para crecer. No parece un proyecto “cerrado”, sino una base real para una plataforma fitness inteligente mucho más grande.