# API Gateway — Punto de Entrada Único

[![Spring Cloud Gateway](https://img.shields.io/badge/Spring%20Cloud%20Gateway-2025.0.0-brightgreen.svg)](https://spring.io/projects/spring-cloud-gateway)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-green.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/)
[![WebFlux](https://img.shields.io/badge/Reactive-WebFlux-orange.svg)](https://docs.spring.io/spring-framework/reference/web/webflux.html)

> Único punto de entrada al sistema iFit. Valida tokens JWT, enruta peticiones a los microservicios internos y aplica el filtro TokenRelay para propagar la identidad del usuario.

> [!NOTE]
> **Capa de Documentación: Núcleo de Infraestructura (Nivel Interno)**  
> Este documento detalla una pieza de infraestructura específica de la red interna de microservicios. Para la vista general y orquestación del sistema completo, consulta el [README.md del proyecto general](../../README.md).

---

## 📖 Antes de comenzar

**¿Eres nuevo en OAuth2 o WebFlux?** Consulta los **Apéndices C y D** de este documento para entender:
- Glosario completo de términos clave de OAuth2
- Qué significa que el Gateway sea un **Resource Server**
- Por qué el Gateway usa **WebFlux** en lugar de Spring Boot Web
- Comparativa detallada: Servlet vs Reactivo
- Decisión arquitectónica y análogías útiles

---

## Tabla de Contenidos

- [Descripción General](#descripción-general)
- [Por qué un API Gateway](#por-qué-un-api-gateway)
- [Stack Tecnológico](#stack-tecnológico)
- [Rutas Configuradas](#rutas-configuradas)
  - [IFIT-PUBLIC](#ifit-public--ruta-pública)
  - [RONNIE](#ronnie--ruta-hacia-el-microservicio-ia)
  - [IFIT-PRIVATE](#ifit-private--ruta-privada-principal)
- [Mecanismos Clave](#mecanismos-clave)
  - [StripPrefix=3](#stripprefix3)
  - [TokenRelay](#tokenrelay)
  - [Resolución lb:// con Eureka](#resolución-lb-con-eureka)
  - [Doble validación JWT](#doble-validación-jwt)
- [Seguridad](#seguridad)
- [Endpoints Protegidos por Rol](#-endpoints-protegidos-por-rol)
  - [Endpoints para Administradores](#endpoints-protegidos-para-administradores-admin-panel)
  - [Endpoints para Usuarios Regulares](#endpoints-accesibles-para-usuarios-regulares)
  - [Validación de Roles](#cómo-se-valida-el-rol)
- [Service Discovery (Eureka)](#service-discovery-eureka)
- [Configuración](#configuración)
- [Puesta en Marcha](#puesta-en-marcha)
- [Apéndices](#-apéndices)

---

## Descripción General

El API Gateway es el servicio que recibe **todas** las peticiones de los clientes autorizados y las distribuye hacia los microservicios internos. Los clientes del sistema son:

- **App MAUI** (.NET) — Cliente móvil para usuarios finales
- **Admin Panel** (Vaadin) — Cliente web para administración centralizada

Ningún microservicio interno es accesible directamente desde el exterior; el gateway actúa como **frontera de seguridad única** y **punto de control centralizado**.

Sus responsabilidades son:

1. **Validar el JWT** antes de que la petición llegue a cualquier servicio interno.
2. **Enrutar** hacia `IFIT` (Puerto 8081) o `RONNIE` (Puerto 8082) según el path.
3. **Aplicar StripPrefix** para eliminar el prefijo de ruta antes de reenviar.
4. **Propagar el token** (`TokenRelay`) a los microservicios que lo necesitan.
5. **Exponer rutas públicas** sin autenticación para registro y login.

---

## Por qué un API Gateway

Sin gateway, los clientes (App MAUI y Admin Panel) necesitarían:
- Conocer las URLs y puertos de cada microservicio
- Gestionar la autenticación individualmente con cada uno
- Manejar cambios de infraestructura de manera descentralizada
- Duplicar lógica de seguridad en cada cliente

Con el gateway, tanto la App MAUI como el Admin Panel:
- Hablan siempre con **un único punto de entrada**: `http://localhost:8080/ifit/api/v1/...`
- Comparten la **validación centralizada** de tokens JWT contra Keycloak
- Se benefician de que los microservicios puedan moverse, escalarse o añadirse sin cambios en el cliente
- Confían en una **defensa en profundidad**: validación en el gateway + validación en cada microservicio

---

## Stack Tecnológico

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.5.7 + Spring Cloud 2025.0.0 |
| Gateway | Spring Cloud Gateway (WebFlux reactivo) |
| Seguridad | Spring Security WebFlux + OAuth2 Resource Server |
| Service Discovery | Spring Cloud Netflix Eureka Client |
| Modelo de concurrencia | Reactivo (Project Reactor, Netty) |

**Nota importante**: Spring Cloud Gateway usa **WebFlux** (reactivo, no blocking) en lugar del modelo servlet tradicional. Por eso la configuración de seguridad usa `@EnableWebFluxSecurity` y `ServerHttpSecurity` en lugar de las clases de servlet. Esto le permite manejar miles de conexiones concurrentes con pocos hilos.

---

## Rutas Configuradas

Todas las rutas siguen el patrón base `/ifit/api/v1/...` y aplican `StripPrefix=3`, lo que elimina los tres primeros segmentos del path antes de reenviar al servicio destino.

### IFIT-PUBLIC — Ruta pública

```yaml
- id: IFIT-PUBLIC
  uri: lb://IFIT
  predicates:
    - Path=/ifit/api/v1/auth/**, /ifit/api/v1/exercise-images/**
  filters:
    - StripPrefix=3
```

| Propiedad | Valor |
|---|---|
| Destino | Microservicio IFIT |
| Paths | `/ifit/api/v1/auth/**` — `/ifit/api/v1/exercise-images/**` |
| Autenticación requerida | **No** |
| TokenRelay | **No** — no hay token que propagar |

Cubre los endpoints de autenticación (login, register, refresh, logout, verify) y las imágenes del catálogo de ejercicios servidas desde Ronnie. Son públicos porque el usuario aún no tiene token antes de autenticarse.

---

### RONNIE — Ruta hacia el microservicio IA

```yaml
- id: RONNIE
  uri: lb://RONNIE
  predicates:
    - Path=/ifit/api/v1/ronnie/**, /ifit/api/v1/serena/**,
           /ifit/api/v1/kael/**, /ifit/api/v1/eliud/**,
           /ifit/api/v1/messages/**
  filters:
    - StripPrefix=3
    - TokenRelay
```

| Propiedad | Valor |
|---|---|
| Destino | Microservicio RONNIE |
| Paths | `/ifit/api/v1/{ronnie,serena,kael,eliud,messages}/**` |
| Autenticación requerida | **Sí** |
| TokenRelay | **Sí** — el JWT se reenvía a Ronnie |

Cubre todos los endpoints de chat y generación de rutinas de los coaches, más el historial de mensajes. El token se propaga porque Ronnie usa `JwtUtils.extractUserId()` para identificar al usuario.

---

### IFIT-PRIVATE — Ruta privada principal

```yaml
- id: IFIT-PRIVATE
  uri: lb://IFIT
  predicates:
    - Path=/ifit/api/v1/**
  filters:
    - StripPrefix=3
    - TokenRelay
```

| Propiedad | Valor |
|---|---|
| Destino | Microservicio IFIT |
| Paths | Cualquier `/ifit/api/v1/**` no capturado antes |
| Autenticación requerida | **Sí** |
| TokenRelay | **Sí** |

Captura el resto de paths (usuarios, cuestionarios, rutinas, coaches, ejercicios). El orden de las rutas importa: Spring Cloud Gateway evalúa en orden de declaración, por lo que `IFIT-PUBLIC` y `RONNIE` tienen prioridad sobre esta regla general.

---

## Mecanismos Clave

### StripPrefix=3

El cliente envía peticiones con el prefijo de ruta completo:

```
Petición del cliente:
  POST /ifit/api/v1/users

Spring Cloud Gateway aplica StripPrefix=3:
  Segmentos eliminados: /ifit  /api  /v1
  
URL reenviada al microservicio:
  POST /users
```

Esto desacopla la URL pública del cliente de la URL interna del servicio. IFit y Ronnie no conocen el prefijo `/ifit/api/v1`; solo ven sus propias rutas (`/users`, `/routines`, `/ronnie/generate-routine`, etc.).

El número 3 corresponde exactamente a los tres segmentos `/ifit/api/v1`. Si el prefijo cambiara, solo habría que actualizar `StripPrefix` en el gateway, sin tocar los microservicios.

---

### TokenRelay

Cuando un usuario autentica, recibe un `access_token` JWT de Keycloak. Lo incluye en cada petición:

```
Authorization: Bearer eyJhbGciOiJSUzI1NiJ9...
```

El filtro `TokenRelay` extrae ese token y lo **reenvía en la cabecera Authorization** de la petición interna hacia el microservicio destino. Sin `TokenRelay`, los servicios internos recibirían peticiones sin token y no podrían identificar al usuario.

```
Cliente → Gateway (valida token) → [TokenRelay] → IFit/Ronnie (recibe token intacto)
```

La ruta `IFIT-PUBLIC` no tiene `TokenRelay` porque en los endpoints de auth no hay token todavía (es la primera llamada antes del login).

---

### Resolución lb:// con Eureka

Las URIs de destino usan el esquema `lb://` (load balancer) en lugar de una URL fija:

```yaml
uri: lb://IFIT    # en lugar de http://localhost:8081
uri: lb://RONNIE  # en lugar de http://localhost:8082
```

El gateway consulta el registro de Eureka para resolver `IFIT` y `RONNIE` a la IP y puerto reales del microservicio. Esto permite:
- Cambiar el puerto de un servicio sin tocar la configuración del gateway.
- Escalar a múltiples instancias: Eureka devuelve varias IPs y el gateway balancea la carga.
- Detectar servicios caídos: Eureka marca instancias no disponibles y el gateway no las usa.

Los nombres `IFIT` y `RONNIE` son los `spring.application.name` registrados en Eureka por cada microservicio (en mayúsculas por convención de Eureka).

---

### Doble validación JWT — Flujo Completo Paso a Paso

El JWT se valida **dos veces** en el sistema. Aquí está el flujo completo detallado:

#### **Paso 1: Cliente obtiene el token de Keycloak**

```
POST /auth/login
├─ Keycloak valida credenciales (usuario/contraseña)
├─ Genera JWT firmado con clave privada RSA
└─ Devuelve: { access_token: "eyJhbGci...", expires_in: 3600 }
```

El token contiene claims como:
```json
{
  "iss": "http://localhost:9090/realms/ifit-realm",
  "sub": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "name": "Juan García",
  "exp": 1719014400,
  "iat": 1719010800
}
```

#### **Paso 2: Cliente envía petición protegida al Gateway**

```
GET /ifit/api/v1/users/profile
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### **Paso 3: Gateway valida la petición (SecurityConfig)**

El `SecurityWebFilterChain` del gateway comprueba:

**a) ¿La ruta es pública o privada?**
```java
.pathMatchers("/ifit/api/v1/auth/**").permitAll()           // Pública
.pathMatchers("/ifit/api/v1/exercise-images/**").permitAll() // Pública
.anyExchange().authenticated()                                // Privada
```

Para `/ifit/api/v1/users/profile` → **Requiere autenticación**

**b) Extrae el JWT de la cabecera Authorization**
```
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
                      ↓ Se extrae automáticamente
Token: eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

**c) VALIDA LA FIRMA CONTRA KEYCLOAK**

La clave aquí es entender qué ocurre:

1. **Primera ejecución del gateway:**
   ```
   GET http://localhost:9090/realms/ifit-realm/protocol/openid-connect/certs
   ↓
   Keycloak responde con el JWKS (JSON Web Key Set):
   {
     "keys": [
       {
         "kty": "RSA",
         "kid": "abc123",
         "use": "sig",
         "n": "xjlCRBqkQr...",  // Módulo N de la clave pública RSA
         "e": "AQAB"            // Exponente público
       }
     ]
   }
   ↓
   Spring Security cachea esta clave pública en memoria
   ```

2. **Para cada petición:**
   - Extrae el header del JWT y busca el `kid` (key ID)
   - Obtiene la clave pública correspondiente del JWKS cacheado
   - Verifica la firma RS256:
     ```
     HMACSHA256(
       base64UrlEncode(header) + "." + base64UrlEncode(payload),
       public_key_from_jwks
     ) === signature_del_jwt
     ```
   - Si la firma **NO coincide** → ❌ **401 Unauthorized** (token manipulado)
   - Si la firma **SÍ coincide** → ✅ Continúa

**d) Verifica claims obligatorios**

Además de la firma:
```
✅ issuer (iss): ¿Es 'http://localhost:9090/realms/ifit-realm'?
✅ expiración (exp): ¿Es mayor que la hora actual?
✅ timestamp de emisión (iat): ¿Es coherente?
```

Si alguno **falla** → ❌ **401 Unauthorized**

#### **Paso 4: Gateway decodifica y extrae los claims**

Tras validar la firma, Spring Security decodifica el payload:

```json
{
  "iss": "http://localhost:9090/realms/ifit-realm",
  "sub": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "name": "Juan García",
  "email": "juan@example.com",
  "exp": 1719014400,
  "iat": 1719010800
}
```

Estos claims están ahora disponibles en el `ServerWebExchange` del gateway.

#### **Paso 5: Gateway determina la ruta destino y aplica filtros**

El gateway compara el path `/ifit/api/v1/users/profile` con las rutas configuradas:

```yaml
- id: IFIT-PUBLIC
  predicates:
    - Path=/ifit/api/v1/auth/**, /ifit/api/v1/exercise-images/**
  # ❌ No coincide

- id: RONNIE
  predicates:
    - Path=/ifit/api/v1/ronnie/**, /ifit/api/v1/serena/**, ...
  # ❌ No coincide

- id: IFIT-PRIVATE
  predicates:
    - Path=/ifit/api/v1/**
  # ✅ COINCIDE → Destino: microservicio IFIT
  filters:
    - StripPrefix=3
    - TokenRelay
```

**StripPrefix=3:**
```
Petición original: GET /ifit/api/v1/users/profile
Segmentos eliminados: /ifit (1), /api (2), /v1 (3)
Petición reenviada: GET /users/profile
```

**TokenRelay:**
```
El filtro TokenRelay reenvía el JWT INTACTO en la cabecera Authorization:

GET /users/profile HTTP/1.1
Host: localhost:8081 (IFIT)
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
                      ↑ MISMO TOKEN que recibió del cliente
```

**¿Por qué es crítico TokenRelay?**
- Sin él, el microservicio recibiría una petición sin token
- El microservicio necesita el token para extraer el usuario_id del claim 'sub'
- Sin el usuario_id, no puede procesar la petición correctamente

#### **Paso 6: Microservicio IFIT recibe la petición y valida de nuevo**

IFIT (puerto 8081) recibe:
```http
GET /users/profile HTTP/1.1
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

IFIT **también tiene su propio SecurityConfig** como OAuth2 Resource Server:

1. Extrae el JWT de la cabecera Authorization
2. Descarga el JWKS de Keycloak (usando el `issuer-uri` configurado en IFIT)
3. Verifica la firma RS256 **de nuevo**
4. Comprueba claims (iss, exp, iat) **de nuevo**
5. Extrae el claim 'sub' → `f47ac10b-58cc-4372-a567-0e02b2c3d479`

Si la validación **pasa**:
- El usuario_id se obtiene correctamente
- La petición se procesa como si fuera del usuario Juan García
- Se aplican reglas de autorización (@PreAuthorize)

Si **falla**:
- ❌ **401 Unauthorized**

#### **Resumen: Doble validación**

```
1. En el Gateway (Spring Cloud Gateway + SecurityConfig)
   - Extrae JWT de la petición
   - Descarga JWKS de Keycloak
   - Verifica firma RS256 contra clave pública
   - Verifica iss, exp, iat
   - Bloquea peticiones inválidas (401)
   - Si pasa: reenvía con TokenRelay

2. En el microservicio destino (IFit o Ronnie)
   - Valida JWT de nuevo contra Keycloak
   - Extrae claims (especialmente 'sub' para usuario_id)
   - Aplica reglas de autorización (@PreAuthorize)
   - Procesa la petición con el usuario identificado
```

Esta arquitectura de **defensa en profundidad** garantiza que aunque un atacante bypasease el gateway, los servicios internos seguirían validando el token por su cuenta. Ningún servicio confía ciegamente en el gateway.

---

## Seguridad

### Roles OAuth2/OpenID Connect en la Arquitectura

Antes de entrar en detalles técnicos, es importante entender los roles en OAuth2:

| Rol | Componente | Responsabilidad |
|---|---|---|
| **Authorization Server** | Keycloak | Autentica usuarios, emite tokens JWT firmados |
| **Resource Server** | Gateway + IFIT + Ronnie | Valida tokens, protege recursos, procesa peticiones |
| **Client** | App MAUI (.NET) | Solicita acceso a recursos en nombre del usuario |
| **Resource Owner** | Usuario | Propietario de los datos (tú, en la app) |

**Nota importante:** El Gateway **NO es un OAuth2 Client** (no necesita `client-id` ni `client-secret` para obtener tokens). El Gateway es un **Resource Server** que recibe tokens ya emitidos por Keycloak y los valida.

#### **¿Resource Server? ¿Qué significa?**

Un Resource Server es un servidor que:
- ✅ **Recibe** tokens JWT del cliente
- ✅ **Valida** que el token es auténtico (descargando JWKS de Keycloak)
- ✅ **Protege** recursos (no deja entrar sin token válido)
- ✅ **Entrega** los recursos si el token es correcto

Un Resource Server **NO:**
- ❌ Emite tokens (eso lo hace Keycloak, el Authorization Server)
- ❌ Autentica usuarios directamente (eso lo hace Keycloak)
- ❌ Necesita `client-secret` de Keycloak

#### **¿Client Secret vs Clave Pública RSA?**

Muchas personas confunden estos conceptos. Son **completamente diferentes**:

| Concepto | ¿Qué es? | ¿Es público? | ¿Para qué? |
|---|---|---|---|
| **Client Secret** | Credencial para autenticar un *cliente* con Keycloak | ❌ NO — secreto | Intercambiar código por token (OAuth2 auth code flow) |
| **Clave Pública RSA** (JWKS) | Clave pública para verificar firmas de JWT | ✅ SÍ — público | El gateway (Resource Server) verifica que Keycloak creó el token |

En tu arquitectura:
- El Gateway es un **Resource Server**, no un Client, por lo que **NO necesita client-secret**
- El Gateway descarga la **clave pública** (JWKS) que Keycloak publica en `/protocol/openid-connect/certs`
- La clave pública es públicamente accesible — cualquiera puede descargarla sin autenticación

### Configuración de Seguridad WebFlux

`SecurityConfig` configura la capa de seguridad WebFlux del gateway:

```java
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        http
            .authorizeExchange(exchange -> exchange
                .pathMatchers("/ifit/api/v1/auth/**").permitAll()
                .pathMatchers("/ifit/api/v1/exercise-images/**").permitAll()
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}))
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
```

La validación JWT del gateway descarga la clave pública de Keycloak desde:

```
http://localhost:9090/realms/ifit-realm/protocol/openid-connect/certs
```

Este endpoint devuelve el **JWKS** (JSON Web Key Set): la clave pública RSA que Keycloak usa para firmar los tokens. Spring Security la cachea y la usa para verificar la firma de cada token entrante sin consultar a Keycloak en cada petición.

---

## Service Discovery (Eureka)

El gateway es un cliente de Eureka. Al arrancar, se registra en el servidor Eureka y descarga el registro de todos los servicios disponibles.

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${server.port}
```

El dashboard de Eureka (`http://localhost:8761`) muestra en tiempo real qué instancias están registradas, sus IPs, puertos y estado de salud.

---

## Flujo Visual Completo del Sistema

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                                                                               │
│  CLIENTES AUTORIZADOS                                                        │
│  ┌───────────────────────────────┐  ┌──────────────────────────────────────┐│
│  │  APP MAUI (.NET)              │  │  ADMIN PANEL (Vaadin)                ││
│  │  (Cliente Móvil)              │  │  (Cliente Web de Administración)      ││
│  │  ┌─────────────────────────┐  │  │  ┌──────────────────────────────┐   ││
│  │  │ 1. Usuario introduce    │  │  │  │ 1. Admin autentica en        │   ││
│  │  │    credenciales         │  │  │  │    Keycloak                  │   ││
│  │  │ POST /auth/login        │  │  │  │ POST /auth/login             │   ││
│  │  └─────────────────────────┘  │  │  └──────────────────────────────┘   ││
│  │         │                      │  │         │                           ││
│  │         ↓                      │  │         ↓                           ││
│  │  ┌─────────────────────────┐  │  │  ┌──────────────────────────────┐   ││
│  │  │ 2. Keycloak valida      │  │  │  │ 2. Keycloak valida y emite  │   ││
│  │  │    y emite JWT          │  │  │  │    JWT                       │   ││
│  │  │ { token: "eyJ..." }     │  │  │  │ { token: "eyJ..." }         │   ││
│  │  └─────────────────────────┘  │  │  └──────────────────────────────┘   ││
│  │         │                      │  │         │                           ││
│  │         ↓                      │  │         ↓                           ││
│  │  ┌─────────────────────────┐  │  │  ┌──────────────────────────────┐   ││
│  │  │ 3. Almacena token en    │  │  │  │ 3. Almacena token en         │   ││
│  │  │    memoria              │  │  │  │    sesión web                │   ││
│  │  └─────────────────────────┘  │  │  └──────────────────────────────┘   ││
│  └───────────────────────────────┘  └──────────────────────────────────────┘│
│         │                                     │                              │
│         └──────────────────┬──────────────────┘                             │
│                            │ Ambas hacen                                    │
│                            │ solicitudes                                    │
│                            ↓                                                │
└──────────────────────────────────────────────────────────────────────────────┘
│  │ 1. Usuario introduce credenciales                                     │ │
│  │    POST /ifit/api/v1/auth/login                                      │ │
│  │    { "username": "juan", "password": "123456" }                      │ │
│  └─────────────────────────────────────┬──────────────────────────────┬─┘ │
│                                         │                              │   │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │ 3. Almacena token en memoria       │              2. Keycloak valida  │ │
│  │    token = "eyJhbGci..."          ↓                 usuario/password  │ │
│  │                            ┌──────────────────┐     y firma JWT      │ │
│  │                            │   KEYCLOAK       │                      │ │
│  │                            │   (9090)         │                      │ │
│  │                            │                  │                      │ │
│  │                            │ Authorization    │                      │ │
│  │                            │ Server (emite    │                      │ │
│  │                            │ tokens)          │                      │ │
│  │                            └──────────────────┘                      │ │
│  │                                  │                                   │ │
│  └──────────────────────────────────┼───────────────────────────────────┘ │
│                                     │ Devuelve access_token                │
│                                     │ { token: "eyJhbGci...", exp: 3600 }│
└─────────────────────────────────────┼───────────────────────────────────────┘
                                      │
         ┌────────────────────────────┴─────────────────────────────┐
         │                                                           │
         │ 4. Petición protegida con token                          │
         ↓                                                           │
┌──────────────────────────────────────────────────────────────────────────────┐
│ GATEWAY (8080) — Resource Server                                            │
│ ┌────────────────────────────────────────────────────────────────────────┐ │
│ │ 4.1. SecurityWebFilterChain recibe:                                   │ │
│ │      GET /ifit/api/v1/users/profile                                   │ │
│ │      Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...   │ │
│ └─────────────────────────────────────┬────────────────────────────────┘ │
│                                       │                                   │
│ ┌───────────────────────────────────────────────────────────────────────┐ │
│ │ 4.2. ¿Ruta pública?                                                 │ │
│ │      /auth/** o /exercise-images/** → SÍ = permitAll()             │ │
│ │      /users/profile → NO = requiere autenticación                  │ │
│ └─────────────────────────────────────┬────────────────────────────────┘ │
│                                       │                                   │
│ ┌───────────────────────────────────────────────────────────────────────┐ │
│ │ 4.3. Extrae JWT de Authorization header:                            │ │
│ │      "Authorization: Bearer eyJhbGciOi..."                           │ │
│ │      Token extraído: eyJhbGciOi...                                   │ │
│ └─────────────────────────────────────┬────────────────────────────────┘ │
│                                       │                                   │
│ ┌───────────────────────────────────────────────────────────────────────┐ │
│ │ 4.4. VALIDA FIRMA CONTRA KEYCLOAK:                                   │ │
│ │                                                                       │ │
│ │   a) Descarga JWKS (primera vez) o usa caché:                        │ │
│ │      GET /realms/ifit-realm/protocol/openid-connect/certs           │ │
│ │      Respuesta: { keys: [{kty: "RSA", kid: "abc123", n: "...", e: "..."}] }
│ │                                                                       │ │
│ │   b) Obtiene el kid del header del JWT:                              │ │
│ │      { "alg": "RS256", "kid": "abc123" }                            │ │
│ │                                                                       │ │
│ │   c) Busca la clave pública en el JWKS cacheado                      │ │
│ │                                                                       │ │
│ │   d) Verifica firma RS256:                                           │ │
│ │      HMACSHA256(                                                      │ │
│ │        header.payload,                                              │ │
│ │        public_key_from_jwks                                         │ │
│ │      ) == signature_en_el_jwt ?                                      │ │
│ │                                                                       │ │
│ │      ✅ SÍ → Firma válida, token auténtico (creado por Keycloak)    │ │
│ │      ❌ NO → Firma inválida → 401 Unauthorized                       │ │
│ └─────────────────────────────────────┬────────────────────────────────┘ │
│                                       │                                   │
│ ┌───────────────────────────────────────────────────────────────────────┐ │
│ │ 4.5. Verifica claims (si firma pasó):                                │ │
│ │      ✅ iss = "http://localhost:9090/realms/ifit-realm"?             │ │
│ │      ✅ exp > ahora_mismo?                                           │ │
│ │      ✅ iat es coherente?                                            │ │
│ │                                                                       │ │
│ │      Si cualquiera falla → 401 Unauthorized                          │ │
│ │      Si todos pasan → Continúa                                       │ │
│ └─────────────────────────────────────┬────────────────────────────────┘ │
│                                       │                                   │
│ ┌───────────────────────────────────────────────────────────────────────┐ │
│ │ 4.6. Decodifica el payload y extrae claims:                          │ │
│ │      {                                                               │ │
│ │        "iss": "http://localhost:9090/realms/ifit-realm",             │ │
│ │        "sub": "f47ac10b-58cc-4372-a567-0e02b2c3d479",                │ │
│ │        "name": "Juan García",                                         │ │
│ │        "email": "juan@example.com",                                   │ │
│ │        "exp": 1719014400,                                            │ │
│ │        "iat": 1719010800                                             │ │
│ │      }                                                               │ │
│ └─────────────────────────────────────┬────────────────────────────────┘ │
│                                       │                                   │
│ ┌───────────────────────────────────────────────────────────────────────┐ │
│ │ 4.7. Determina ruta destino:                                         │ │
│ │      /ifit/api/v1/** matches IFIT-PRIVATE                            │ │
│ │      Destino: http://localhost:8081 (IFIT)                           │ │
│ └─────────────────────────────────────┬────────────────────────────────┘ │
│                                       │                                   │
│ ┌───────────────────────────────────────────────────────────────────────┐ │
│ │ 4.8. Aplica filtros:                                                 │ │
│ │      a) StripPrefix=3:                                              │ │
│ │         /ifit/api/v1/users/profile → /users/profile                 │ │
│ │                                                                       │ │
│ │      b) TokenRelay:                                                  │ │
│ │         Reenvía JWT original intacto en Authorization header         │ │
│ └─────────────────────────────────────┬────────────────────────────────┘ │
│                                       │                                   │
└──────────────────────────────────────┼───────────────────────────────────┘
                                       │
         ┌─────────────────────────────┴──────────────────────┐
         │ GET /users/profile                                 │
         │ Authorization: Bearer eyJhbGci...                  │
         │ (MISMO TOKEN)                                      │
         ↓                                                    │
┌──────────────────────────────────────────────────────────────────────────────┐
│ IFIT MICROSERVICE (8081) — Resource Server también                          │
│ ┌────────────────────────────────────────────────────────────────────────┐ │
│ │ 5.1. Recibe petición del gateway                                      │ │
│ │      GET /users/profile                                               │ │
│ │      Authorization: Bearer eyJhbGci...                                │ │
│ └─────────────────────────────────────┬────────────────────────────────┘ │
│                                       │                                   │
│ ┌───────────────────────────────────────────────────────────────────────┐ │
│ │ 5.2. VALIDA JWT DE NUEVO (Doble validación):                          │ │
│ │      Repite los pasos 4.3-4.6 (extrae, descarga JWKS, verifica,     │ │
│ │      valida claims)                                                   │ │
│ │                                                                       │ │
│ │      ✅ Si pasa → Continúa                                           │ │
│ │      ❌ Si falla → 401 Unauthorized                                  │ │
│ └─────────────────────────────────────┬────────────────────────────────┘ │
│                                       │                                   │
│ ┌───────────────────────────────────────────────────────────────────────┐ │
│ │ 5.3. Extrae usuario_id del claim 'sub':                              │ │
│ │      usuario_id = "f47ac10b-58cc-4372-a567-0e02b2c3d479"             │ │
│ └─────────────────────────────────────┬────────────────────────────────┘ │
│                                       │                                   │
│ ┌───────────────────────────────────────────────────────────────────────┐ │
│ │ 5.4. Aplica autorización (@PreAuthorize("hasRole('USER')")):         │ │
│ │      ¿El usuario tiene rol USER? → SÍ → Procesa petición             │ │
│ └─────────────────────────────────────┬────────────────────────────────┘ │
│                                       │                                   │
│ ┌───────────────────────────────────────────────────────────────────────┐ │
│ │ 5.5. Procesa la petición como del usuario Juan García:               │ │
│ │      SELECT * FROM users WHERE id = "f47ac10b-58cc-4372-a567..."    │ │
│ │      Devuelve datos del perfil                                        │ │
│ └─────────────────────────────────────┬────────────────────────────────┘ │
│                                       │                                   │
└──────────────────────────────────────┼───────────────────────────────────┘
                                       │
         ┌─────────────────────────────┴──────────────────────┐
         │ 6. Respuesta JSON                                  │
         ↓                                                    │
┌──────────────────────────────────────────────────────────────────────────────┐
│ CLIENTE MAUI                                                                 │
│ ┌────────────────────────────────────────────────────────────────────────┐ │
│ │ 6. Recibe datos del perfil:                                           │ │
│ │ {                                                                     │ │
│ │   "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",                      │ │
│ │   "name": "Juan García",                                              │ │
│ │   "email": "juan@example.com",                                        │ │
│ │   "age": 28                                                           │ │
│ │ }                                                                     │ │
│ └────────────────────────────────────────────────────────────────────────┘ │
│                                                                               │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## Configuración

Archivo: `src/main/resources/application.yaml`

```yaml
spring:
  application:
    name: ApiGatewayService
  main:
    web-application-type: reactive       # Obligatorio para Spring Cloud Gateway

  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:9090/realms/ifit-realm
          jwk-set-uri: http://localhost:9090/realms/ifit-realm/protocol/openid-connect/certs

  cloud:
    gateway:
      routes:
        - id: IFIT-PUBLIC
          uri: lb://IFIT
          predicates:
            - Path=/ifit/api/v1/auth/**, /ifit/api/v1/exercise-images/**
          filters:
            - StripPrefix=3

        - id: RONNIE
          uri: lb://RONNIE
          predicates:
            - Path=/ifit/api/v1/ronnie/**, /ifit/api/v1/serena/**,
                   /ifit/api/v1/kael/**, /ifit/api/v1/eliud/**,
                   /ifit/api/v1/messages/**
          filters:
            - StripPrefix=3
            - TokenRelay

        - id: IFIT-PRIVATE
          uri: lb://IFIT
          predicates:
            - Path=/ifit/api/v1/**
          filters:
            - StripPrefix=3
            - TokenRelay

server:
  port: 8080

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

El logging está configurado a nivel `TRACE` para todos los componentes relevantes (Spring Security, Spring Cloud Gateway, Reactor Netty, Eureka) durante el desarrollo, lo que facilita depurar problemas de enrutamiento y autenticación.

---

## Troubleshooting — ¿Qué ocurre si la validación falla?

| Escenario | Causa | Síntoma | Solución |
|---|---|---|---|
| **Sin token** | No incluyes header `Authorization` | `401 Unauthorized` | Incluir `Authorization: Bearer <token>` en la petición |
| **Token vencido** | El claim `exp` es menor que la hora actual | `401 Unauthorized` en el gateway | Hacer login de nuevo en Keycloak para obtener nuevo token |
| **Token manipulado** | Alguien modificó el payload del JWT | Firma no coincide → `401 Unauthorized` | No puedes hacerlo sin la clave privada de Keycloak |
| **Issuer incorrecto** | El claim `iss` no es `http://localhost:9090/realms/ifit-realm` | `401 Unauthorized` | Token de otro Keycloak — debe ser del realm correcto |
| **Keycloak no responde** | Keycloak no está corriendo en puerto 9090 | El gateway no puede descargar JWKS → Peticiones se quedan colgadas | Iniciar Keycloak: `docker-compose up keycloak` |
| **JWKS no cacheado** | El gateway acaba de arrancar y Keycloak no responde rápido | Primer login lento | Normal — esperar a que JWKS se cachee (ocurre una sola vez) |
| **Ruta pública sin token** | Intentas acceder a `/ifit/api/v1/auth/**` sin token | `200 OK` — permitAll() | Correcto, las rutas públicas no requieren token |
| **Ruta privada sin token** | Intentas acceder a `/ifit/api/v1/users/**` sin token | `401 Unauthorized` | Incluir token válido |

---

## Preguntas Frecuentes

### **P: ¿Por qué el gateway valida el token SI Keycloak ya lo validó?**
**R:** Doble validación (defensa en profundidad). Si un atacante logra manipular una petición en la red local, el gateway la rechaza. Además, el gateway cachea el JWKS, así que no depende de Keycloak en cada petición.

### **P: ¿El gateway necesita client-secret de Keycloak?**
**R:** No. El gateway es un **Resource Server**, no un **OAuth2 Client**. El client-secret se usa para que una aplicación obtenga nuevos tokens de Keycloak. El gateway recibe tokens ya emitidos y solo los valida.

### **P: ¿Qué es TokenRelay? ¿Por qué es importante?**
**R:** TokenRelay es un filtro que reenvía el JWT original intacto al microservicio destino. Sin él:
- El microservicio recibiría una petición sin token
- No podría extraer el usuario_id del claim 'sub'
- No sabría quién hizo la petición

### **P: ¿Quién firma el JWT, Keycloak o el gateway?**
**R:** **Keycloak** lo firma con su clave privada RSA. El gateway lo valida con la **clave pública** de Keycloak (descargada del JWKS).

### **P: ¿Si Keycloak cambia su clave privada, qué ocurre?**
**R:** El JWKS se actualiza automáticamente. El gateway descarga el JWKS periódicamente y cachea la clave pública. Los tokens antiguos dejarían de validarse (correcto, porque serían de un Keycloak antiguo).

---

## 🔐 Endpoints Protegidos por Rol

El Gateway valida tokens JWT y los microservicios aplican autorización basada en roles. Esta sección documenta qué endpoints están protegidos para administradores (usados por el Admin Panel) y cuáles están disponibles para usuarios regulares.

### **Endpoints Protegidos para Administradores (Admin Panel)**

Estos endpoints requieren:
- ✅ Token JWT válido en la cabecera `Authorization: Bearer <token>`
- ✅ Rol `admin_client_role` en el token (validado por los microservicios)

Si un usuario sin rol de administrador intenta acceder, recibe `403 Forbidden`.

#### **Gestión de Usuarios**

| Método | Endpoint | Descripción | Protección |
|---|---|---|---|
| GET | `/ifit/api/v1/users` | Listar todos los clientes | admin_client_role |
| POST | `/ifit/api/v1/auth/register` | Registrar nuevo cliente | Público (sin protección) |
| PUT | `/ifit/api/v1/users/{id}` | Actualizar datos del cliente | admin_client_role |
| DELETE | `/ifit/api/v1/users/{id}` | Eliminar cliente | admin_client_role |

#### **Gestión de Ejercicios**

| Método | Endpoint | Descripción | Protección |
|---|---|---|---|
| GET | `/ifit/api/v1/exercises?page=X&size=Y` | Listar ejercicios paginados | Token requerido |
| GET | `/ifit/api/v1/exercises/{id}` | Detalle completo de ejercicio | Token requerido |

**Nota:** Los ejercicios son **solo lectura**. La creación/edición de ejercicios se realiza directamente en la base de datos o a través de scripts de carga.

#### **Gestión de Entrenadores (Coaches)**

| Método | Endpoint | Descripción | Protección |
|---|---|---|---|
| GET | `/ifit/api/v1/coach-models/all` | Listar todos los coaches (incluidos deshabilitados) | admin_client_role |
| POST | `/ifit/api/v1/coach-models` | Crear nuevo coach | admin_client_role |
| PUT | `/ifit/api/v1/coach-models/{id}` | Actualizar coach | admin_client_role |
| DELETE | `/ifit/api/v1/coach-models/{id}` | Deshabilitar coach (soft delete) | admin_client_role |
| PATCH | `/ifit/api/v1/coach-models/{id}/enable` | Rehabilitar coach deshabilitado | admin_client_role |

#### **Gestión de Niveles de Experiencia**

| Método | Endpoint | Descripción | Protección |
|---|---|---|---|
| GET | `/ifit/api/v1/experience-levels` | Listar niveles de experiencia | admin_client_role |
| POST | `/ifit/api/v1/experience-levels` | Crear nuevo nivel | admin_client_role |
| PATCH | `/ifit/api/v1/experience-levels/{id}` | Actualizar descripción del nivel | admin_client_role |
| DELETE | `/ifit/api/v1/experience-levels/{id}` | Eliminar nivel de experiencia | admin_client_role |

#### **Gestión de Cuestionarios**

| Método | Endpoint | Descripción | Protección |
|---|---|---|---|
| GET | `/ifit/api/v1/questionnaires` | Listar todos los cuestionarios | admin_client_role |
| GET | `/ifit/api/v1/questionnaires/{id}` | Obtener cuestionario por ID | admin_client_role |
| POST | `/ifit/api/v1/questionnaires` | Crear cuestionario nuevo | admin_client_role |
| PUT | `/ifit/api/v1/questionnaires/{id}` | Actualizar cuestionario | admin_client_role |
| DELETE | `/ifit/api/v1/questionnaires/{id}` | Eliminar cuestionario | admin_client_role |

#### **Gestión de Rutinas (Admin)**

| Método | Endpoint | Descripción | Protección |
|---|---|---|---|
| GET | `/ifit/api/v1/routines/user/{userId}` | Listar rutinas de un cliente | Token requerido |
| PATCH | `/ifit/api/v1/routines/{id}/toggle-active?isActive=X` | Activar/desactivar rutina | admin_client_role |
| DELETE | `/ifit/api/v1/routines/{id}` | Borrar rutina | admin_client_role |

---

### **Endpoints Accesibles para Usuarios Regulares**

Todos los demás endpoints no mencionados anteriormente están disponibles para usuarios autenticados sin requerir rol de administrador. Esto incluye:

- **Autenticación**: Login, logout, refresh token, verificación de email
- **Perfil de usuario**: Obtener datos propios, actualizar perfil personal
- **Entrenamientos**: Generar rutinas, chat con coaches (Ronnie)
- **Historial**: Obtener historial de mensajes, rutinas propias
- **Catálogo**: Acceder a ejercicios (vista del usuario, no administración)
- **Cuestionarios**: Responder cuestionarios del onboarding

**Restricción principal**: Un usuario regular **solo puede acceder a sus propios datos**. El Gateway y los microservicios aplican control de acceso basado en el claim `sub` del JWT (ID del usuario autenticado).

---

### **¿Cómo se valida el rol?**

```
1. Cliente envía petición con JWT
   Authorization: Bearer eyJhbGciOiJSUzI1NiJ9...

2. Gateway valida firma del JWT contra Keycloak
   ✅ Firma válida → Continúa
   ❌ Firma inválida → 401 Unauthorized

3. Gateway reenvía JWT al microservicio (TokenRelay)

4. Microservicio lee el claim 'roles' del JWT
   Ejemplo: "roles": ["user", "admin_client_role"]

5. Microservicio verifica si el rol requerido está presente
   @PreAuthorize("hasRole('ADMIN_CLIENT_ROLE')")
   ✅ Presente → Procesa petición
   ❌ Ausente → 403 Forbidden

6. Si autorización pasa: microservicio procesa petición
```

---

## Puesta en Marcha

### Prerrequisitos

- Java 21+
- Maven 3.9+
- Keycloak corriendo en `localhost:9090` con realm `ifit-realm`
- Eureka Server corriendo en `localhost:8761`

### Compilar y arrancar

```bash
cd api-gateway
mvn clean package -DskipTests
mvn spring-boot:run
```

El gateway estará disponible en `http://localhost:8080`.

### Orden de arranque recomendado

```
1. MySQL          → localhost:3306
2. Keycloak       → localhost:9090
3. Eureka Server  → localhost:8761
4. API Gateway    → localhost:8080
5. IFit           → localhost:8081
6. Ronnie         → localhost:8082
```

El gateway puede arrancar antes que IFit y Ronnie (Eureka actualizará el registro cuando estén disponibles), pero Keycloak y Eureka deben estar activos antes de que el gateway inicie.

---

## Apéndice A: Criptografía RSA — ¿Cómo se valida la firma?

Para entender completamente cómo funciona la validación de tokens, es útil conocer los conceptos básicos de RSA:

### **¿Qué es RSA?**

RSA es un algoritmo de **criptografía asimétrica** que usa **dos claves** matemáticamente relacionadas:

```
┌────────────────────────────────────┐
│  Clave Privada (Private Key)       │
│  ├─ Solo Keycloak la tiene         │
│  ├─ Se usa para FIRMAR tokens      │
│  └─ Nunca se comparte              │
└────────────────────────────────────┘

        ↕ Matemáticamente relacionadas

┌────────────────────────────────────┐
│  Clave Pública (Public Key)        │
│  ├─ Publicada en JWKS              │
│  ├─ Se usa para VERIFICAR firmas   │
│  └─ Cualquiera puede descargarla   │
└────────────────────────────────────┘
```

### **Proceso de firma (Keycloak)**

1. Keycloak tiene el header y payload del JWT:
   ```
   header.payload = "eyJhbGciOi...JXVzZXIifQ"
   ```

2. Keycloak aplica un hash SHA256:
   ```
   hash = SHA256("eyJhbGciOi...JXVzZXIifQ")
   hash = "a3f7d8e2c1b4f6..."
   ```

3. Keycloak cifra el hash con su **clave privada**:
   ```
   signature = RSA_encrypt(hash, private_key)
   signature = "xjlCRBqkQr2..."
   ```

4. El JWT completo es:
   ```
   JWT = header.payload.signature
   JWT = "eyJhbGciOi...JXVzZXIifQ.xjlCRBqkQr2..."
   ```

### **Proceso de verificación (Gateway)**

1. El gateway recibe el JWT:
   ```
   JWT = "eyJhbGciOi...JXVzZXIifQ.xjlCRBqkQr2..."
   ```

2. El gateway divide el JWT:
   ```
   header = "eyJhbGciOi..."
   payload = "JXVzZXIifQ"
   signature_recibida = "xjlCRBqkQr2..."
   ```

3. El gateway descarga la **clave pública** de Keycloak (del JWKS):
   ```json
   {
     "kty": "RSA",
     "n": "xjlCRBqkQr...",  // Módulo N (parte de la clave pública)
     "e": "AQAB"             // Exponente público
   }
   ```

4. El gateway calcula el hash del header.payload:
   ```
   hash_calculado = SHA256("eyJhbGciOi...JXVzZXIifQ")
   hash_calculado = "a3f7d8e2c1b4f6..."
   ```

5. El gateway desencripta la firma con la **clave pública**:
   ```
   hash_extraido = RSA_decrypt(signature_recibida, public_key)
   hash_extraido = "a3f7d8e2c1b4f6..."
   ```

6. El gateway compara:
   ```
   if (hash_calculado == hash_extraido) {
     ✅ Firma válida → Token auténtico (creado por Keycloak)
   } else {
     ❌ Firma inválida → Token manipulado o de otra fuente
   }
   ```

### **¿Por qué es seguro?**

- **No se puede falsificar sin clave privada**: La clave privada de Keycloak solo la tiene Keycloak. Sin ella, es matemáticamente imposible generar una firma válida que coincida con la clave pública.
- **La clave pública es inútil para firmar**: Aunque cualquiera descargue la clave pública, solo se puede usar para verificar firmas, no para crearlas.
- **No se puede manipular el token**: Si cambias un bit del payload, el hash es completamente diferente y la firma no coincidirá.

### **Visualmente**

```
Keycloak (Private Key)        Gateway (Public Key)
┌──────────────────────┐      ┌──────────────────────┐
│ eyJhbGc....         │      │ eyJhbGc....          │
│ → SHA256()          │      │ → SHA256()           │
│ → a3f7d8e2...       │      │ → a3f7d8e2...        │
│ → RSA_sign()        │      │ (Recibido)           │
│ → signature: xyz... │  →   │ → RSA_verify()       │
│                     │      │ → ¿Coincide? ✅ SÍ   │
└──────────────────────┘      └──────────────────────┘
        Emite JWT                   Valida JWT
```

---

## Apéndice B: Configuración Detallada por Archivo

### **SecurityConfig.java**

```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        http
            // 1. Define qué rutas son públicas y cuáles privadas
            .authorizeExchange(exchange -> exchange
                .pathMatchers("/ifit/api/v1/auth/**").permitAll()
                .pathMatchers("/ifit/api/v1/exercise-images/**").permitAll()
                .anyExchange().authenticated()
            )
            // 2. Activa validación JWT contra Keycloak
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}))
            // 3. Desactiva CSRF (no es necesario para APIs REST con JWT)
            .csrf(csrf -> csrf.disable());
        
        return http.build();
    }
}
```

**¿Qué hace `oauth2ResourceServer()`?**
- Activa Spring Security como **Resource Server** (no como Client)
- Automáticamente:
  - Lee `issuer-uri` y `jwk-set-uri` de `application.yaml`
  - Descarga el JWKS de Keycloak
  - Valida cada JWT contra la clave pública
  - Extrae claims y los pone en el contexto de Spring Security

### **application.yaml**

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:9090/realms/ifit-realm
          jwk-set-uri: http://localhost:9090/realms/ifit-realm/protocol/openid-connect/certs
```

**¿Qué hace cada línea?**

| Línea | Propósito |
|---|---|
| `issuer-uri` | URL del Authorization Server (Keycloak). Spring Security verifica que el claim `iss` del JWT coincida con este valor. |
| `jwk-set-uri` | URL donde descargar la clave pública (JWKS). Spring Security la cachea y la usa para verificar firmas. |

---

---

## OpenID Connect (OIDC) — ¿Qué es y por qué tu proyecto lo usa?

### **¿Cuál es la diferencia entre OAuth2 y OpenID Connect?**

Esta es una confusión común. Vamos a aclararla:

| Aspecto | OAuth2 | OpenID Connect |
|---|---|---|
| **Propósito** | **AUTORIZACIÓN** — ¿Qué puede hacer el usuario? | **AUTENTICACIÓN** — ¿Quién es el usuario? |
| **Se enfoca en** | Otorgar permisos (scopes) | Verificar identidad |
| **Qué devuelve** | `access_token` (solo para acceso) | `access_token` + `id_token` (con identidad) |
| **Estándar** | Open Authorization 2.0 | Capa sobre OAuth2 |
| **¿Autentica?** | NO (no verifica identidad) | SÍ (verifica quién eres) |

### **Ejemplo Simple**

**Escenario: Acceder a la app iFit**

```
SIN OpenID Connect (solo OAuth2):
- Keycloak verifica credenciales
- Emite un access_token que dice "puedes acceder a recursos"
- Pero NO te dice quién eres realmente
- ❌ El Gateway no sabe el nombre, email, ID del usuario

CON OpenID Connect:
- Keycloak verifica credenciales
- Emite un access_token + id_token
- El id_token contiene: nombre, email, ID, foto, etc.
- ✅ El Gateway sabe exactamente quién eres
```

### **En tu proyecto: iFit usa OpenID Connect**

Cuando un usuario inicia sesión en iFit:

```
1. Usuario ingresa email/contraseña en Keycloak
   
2. Keycloak responde con:
   - access_token: para acceder a recursos
   - id_token: contiene identidad del usuario
   - refresh_token: para renovar tokens
   
3. El id_token contiene claims como:
   {
     "iss": "http://localhost:9090/realms/ifit-realm",
     "sub": "f47ac10b-58cc-4372-a567-0e02b2c3d479",  ← ID del usuario
     "name": "Juan García",                            ← Nombre
     "email": "juan@example.com",                      ← Email
     "picture": "...",                                 ← Foto
     "given_name": "Juan",
     "family_name": "García",
     "exp": 1719014400,
     "iat": 1719010800
   }
```

### **¿Por qué es importante OpenID Connect en tu proyecto?**

El Gateway y los microservicios necesitan **saber quién es el usuario** para:
- ✅ Registrar en logs quién hizo qué (auditoría)
- ✅ Aplicar autorización (¿tiene rol admin?)
- ✅ Asignar recursos al usuario correcto (solo tu rutina, no la de otros)
- ✅ Personalizar la experiencia (mostrar "Bienvenido, Juan")

Sin OpenID Connect, solo tendrían un token sin saber quién es el usuario.

### **Configuración en tu proyecto**

En `api-gateway/src/main/resources/application.yaml`, tienes:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:9090/realms/ifit-realm
          jwk-set-uri: http://localhost:9090/realms/ifit-realm/protocol/openid-connect/certs
```

La URL contiene `/openid-connect/certs` → **Confirmación de que usas OpenID Connect**.

Keycloak emite JWT que contienen tanto `access_token` (para autorización) como `id_token` (para identificar al usuario) — esto es **OpenID Connect**.

---

## 📚 Apéndices

Esta documentación incluye los siguientes apéndices:

- **Apéndice A**: Criptografía RSA (más abajo)
- **Apéndice B**: Configuración Detallada (más abajo)
- **Apéndice C**: Glosario OAuth2 y Conceptos Fundamentales (más abajo)
- **Apéndice D**: Resource Server, WebFlux y Decisión Arquitectónica (más abajo)

---

## Apéndice C: 🔐 Glosario OAuth2 y Conceptos Fundamentales

OAuth2 es un **estándar abierto** para autorización (no autenticación). Define roles y flujos para que usuarios autoricen que aplicaciones accedan a sus datos sin compartir contraseñas.

### **Términos Clave de OAuth2**

#### **1. Authorization Server (Servidor de Autorización)**

**¿Qué es?**
- El servidor que **emite tokens** tras verificar credenciales del usuario
- En tu proyecto: **Keycloak**
- Es como el **guardia de seguridad de un edificio**: verifica tu identidad y te da un pase (token)

**¿Cuál es su rol en el flujo?**

```
Usuario                Authorization Server          Resource Server
 │                            │                             │
 ├─ Email + Contraseña ─────►│                             │
 │                            │                             │
 │                            ├─ Verifica credenciales      │
 │                            │                             │
 │◄─ Token JWT Firmado ────────┤                            │
 │                            │                             │
 │──── Petición con Token ────────────────────────────────►│
 │                            │                             │
 │                            │                             │
 │                            │◄─── Valida Token ──────┐   │
 │                            │                        │   │
 │                            ├─ ¿Es válido? ◄────────┘   │
 │                            │   ✅ SÍ → Acceso permitido
 │                            │   ❌ NO → 401 Unauthorized
 │                            │                             │
 │◄─────────────── Recurso Accedido ─────────────────────┤
```

**Responsabilidades:**
- ✅ Autentica usuarios (verifica usuario/contraseña)
- ✅ Emite tokens JWT firmados con **su clave privada RSA**
- ✅ Valida solicitudes de token
- ✅ Publica claves públicas (JWKS) para que otros verifiquen sus tokens
- ✅ Gestiona usuarios, roles y permisos
- ✅ Revoca tokens si es necesario

**¿Por qué existe el Authorization Server?**

Sin Authorization Server, cada microservicio tendría que:
- Almacenar contraseñas (peligroso)
- Implementar su propio sistema de autenticación (duplicado)
- Mantener sincronizados los usuarios en múltiples bases de datos

Con Authorization Server centralizado:
- ✅ Un único lugar seguro para almacenar credenciales
- ✅ Los microservicios **solo validan tokens**, no manejan contraseñas
- ✅ Los usuarios se registran una sola vez
- ✅ Cambiar contraseña afecta a todo el sistema

---

#### **2. Resource Server (Servidor de Recursos)**

**¿Qué es?**
- El servidor que **protege recursos** verificando que el token es válido
- En tu proyecto: **API Gateway, IFit, Ronnie**

**Responsabilidades:**
- ✅ Recibe peticiones con tokens JWT
- ✅ Valida que el token fue emitido por un Authorization Server confiable
- ✅ Verifica que el token no está vencido
- ✅ Extrae información del usuario del token
- ✅ Otorga acceso a recursos si el token es válido

---

#### **3. Client (Cliente)**

**¿Qué es?**
- La aplicación que quiere acceder a recursos en nombre del usuario
- En tu proyecto: **App MAUI (.NET)** y **Admin Panel (Vaadin)**

**Responsabilidades:**
- ✅ Obtiene tokens del Authorization Server (Keycloak)
- ✅ Almacena el token (en memoria / sesión web)
- ✅ Envía el token en cada petición a Resources Servers
- ✅ Maneja tokens vencidos (refresh token)

**Nota:** El Gateway **NO es un Client**. El Gateway es un Resource Server que solo valida tokens, no los obtiene.

---

#### **4. Resource Owner (Propietario del Recurso)**

**¿Qué es?**
- El usuario propietario de los datos

**Responsabilidades:**
- ✅ Autoriza que el cliente acceda a sus datos
- ✅ Proporciona credenciales al Authorization Server

---

#### **5. Access Token (Token de Acceso)**

**¿Qué es?**
- Una credencial que representa la autorización del usuario
- En tu proyecto: **JWT firmado por Keycloak**

**Características:**
- ✅ Firmado criptográficamente (no se puede falsificar)
- ✅ Contiene información del usuario (claims)
- ✅ Tiene fecha de expiración
- ✅ Se envía en cada petición protegida

---

#### **6. JWT (JSON Web Token)**

**¿Qué es?**
- Un formato estándar para representar tokens de acceso
- Estructura: `header.payload.signature`

**Partes:**

```
Header (algoritmo de firma):
{
  "alg": "RS256",
  "typ": "JWT"
}

Payload (datos del usuario):
{
  "iss": "http://localhost:9090/realms/ifit-realm",
  "sub": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "name": "Juan García",
  "email": "juan@example.com",
  "exp": 1719014400,
  "iat": 1719010800
}

Signature (firma criptográfica):
HMACSHA256(header.payload, secret_key)
```

---

#### **7. JWKS (JSON Web Key Set)**

**¿Qué es?**
- Un conjunto de claves públicas publicadas por el Authorization Server
- Se usa para verificar firmas de JWT

**Ubicación en tu proyecto:**
```
http://localhost:9090/realms/ifit-realm/protocol/openid-connect/certs
```

---

#### **8. Claims (Reclamaciones)**

**¿Qué es?**
- Información dentro del JWT que hace afirmaciones sobre el usuario

**Claims estándar en OAuth2/OpenID Connect:**

| Claim | Significa | Ejemplo |
|---|---|---|
| `iss` | Issuer (quién emitió el token) | `http://localhost:9090/realms/ifit-realm` |
| `sub` | Subject (ID del usuario) | `f47ac10b-58cc-4372-a567-0e02b2c3d479` |
| `aud` | Audience (para quién es el token) | `ifit-app` |
| `exp` | Expiration time (timestamp de expiración) | `1719014400` |
| `iat` | Issued at (timestamp de emisión) | `1719010800` |
| `name` | Nombre del usuario | `Juan García` |
| `email` | Email del usuario | `juan@example.com` |

---

#### **9. Scope (Alcance)**

**¿Qué es?**
- Permisos específicos que el usuario autoriza al cliente

**Ejemplo en Keycloak:**
```
Scopes: openid profile email
```

Significado:
- `openid`: Acceso básico (OpenID Connect)
- `profile`: Acceso a datos del perfil (name, picture, etc.)
- `email`: Acceso al email del usuario

---

#### **10. Grant Type (Tipo de Autorización)**

**¿Qué es?**
- El método específico para obtener un access token

**Tipos comunes:**

| Tipo | Uso | Flujo |
|---|---|---|
| `authorization_code` | Apps web y móviles | Usuario inicia sesión → Código → Token |
| `client_credentials` | Servicio a servicio | Cliente autentica con credenciales → Token |
| `refresh_token` | Renovar access token expirado | Token viejo → Refresh token → Token nuevo |
| `implicit` | Apps JavaScript antiguas | Obsoleto (deprecated) |
| `password` | Aplicaciones de confianza | Usuario proporciona credenciales directamente |

---

### **Visión Integrada: Cómo Todo Funciona Junto en iFit**

Para terminar el Apéndice C, aquí está cómo todos estos conceptos trabajan juntos en tu proyecto real:

#### **Participantes en el Sistema**

```
┌──────────────────────────────────────────────────────────────────────┐
│                         FLUJO COMPLETO EN IFIT                       │
└──────────────────────────────────────────────────────────────────────┘

1. USUARIO (Resource Owner)
   └─ Propietario de los datos (nombre, perfil, rutinas, etc.)

2. KEYCLOAK (Authorization Server)
   └─ Autentica al usuario
   └─ Emite JWT con identidad (OpenID Connect)
   └─ Publica clave pública (JWKS) para que otros validen tokens

3. APP MAUI (Client)
   └─ Solicita acceso a recursos en nombre del usuario
   └─ Obtiene tokens de Keycloak
   └─ Envía tokens con cada petición

4. API GATEWAY (Resource Server)
   └─ Recibe peticiones con tokens
   └─ Valida que el token es válido (descargando clave pública de Keycloak)
   └─ Extrae identidad del usuario del token (claim 'sub')
   └─ Reenvía el token intacto a los microservicios

5. IFIT + RONNIE (Resource Servers también)
   └─ Reciben peticiones del Gateway con token
   └─ Validan el token NUEVAMENTE (defensa en profundidad)
   └─ Extraen identidad del usuario (claim 'sub')
   └─ Aplican autorización (¿tiene rol admin?)
   └─ Procesan la petición y devuelven recurso
```

#### **Secuencia paso a paso**

```
INICIO: Usuario abre la app MAUI

1️⃣  APP MAUI → KEYCLOAK
    POST /token
    {
      "client_id": "maui-app",
      "client_secret": "secret",
      "username": "juan@example.com",
      "password": "password123"
    }

2️⃣  KEYCLOAK → APP MAUI (respuesta)
    {
      "access_token": "eyJhbGci...",  ← Token firmado con clave privada de Keycloak
      "id_token": "eyJhbGci...",       ← Contiene identidad
      "refresh_token": "eyJhbGci...",
      "expires_in": 300
    }

3️⃣  APP MAUI guarda el token en memoria

4️⃣  Usuario abre una rutina → APP MAUI → API GATEWAY
    GET /ifit/api/v1/routines/123
    Authorization: Bearer eyJhbGci...  ← Envía el access_token

5️⃣  API GATEWAY (valida el token)
    - Extrae JWT del header Authorization
    - Descarga clave pública de Keycloak (JWKS)
    - Verifica firma del JWT: ¿fue firmado con la clave privada de Keycloak?
      ✅ SÍ → Token legítimo
      ❌ NO → 401 Unauthorized
    - Decodifica el token y extrae claims:
      {
        "iss": "http://localhost:9090/realms/ifit-realm",
        "sub": "f47ac10b-58cc-4372-a567-0e02b2c3d479",  ← ID del usuario
        "name": "Juan García",
        "email": "juan@example.com",
        "roles": ["user_client_role"],
        "exp": 1719014400
      }
    - El usuario es válido, continúa
    - Aplica StripPrefix=3: /ifit/api/v1/routines/123 → /routines/123
    - Aplica TokenRelay: incluye el token en la cabecera al enviar a IFIT

6️⃣  API GATEWAY → IFIT
    GET /routines/123
    Authorization: Bearer eyJhbGci...  ← MISMO token

7️⃣  IFIT (valida el token NUEVAMENTE)
    - Repite los mismos pasos de validación que el Gateway
    - Verifica firma contra JWKS de Keycloak
    - Decodifica y extrae claims
    - Obtiene el ID del usuario: "f47ac10b-58cc-4372-a567-0e02b2c3d479"
    - Aplica autorización:
      @PreAuthorize("hasRole('USER')")  ← ¿El usuario tiene rol USER?
      ✅ SÍ (tiene "user_client_role" en el token)
    - Busca rutina:
      SELECT * FROM routine WHERE id = 123 AND user_id = 'f47ac10b-...'
      ✅ La rutina pertenece a Juan
    - Devuelve la rutina

8️⃣  IFIT → API GATEWAY → APP MAUI
    {
      "id": 123,
      "name": "Push-Pull-Legs",
      "days": [...]
    }

9️⃣  APP MAUI muestra "Bienvenido, Juan" + rutina
```

#### **¿Por qué esta arquitectura es segura?**

```
Atacante intentó falsificar un JWT:

1. Modifica el payload: "user_id": "otro_usuario_id"
2. El Gateway lo recibe
3. Verifica la firma:
   ❌ FALLA — La firma no coincide (porque no tiene clave privada de Keycloak)
4. Rechaza la petición: 401 Unauthorized

Conclusión: Sin la clave privada de Keycloak, es imposible falsificar un token.
```

---

## Apéndice D: 🛡️ Resource Server, WebFlux y Decisión Arquitectónica

### **¿Qué es un Resource Server?**

Un **Resource Server** es un tipo específico de servidor en la arquitectura OAuth2. Aquí está la definición completa:

#### **Definición**

Un Resource Server es un servidor que:

1. **Recibe peticiones HTTP** con un JWT en la cabecera `Authorization`
2. **Valida el JWT** verificando:
   - Que fue emitido por un Authorization Server confiable (Keycloak)
   - Que la firma es válida
   - Que no ha expirado
3. **Extrae información del usuario** del JWT (del claim `sub`)
4. **Otorga acceso** a recursos si el token es válido
5. **Rechaza acceso** (401 Unauthorized) si el token es inválido

#### **¿Qué NO hace?**

- ❌ **NO emite tokens** (eso lo hace el Authorization Server)
- ❌ **NO autentica usuarios** directamente (eso lo hace el Authorization Server)
- ❌ **NO intercambia credenciales** con el Authorization Server
- ❌ **NO necesita `client-secret`** de Keycloak

#### **¿Por qué tu Gateway es un Resource Server y no un Client?**

Un **Client** sería si el Gateway necesitara:
- Obtener tokens de Keycloak
- Usar `client-id` y `client-secret`
- Intercambiar código por token

Pero el Gateway:
- **Recibe tokens** que el cliente ya obtuvo
- **Solo valida** que esos tokens son válidos
- **No obtiene nuevos tokens**

Por eso es un **Resource Server**, no un **Client**.

---

### **⚡ ¿Por qué WebFlux en el Gateway?**

Esta es una decisión arquitectónica crucial. Para entenderla, primero necesitas saber la diferencia entre dos modelos de concurrencia.

#### **Modelo Tradicional: Spring Boot Web (Servlet)**

**¿Cómo funciona?**

```
Petición 1 ──→ [ Hilo 1 ] ──→ Procesa → Respuesta 1
Petición 2 ──→ [ Hilo 2 ] ──→ Procesa → Respuesta 2
Petición 3 ──→ [ Hilo 3 ] ──→ Procesa → Respuesta 3
Petición 4 ──→ [ Hilo 4 ] ──→ Procesa → Respuesta 4
(Pool de hilos: máximo 200 hilos)
```

**Características:**
- ✅ Cada petición usa **un hilo dedicado**
- ✅ El hilo se bloquea mientras espera I/O (base de datos, HTTP, etc.)
- ✅ El hilo se libera cuando la respuesta se envía
- ❌ El número de peticiones concurrentes está limitado por el número de hilos

**Problema del Gateway con Servlet:**

```
Cliente MAUI      Cliente MAUI      Cliente MAUI      Cliente MAUI
     │                 │                 │                 │
     └─────────────────┴─────────────────┴─────────────────┘
                       │
         ┌─────────────────────────────┐
         │   API GATEWAY (Servlet)      │
         │                              │
         │  Hilo 1: petición → IFIT     │  (bloqueado esperando respuesta)
         │  Hilo 2: petición → Ronnie   │  (bloqueado esperando respuesta)
         │  Hilo 3: petición → IFIT     │  (bloqueado esperando respuesta)
         │  ...                         │
         │  Hilo 200: petición → Ronnie │  (bloqueado esperando respuesta)
         │                              │
         │  ⚠️ Petición 201 llega pero  │
         │     NO HAY HILO DISPONIBLE   │
         │  → ENCOLA (espera)           │
         │                              │
         └─────────────────────────────┘
```

---

#### **Modelo Reactivo: Spring Cloud Gateway (WebFlux)**

**¿Cómo funciona?**

```
Petición 1 ──┐
Petición 2 ──┤
Petición 3 ──┼──→ [ Pocos Hilos (ej: 4) ] ──→ Procesan juntas → Respuestas
Petición 4 ──┤    (usando Reactor y Netty)
Petición 5 ──┤
...          │
Petición 10000 ┘
```

**Características:**
- ✅ Usa **muy pocos hilos** (típicamente 4-8, uno por CPU core)
- ✅ Los hilos **nunca se bloquean**
- ✅ Mientras un hilo espera I/O, puede procesar otras peticiones
- ✅ Puede manejar **miles de peticiones concurrentes** con pocos hilos

**Ventaja del Gateway con WebFlux:**

```
Cliente MAUI      Cliente MAUI      Cliente MAUI      Cliente MAUI
Cliente MAUI      Cliente MAUI      Cliente MAUI      Cliente MAUI
(1000 clientes)                                        ...
     │                 │                 │                 │
     └─────────────────┴─────────────────┴─────────────────┘
                       │
         ┌──────────────────────────────────────────┐
         │  API GATEWAY (WebFlux)                   │
         │                                          │
         │  Hilo 1: P1→IFIT  P2→Ronnie P3→IFIT ... │
         │  Hilo 2: P4→IFIT  P5→Ronnie P6→IFIT ... │
         │  Hilo 3: P7→IFIT  P8→Ronnie P9→IFIT ... │
         │  Hilo 4: P10→IFIT P11→Ronnie ...        │
         │                                          │
         │  ✅ 1000 peticiones concurrentes sin    │
         │     encolar, usando solo 4 hilos        │
         │                                          │
         └──────────────────────────────────────────┘
```

Mientras un hilo espera respuesta de IFIT, puede procesar otras peticiones a Ronnie o a IFit sin bloquearse.

---

#### **Comparativa: Spring Boot Web vs WebFlux**

| Característica | Spring Boot Web (Servlet) | Spring Cloud Gateway (WebFlux) |
|---|---|---|
| **Tecnología base** | Tomcat (servlet blocking) | Netty (event-driven) |
| **Modelo de concurrencia** | 1 hilo por petición | N hilos para M peticiones (M >> N) |
| **Hilos tipicos** | 100-200 | 4-8 |
| **¿Se bloquea esperando I/O?** | ✅ SÍ (el hilo se bloquea) | ❌ NO (el hilo sigue activo) |
| **Peticiones concurrentes máx** | ~200 (limitado por hilos) | ~10,000+ |
| **Memoria por hilo** | ~1 MB (stack del hilo) | <100 KB (coroutine) |
| **Uso de memoria total** | Alto (200 hilos × 1 MB) | Bajo (4 hilos × <100 KB) |
| **Latencia con mucho tráfico** | ⬆️ Sube (hay que esperar hilo libre) | ➡️ Estable (maneja igual) |
| **Configuración de seguridad** | `@EnableWebSecurity` + `ServerHttpSecurity` | `@EnableWebFluxSecurity` + `ServerHttpSecurity` |
| **Adecuado para** | Aplicaciones tradicionales | Gateways, APIs reactivas |
| **Complejidad** | Media | Media-Alta (requiere entender streams) |
| **Debugging** | Más fácil (stack traces lineales) | Más difícil (stack traces reactivos) |

---

### **🏗️ Decisión Arquitectónica Final**

#### **¿Por qué el Gateway usa WebFlux y los microservicios usan Servlet?**

| Componente | Tecnología | Razón |
|---|---|---|
| **Gateway** | WebFlux | Es un **cuello de botella**: todas las peticiones pasan por él. Con mucho tráfico, necesita manejar miles de conexiones simultáneamente. |
| **IFIT** | Servlet (Spring Boot Web) | Es un **microservicio**: procesa lógica específica (usuarios, cuestionarios, rutinas). No necesita manejar miles de conexiones simultáneamente. |
| **Ronnie** | Servlet (Spring Boot Web) | Es un **microservicio**: procesa lógica de IA y coaches. No necesita manejar miles de conexiones simultáneamente. |

#### **Analógía útil**

```
Servidor de correo (Servlet):
- Procesa emails de forma secuencial
- Es eficiente para la tarea específica
- No necesita manejar múltiples tareas al mismo tiempo

Recepcionista en un hotel (WebFlux):
- Recibe a múltiples clientes simultáneamente
- Mientras uno espera registrarse, atiende a otro
- Si necesitara 1 recepcionista por cliente, el hotel colapsaría
```

El **Gateway es como una recepcionista** que debe atender a muchos clientes rápidamente. Los **microservicios son como especialistas** que procesan una tarea específica.

#### **¿Qué pasa si el Gateway fuera Servlet?**

**Escenario real:**

```
Hora punta:
- 500 usuarios de la app MAUI realizan peticiones simultáneamente
- El Gateway (Servlet) tiene 200 hilos máximo
- Pool de hilos: 200 hilos OCUPADOS
- Petición 201 llega → ENCOLA
- Petición 202 llega → ENCOLA
- Petición 203 llega → ENCOLA
- ...
- Petición 500 llega → ENCOLA

Resultado:
- Usuarios esperan 10-30 segundos por respuesta
- Memoria se agota (200 hilos × 1 MB cada uno = 200 MB solo en hilos)
- El sistema se vuelve lento
- Usuarios reportan "el servidor está lento"
```

**Con WebFlux:**

```
Hora punta:
- 500 usuarios de la app MAUI realizan peticiones simultáneamente
- El Gateway (WebFlux) tiene 4-8 hilos
- Todos procesan sin bloquearse
- Mientras uno espera respuesta de IFIT, procesa otra petición
- Resultado: respuesta en < 100 ms

Memoria: solo 4 hilos × <100 KB = < 400 KB
```

---

## Autor

**Juan García Candón**  
Universidad de Cádiz — Escuela Superior de Ingeniería  
Trabajo Final de Grado (TFG), 2024–2025
