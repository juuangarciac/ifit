# 🏋️ IFit API

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-orange.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> **Sistema integral de fitness personalizado con coaching de IA**

IFit es una API RESTful desarrollada con Spring Boot que proporciona un sistema completo de gestión de fitness con coaching personalizado mediante inteligencia artificial. La plataforma permite a los usuarios registrarse, completar cuestionarios de evaluación, y recibir planes de entrenamiento adaptados a sus necesidades mediante diferentes modelos de IA.

---

## 📋 Tabla de Contenidos

- [Características Principales](#-características-principales)
- [Arquitectura](#-arquitectura)
- [Tecnologías](#-tecnologías)
- [Requisitos Previos](#-requisitos-previos)
- [Instalación](#-instalación)
- [Configuración](#-configuración)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Módulos de la API](#-módulos-de-la-api)
- [Endpoints Principales](#-endpoints-principales)
- [Seguridad](#-seguridad)
- [Documentación de API](#-documentación-de-api)
- [Testing](#-testing)
- [Roadmap](#-roadmap)
- [Contribución](#-contribución)
- [Autor](#-autor)
- [Licencia](#-licencia)

---

## ✨ Características Principales

### 🔐 Gestión de Usuarios
- ✅ Registro y autenticación segura con OAuth2/JWT
- ✅ Integración con Keycloak para gestión de identidades
- ✅ Roles y permisos (USER, ADMIN)
- ✅ Verificación de email
- ✅ Perfil de usuario con preferencias personalizadas

### 🤖 Coaching con IA
- ✅ Múltiples modelos de IA disponibles (GPT-4, Claude, Gemini, etc.)
- ✅ Integración con Ollama para procesamiento local
- ✅ Asignación personalizada de coach según nivel de experiencia
- ✅ Generación de planes de entrenamiento adaptativos

### 📝 Sistema de Cuestionarios
- ✅ Cuestionarios dinámicos de evaluación
- ✅ Flujo condicional basado en respuestas
- ✅ Múltiples tipos de preguntas (selección única, múltiple, texto)
- ✅ Análisis y almacenamiento de respuestas
- ✅ Generación de perfiles de fitness

### 📧 Notificaciones
- ✅ Sistema de emails transaccionales
- ✅ Plantillas HTML personalizables
- ✅ Verificación de email automatizada
- ✅ Notificaciones de eventos importantes

### 📊 Características Técnicas
- ✅ Arquitectura de microservicios
- ✅ API RESTful con versionado
- ✅ Documentación automática con Swagger/OpenAPI
- ✅ Containerización con Docker
- ✅ Gateway API para enrutamiento
- ✅ Validaciones completas con Bean Validation
- ✅ Mapeo DTO-Entity con separación de responsabilidades

---

## 🏗️ Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                       API Gateway                            │
│                    (Enrutamiento & Seguridad)               │
└──────────────────────┬──────────────────────────────────────┘
                       │
        ┌──────────────┴──────────────┐
        │                             │
┌───────▼────────┐           ┌────────▼───────┐
│  Keycloak      │           │   IFit API     │
│  (OAuth2/JWT)  │◄─────────►│  (Spring Boot) │
└────────────────┘           └────────┬───────┘
                                      │
                    ┌─────────────────┼─────────────────┐
                    │                 │                 │
            ┌───────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐
            │   MySQL      │  │   Ollama    │  │   Email     │
            │  (Database)  │  │   (AI)      │  │  Service    │
            └──────────────┘  └─────────────┘  └─────────────┘
```

### Principios de Diseño

- **Modular**: Separación clara de responsabilidades por módulos
- **Escalable**: Preparado para crecimiento horizontal
- **Mantenible**: Código limpio con documentación completa
- **Seguro**: Autenticación y autorización robustas
- **Testeable**: Alta cobertura de tests unitarios e integración

---

## 🛠️ Tecnologías

### Backend
- **Spring Boot 3.x** - Framework principal
- **Spring Security** - Seguridad y autenticación
- **Spring Data JPA** - Persistencia de datos
- **Hibernate** - ORM
- **Bean Validation** - Validación de datos

### Base de Datos
- **MySQL 8.0+** - Base de datos relacional

### Autenticación
- **Keycloak** - Gestión de identidades
- **OAuth2** - Protocolo de autorización
- **JWT** - Tokens de acceso

### IA & Machine Learning
- **Ollama** - Procesamiento de modelos de IA locales
- Integración con múltiples modelos (GPT-4, Claude, Gemini)

### Documentación
- **Swagger/OpenAPI 3.0** - Documentación automática de API
- **SpringDoc** - Generación de documentación

### Email
- **Spring Mail** - Envío de emails
- **Thymeleaf** - Templates de email HTML

### Containerización
- **Docker** - Contenedores
- **Docker Compose** - Orquestación

### Testing
- **JUnit 5** - Framework de testing
- **Mockito** - Mocking
- **Spring Boot Test** - Tests de integración

### Tools & Utils
- **Lombok** - Reducción de boilerplate
- **Jackson** - Serialización JSON
- **SLF4J/Logback** - Logging

---

## 📦 Requisitos Previos

Antes de comenzar, asegúrate de tener instalado:

- **Java 17+** ([Descargar](https://www.oracle.com/java/technologies/downloads/))
- **Maven 3.8+** ([Descargar](https://maven.apache.org/download.cgi))
- **MySQL 8.0+** ([Descargar](https://dev.mysql.com/downloads/))
- **Docker & Docker Compose** (Opcional) ([Descargar](https://www.docker.com/products/docker-desktop))
- **Keycloak** (para autenticación) ([Descargar](https://www.keycloak.org/downloads))
- **Ollama** (para IA) ([Descargar](https://ollama.ai/))

---

## 🚀 Instalación

### 1. Clonar el Repositorio

```bash
git clone https://github.com/tu-usuario/ifit-api.git
cd ifit-api
```

### 2. Configurar Base de Datos

Crear la base de datos en MySQL:

```sql
CREATE DATABASE ifit CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Configurar Variables de Entorno

Crear archivo `.env` en la raíz del proyecto:

```env
# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=ifit
DB_USER=root
DB_PASSWORD=tu_password

# Email
IFIT_MAIL_PASSWORD=tu_password_email_app

# Keycloak
KEYCLOAK_REALM=ifit
KEYCLOAK_CLIENT_ID=ifit-api
KEYCLOAK_CLIENT_SECRET=tu_client_secret
```

### 4. Compilar el Proyecto

```bash
mvn clean install
```

### 5. Ejecutar la Aplicación

```bash
mvn spring-boot:run
```

O con Java:

```bash
java -jar target/ifit-0.0.1-SNAPSHOT.jar
```

La API estará disponible en: `http://localhost:8080/ifit/api/v1`

---

## ⚙️ Configuración

### application.properties

Archivo principal de configuración ubicado en `src/main/resources/application.properties`:

```properties
# Application
spring.application.name=ifit

# Server
server.port=8080
server.servlet.context-path=/ifit/api/v1

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/ifit
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=ifit.communication@gmail.com
spring.mail.password=${IFIT_MAIL_PASSWORD}

# Swagger/OpenAPI
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

### Docker Compose

Para ejecutar con Docker:

```bash
docker-compose up -d
```

---

## 📁 Estructura del Proyecto

```
ifit-api/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/uca/juangarcia/ifit/
│   │   │       ├── IfitApplication.java          # Clase principal
│   │   │       ├── modules/                       # Módulos funcionales
│   │   │       │   ├── user/                     # Gestión de usuarios
│   │   │       │   │   ├── controller/
│   │   │       │   │   ├── dto/
│   │   │       │   │   ├── mapper/
│   │   │       │   │   ├── model/
│   │   │       │   │   ├── repository/
│   │   │       │   │   └── service/
│   │   │       │   ├── coach/                    # Modelos de IA
│   │   │       │   ├── questionnaire/            # Cuestionarios
│   │   │       │   ├── notification/             # Emails
│   │   │       │   └── auth/                     # Autenticación
│   │   │       └── shared/                       # Componentes compartidos
│   │   │           ├── config/                   # Configuraciones
│   │   │           ├── exception/                # Excepciones custom
│   │   │           ├── security/                 # Seguridad
│   │   │           └── utils/                    # Utilidades
│   │   └── resources/
│   │       ├── application.properties            # Configuración
│   │       ├── data-init.sql                     # Datos iniciales
│   │       └── templates/                        # Templates email
│   └── test/                                      # Tests
│       └── java/
│           └── com/uca/juangarcia/ifit/
│               ├── junit/                         # Tests unitarios
│               └── helpers/                       # Helpers de test
├── docker-compose.yml                             # Configuración Docker
├── pom.xml                                        # Dependencias Maven
└── README.md                                      # Este archivo
```

---

## 🎯 Módulos de la API

### 1. 👤 User Module

Gestión completa de usuarios del sistema.

**Entidades:**
- `AppUser` - Usuario principal
- `AppRole` - Roles del sistema (USER, ADMIN)
- `ExperienceLevel` - Niveles de experiencia fitness

**Funcionalidades:**
- CRUD de usuarios
- Asignación de roles
- Gestión de niveles de experiencia
- Asignación de coaches de IA
- Verificación de email
- Proceso de registro completo

**DTOs:**
- `AppUserResponseDto` - Respuesta completa del usuario
- `CreateAppUserRequestDto` - Creación de usuario
- `UpdateAppUserRequestDto` - Actualización de usuario

---

### 2. 🤖 Coach Module

Gestión de modelos de IA para coaching personalizado.

**Entidades:**
- `CoachModelType` - Tipos de modelos de IA disponibles

**Funcionalidades:**
- Gestión de modelos de IA (GPT-4, Claude, Gemini)
- Habilitación/deshabilitación de modelos
- Asignación de coaches a usuarios
- Metadata de modelos (nombre, descripción, emoji)

**DTOs:**
- `CoachModelTypeResponseDto` - Información del modelo
- `CreateCoachModelTypeRequestDto` - Crear modelo
- `UpdateCoachModelTypeRequestDto` - Actualizar modelo

---

### 3. 📝 Questionnaire Module

Sistema completo de cuestionarios dinámicos.

**Entidades:**
- `Questionnaire` - Cuestionario completo
- `Question` - Pregunta individual
- `QuestionOption` - Opciones de respuesta
- `QuestionType` - Tipo de pregunta (ENUM)
- `QuestionnaireResponse` - Respuesta de usuario a cuestionario
- `UserAnswer` - Respuesta individual a pregunta

**Funcionalidades:**
- Creación de cuestionarios dinámicos
- Preguntas con flujo condicional
- Múltiples tipos de preguntas
- Almacenamiento de respuestas
- Generación de perfiles basados en respuestas
- Resúmenes de cuestionarios completados

**Tipos de Preguntas:**
- `SINGLE_CHOICE` - Selección única
- `MULTIPLE_CHOICE` - Selección múltiple
- `TEXT` - Respuesta de texto libre
- `NUMBER` - Respuesta numérica

---

### 4. 📧 Notification Module

Sistema de notificaciones por email.

**Entidades:**
- `AppEmailDetails` - Detalles de email

**Funcionalidades:**
- Envío de emails transaccionales
- Templates HTML personalizables
- Verificación de email
- Notificaciones de eventos
- Cola de emails

**DTOs:**
- `EmailRequestDto` - Solicitud de envío
- `EmailResponseDto` - Confirmación de envío
- `VerifyEmailRequestDto` - Verificación de email
- `VerifyEmailResponseDto` - Resultado de verificación

---

### 5. 🔐 Auth Module

Servicios de autenticación y validación.

**Funcionalidades:**
- Integración con Keycloak
- Validación de tokens JWT
- Validación de permisos
- Servicios de validación custom

---

## 🔌 Endpoints Principales

### Base URL
```
http://localhost:8080/ifit/api/v1
```

### 👤 Users

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/users` | Listar todos los usuarios | ✅ |
| GET | `/users/{id}` | Obtener usuario por ID | ✅ |
| GET | `/users/email/{email}` | Buscar usuario por email | ✅ |
| POST | `/users` | Crear nuevo usuario | ❌ |
| PUT | `/users/{id}` | Actualizar usuario | ✅ |
| DELETE | `/users/{id}` | Eliminar usuario | ✅ |
| POST | `/users/{id}/assign-coach/{coachId}` | Asignar coach | ✅ |
| POST | `/users/{id}/assign-experience/{levelId}` | Asignar nivel | ✅ |
| POST | `/users/{userId}/verify` | Verificar email | ❌ |

### 🤖 Coach Models

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/coach-models` | Listar modelos habilitados | ✅ |
| GET | `/coach-models/{id}` | Obtener modelo por ID | ✅ |
| POST | `/coach-models` | Crear modelo | ✅ ADMIN |
| PUT | `/coach-models/{id}` | Actualizar modelo | ✅ ADMIN |
| DELETE | `/coach-models/{id}` | Deshabilitar modelo | ✅ ADMIN |

### 📝 Questionnaires

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/questionnaires` | Listar cuestionarios | ✅ |
| GET | `/questionnaires/{id}` | Obtener cuestionario | ✅ |
| POST | `/questionnaires/{id}/start` | Iniciar cuestionario | ✅ |
| POST | `/questionnaires/responses/{responseId}/answer` | Responder pregunta | ✅ |
| GET | `/questionnaires/responses/{responseId}/summary` | Resumen de respuestas | ✅ |
| GET | `/questionnaires/user/{userId}/responses` | Respuestas de usuario | ✅ |

### 📧 Notifications

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/emails/send` | Enviar email | ✅ |
| POST | `/emails/verify` | Verificar email | ❌ |

### 📊 Experience Levels

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/experience-levels` | Listar niveles | ❌ |

---

## 🔒 Seguridad

### Autenticación

La API utiliza **OAuth2 con JWT** a través de **Keycloak**:

1. El cliente obtiene un token de Keycloak
2. El token se incluye en cada request: `Authorization: Bearer {token}`
3. El API Gateway valida el token
4. La API procesa la request

### Roles

| Rol | Descripción | Acceso |
|-----|-------------|--------|
| `ROLE_USER` | Usuario estándar | Endpoints de usuario |
| `ROLE_ADMIN` | Administrador | Todos los endpoints |

### Endpoints Públicos

- `POST /users` - Registro de usuario
- `POST /users/{userId}/verify` - Verificación de email
- `GET /experience-levels` - Listar niveles de experiencia
- `POST /emails/verify` - Verificar email

---

## 📚 Documentación de API

### Swagger UI

Una vez la aplicación esté ejecutándose, accede a la documentación interactiva:

```
http://localhost:8080/ifit/api/v1/swagger-ui.html
```

### OpenAPI JSON

Especificación OpenAPI 3.0 disponible en:

```
http://localhost:8080/ifit/api/v1/v3/api-docs
```

### Características de la Documentación

- ✅ Especificaciones completas de todos los endpoints
- ✅ Ejemplos de request/response
- ✅ Modelos de datos con validaciones
- ✅ Códigos de respuesta HTTP
- ✅ Interfaz interactiva para probar endpoints
- ✅ Autenticación integrada

---

## 🧪 Testing

### Ejecutar Tests

Todos los tests:
```bash
mvn test
```

Tests de un módulo específico:
```bash
mvn test -Dtest=AppUserServiceTest
```

### Cobertura de Tests

Generar reporte de cobertura:
```bash
mvn clean test jacoco:report
```

Ver reporte en: `target/site/jacoco/index.html`

### Tipos de Tests

#### Tests Unitarios
- Tests de servicios con mocks
- Tests de mappers
- Tests de validaciones
- Ubicación: `src/test/java/.../junit/`

#### Tests de Integración
- Tests de controllers
- Tests de repositorios
- Tests end-to-end
- Ubicación: `src/test/java/.../integration/`

### Objetivo de Cobertura

🎯 **Meta: >80% de cobertura de código**

---

## 🗺️ Roadmap

### ✅ Fase 1: Core API (Completado)
- [x] Módulo de usuarios
- [x] Autenticación con Keycloak
- [x] Sistema de cuestionarios
- [x] Integración con modelos de IA
- [x] Sistema de notificaciones

### 🔄 Fase 2: Refactorización (En Progreso)
- [x] Módulo User refactorizado
- [ ] Módulo Coach refactorizado
- [ ] Módulo Notification refactorizado
- [ ] Módulo Questionnaire refactorizado
- [ ] Documentación completa

### 🔮 Fase 3: Características Avanzadas (Planificado)
- [ ] Sistema de planes de entrenamiento
- [ ] Tracking de progreso
- [ ] Dashboard de estadísticas
- [ ] API de nutrición
- [ ] Integración con wearables
- [ ] Sistema de gamificación
- [ ] Comunidad y social features

### 🚀 Fase 4: Optimización (Futuro)
- [ ] Caché con Redis
- [ ] Message broker (RabbitMQ/Kafka)
- [ ] Búsqueda con Elasticsearch
- [ ] Monitorización con Prometheus
- [ ] CI/CD pipeline
- [ ] Kubernetes deployment

---

## 🤝 Contribución

¡Las contribuciones son bienvenidas! Para contribuir:

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

### Estándares de Código

- ✅ Java 17+ features
- ✅ Records para DTOs
- ✅ JavaDoc completo
- ✅ Bean Validation
- ✅ Tests unitarios (>80% cobertura)
- ✅ Código limpio y SOLID principles

---

## 👨‍💻 Autor

**Juan García Candón**

- Universidad: Universidad de Cádiz
- Proyecto: Trabajo Final de Grado (TFG)
- Email: ifit.communication@gmail.com
- LinkedIn: [Tu LinkedIn]
- GitHub: [@tu-usuario]

---

## 📄 Licencia

Este proyecto es un Trabajo Final de Grado desarrollado para la Universidad de Cádiz.

**Todos los derechos reservados © 2024-2025 Juan García Candón**

---

## 🙏 Agradecimientos

- Universidad de Cádiz - Escuela Superior de Ingeniería
- Spring Framework Team
- Keycloak Community
- Ollama Project
- Todos los contribuidores de librerías open source utilizadas

---

## 📞 Soporte

Si encuentras algún problema o tienes preguntas:

1. Revisa la [documentación](#-documentación-de-api)
2. Busca en [Issues existentes](https://github.com/tu-usuario/ifit-api/issues)
3. Crea un [nuevo Issue](https://github.com/tu-usuario/ifit-api/issues/new)
4. Contacta al autor

---

<p align="center">
  Hecho con ❤️ para mejorar el fitness con tecnología
</p>

<p align="center">
  <strong>IFit API</strong> - Tu compañero de fitness inteligente
</p>