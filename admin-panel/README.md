# Admin Panel — Panel de Administración (Vaadin)

[![Vaadin](https://img.shields.io/badge/Vaadin-24.8.0-00b4f0.svg)](https://vaadin.com/)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-green.svg)](https://spring.io/projects/spring-boot)

> Panel de administración web de iFit (CU-10). Aplicación Vaadin Flow que permite al equipo gestionar clientes, ejercicios, entrenadores, niveles y cuestionarios. Consume la API **siempre a través del API Gateway**, nunca contra los microservicios ni la base de datos directamente.

> [!NOTE]
> **Capa de Documentación: Núcleo de Administración (Nivel Interno)**  
> Este documento detalla la interfaz web administrativa de back-office. Para la vista general y orquestación del sistema completo, consulta el [README.md del proyecto general](../../README.md).

---

## Tabla de Contenidos

- [Descripción General](#descripción-general)
- [Stack Tecnológico](#stack-tecnológico)
- [Arquitectura](#arquitectura)
- [Vistas](#vistas)
- [Integración con el Gateway](#integración-con-el-gateway)
- [Seguridad y Sesión](#seguridad-y-sesión)
- [Configuración](#configuración)
- [Puesta en Marcha](#puesta-en-marcha)
- [Autor](#autor)

---

## Descripción General

El Admin Panel es la interfaz de back-office de iFit. A diferencia de la app móvil (cliente final), está pensado para tareas administrativas: alta y mantenimiento del catálogo de ejercicios, gestión de usuarios, definición de entrenadores (coaches), niveles de experiencia y cuestionarios.

Está construido con **Vaadin Flow**, lo que permite escribir toda la interfaz en Java (sin HTML/JS manual). Aplica el tema propio `ifit-admin` en variante oscura (`Lumo.DARK`) para alinear la identidad visual con la app móvil.

---

## Stack Tecnológico

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.5.7 |
| UI | Vaadin Flow 24.8.0 (`vaadin-spring-boot-starter`) |
| Tema | `ifit-admin` · `Lumo.DARK` |
| Cliente HTTP | `RestClient` (JDK HttpClient, soporta PATCH) |
| Puerto | 8090 (local) · 8084 (perfil Docker) |

---

## Arquitectura

El panel **no tiene base de datos ni lógica de negocio propia**: es un cliente que orquesta llamadas REST contra el resto del sistema, siempre pasando por el gateway.

```
┌─────────────────┐      REST (RestClient)      ┌──────────────┐      ┌──────────────────┐
│  Admin Panel    │ ──────────────────────────▶ │ API Gateway  │ ───▶ │ IFit / Ronnie    │
│  (Vaadin Flow)  │     ifit.gateway.base-url   │  :8080       │      │ :8081 / :8082    │
└─────────────────┘                             └──────────────┘      └──────────────────┘
```

Cada área funcional tiene un **cliente de API** dedicado que encapsula las llamadas al gateway:

| Cliente | Responsabilidad |
|---|---|
| `AuthService` | Login/logout y sesión del administrador |
| `UserApiClient` | Gestión de clientes/usuarios |
| `ExerciseApiClient` | Catálogo de ejercicios |
| `CoachApiClient` | Entrenadores (coaches) |
| `ExperienceLevelApiClient` | Niveles de experiencia |
| `QuestionnaireApiClient` | Cuestionarios |
| `RoutineApiClient` | Rutinas generadas |

---

## Vistas

El layout principal (`MainLayout`) aporta cabecera, drawer de navegación y guard de sesión. El drawer expone cinco secciones:

| Sección (drawer) | Vista | Contenido |
|---|---|---|
| Clientes | `UsersView` | Listado y gestión de usuarios |
| Ejercicios | `ExercisesView` | Catálogo de ejercicios |
| Entrenadores | `CoachesView` | Coaches y su modelo de IA |
| Niveles | `ExperienceLevelsView` | Niveles de experiencia |
| Cuestionarios | `QuestionnairesView` | Cuestionarios del onboarding |

Además existen `LoginView` (acceso, fuera del layout protegido) y `RoutinesView` (detalle de rutinas). `ViewSupport` reúne utilidades comunes de UI compartidas por las vistas.

---

## Integración con el Gateway

Todo el tráfico saliente usa un único `RestClient` configurado en `GatewayClientConfig`:

```java
@Bean
public RestClient gatewayRestClient(@Value("${ifit.gateway.base-url}") String baseUrl) {
    return RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(new JdkClientHttpRequestFactory())
            .build();
}
```

Se usa `JdkClientHttpRequestFactory` (HttpClient de la JDK) en lugar de la factoría por defecto porque esta última, basada en `HttpURLConnection`, **no soporta el verbo PATCH** (necesario, por ejemplo, para `toggle-active`).

---

## Seguridad y Sesión

El administrador se autentica a través del gateway (`AuthService`), que valida las credenciales contra Keycloak y devuelve el token. La sesión se mantiene en el lado del panel y `MainLayout` actúa como **guard**: cualquier vista bajo el layout protegido exige sesión iniciada.

```java
@Override
public void beforeEnter(BeforeEnterEvent event) {
    if (!auth.isAuthenticated()) {
        event.forwardTo(LoginView.class);   // sin sesión → login
    }
}
```

El botón "Salir" de la cabecera invoca `auth.logout()` y redirige a `LoginView`.

---

## Configuración

Archivo: `src/main/resources/application.properties`

```properties
spring.application.name=admin-panel
server.port=8090

# Todo el tráfico del panel pasa por el API Gateway
ifit.gateway.base-url=http://localhost:8080

vaadin.launch-browser=true
vaadin.allowed-packages=com.uca.juangarcia.adminpanel
```

Existe además un perfil Docker (`application-docker.properties`, activable con `SPRING_PROFILES_ACTIVE=docker`) que apunta al gateway por DNS interno (`http://gateway:8080`), usa el puerto 8084 y desactiva la apertura automática del navegador.

---

## Puesta en Marcha

### Prerrequisitos

- Java 21+
- Maven 3.9+
- **API Gateway** corriendo en `localhost:8080` (y, por detrás, Keycloak, Eureka, IFit y Ronnie)

### Compilar y arrancar

```bash
cd admin-panel
mvn clean package -DskipTests
mvn spring-boot:run
```

El panel estará disponible en `http://localhost:8090` (abre el navegador automáticamente en local).

### Build de producción

El perfil `production` compila el frontend de Vaadin optimizado:

```bash
mvn clean package -Pproduction
```

### Orden de arranque

El panel es el último eslabón: necesita el gateway (y la cadena completa por detrás) activo para funcionar.

```
1. Eureka Server  → localhost:8761
2. Keycloak       → localhost:9090
3. API Gateway    → localhost:8080
4. IFit / Ronnie  → localhost:8081 / 8082
5. Admin Panel    → localhost:8090   ← este módulo
```

---

## Autor

**Juan García Candón**  
Universidad de Cádiz — Escuela Superior de Ingeniería  
Trabajo Final de Grado (TFG), 2024–2025
