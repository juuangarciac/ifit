# API Gateway — Punto de Entrada Único

[![Spring Cloud Gateway](https://img.shields.io/badge/Spring%20Cloud%20Gateway-2025.0.0-brightgreen.svg)](https://spring.io/projects/spring-cloud-gateway)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/)
[![WebFlux](https://img.shields.io/badge/Reactive-WebFlux-orange.svg)](https://docs.spring.io/spring-framework/reference/web/webflux.html)

> Único punto de entrada al sistema iFit. Valida tokens JWT, enruta peticiones a los microservicios internos y aplica el filtro TokenRelay para propagar la identidad del usuario.

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
- [Service Discovery (Eureka)](#service-discovery-eureka)
- [Configuración](#configuración)
- [Puesta en Marcha](#puesta-en-marcha)

---

## Descripción General

El API Gateway es el servicio que recibe **todas** las peticiones del cliente (app .NET MAUI) y las distribuye hacia los microservicios internos. Ningún microservicio interno es accesible directamente desde el exterior; el gateway actúa como frontera de seguridad y punto de control único.

Sus responsabilidades son:

1. **Validar el JWT** antes de que la petición llegue a cualquier servicio interno.
2. **Enrutar** hacia `IFIT` (Puerto 8081) o `RONNIE` (Puerto 8082) según el path.
3. **Aplicar StripPrefix** para eliminar el prefijo de ruta antes de reenviar.
4. **Propagar el token** (`TokenRelay`) a los microservicios que lo necesitan.
5. **Exponer rutas públicas** sin autenticación para registro y login.

---

## Por qué un API Gateway

Sin gateway, el cliente necesitaría conocer las URLs y puertos de cada microservicio, gestionar la autenticación individualmente con cada uno y manejar cambios de infraestructura. Con el gateway:

- El cliente siempre habla con `http://localhost:8080/ifit/api/v1/...` independientemente de cuántos servicios haya internamente.
- La validación de seguridad centralizada evita duplicar lógica de autenticación en cada servicio.
- Los microservicios pueden moverse, escalarse o añadirse sin que el cliente lo perciba.

---

## Stack Tecnológico

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot + Spring Cloud 2025.0.0 |
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

### Doble validación JWT

El JWT se valida **dos veces** en el sistema:

```
1. En el Gateway (Spring Cloud Gateway + SecurityConfig)
   - Verifica firma RS256 contra el JWKS de Keycloak
   - Verifica iss, exp, iat
   - Bloquea peticiones con token inválido o ausente (excepto rutas públicas)
   - Si pasa: reenvía la petición con TokenRelay

2. En el microservicio destino (IFit o Ronnie)
   - IFit: SpringSecurityConfig como OAuth2 Resource Server
   - Ronnie: JwtUtils.extractUserId() para leer el claim 'sub'
   - Cada servicio puede aplicar sus propias reglas de autorización (@PreAuthorize)
```

Esta arquitectura de defensa en profundidad garantiza que aunque un atacante bypasease el gateway, los servicios internos seguirían validando el token por su cuenta.

---

## Seguridad

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

## Autor

**Juan García Candón**  
Universidad de Cádiz — Escuela Superior de Ingeniería  
Trabajo Final de Grado (TFG), 2024–2025
