# Eureka Server — Registro y Descubrimiento de Servicios

[![Spring Cloud Netflix Eureka](https://img.shields.io/badge/Spring%20Cloud%20Netflix-Eureka%20Server-brightgreen.svg)](https://spring.io/projects/spring-cloud-netflix)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-green.svg)](https://spring.io/projects/spring-boot)

> Servidor de descubrimiento del sistema iFit. Mantiene el registro en tiempo real de todos los microservicios (IFit, Ronnie, API Gateway), permitiendo que se localicen entre sí por nombre lógico en lugar de por IP y puerto fijos.

> [!NOTE]
> **Capa de Documentación: Núcleo de Infraestructura (Nivel Interno)**  
> Este documento detalla una pieza de infraestructura específica de la red interna de microservicios. Para la vista general y orquestación del sistema completo, consulta el [README.md del proyecto general](../../README.md).

---

## Tabla de Contenidos

- [Descripción General](#descripción-general)
- [Por qué un servidor de descubrimiento](#por-qué-un-servidor-de-descubrimiento)
- [Stack Tecnológico](#stack-tecnológico)
- [Configuración](#configuración)
- [Quién se registra](#quién-se-registra)
- [Puesta en Marcha](#puesta-en-marcha)
- [Autor](#autor)

---

## Descripción General

Eureka Server es el **registro central** del sistema. Cada microservicio, al arrancar, se da de alta aquí indicando su nombre (`spring.application.name`), su IP y su puerto. El resto de servicios consulta este registro para resolver destinos mediante el esquema `lb://NOMBRE` sin necesidad de conocer direcciones físicas.

Se activa con una única anotación:

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

El dashboard web queda disponible en `http://localhost:8761`, donde puede verse en tiempo real qué instancias están registradas, sus IPs, puertos y estado de salud.

---

## Por qué un servidor de descubrimiento

Sin descubrimiento de servicios, cada componente necesitaría tener cableadas las URLs y puertos de los demás. Con Eureka:

- Los servicios se localizan por **nombre lógico** (`IFIT`, `RONNIE`), no por IP.
- El **API Gateway** resuelve `lb://IFIT` y `lb://RONNIE` consultando este registro.
- Se puede **escalar** a varias instancias del mismo servicio: Eureka devuelve todas y el llamante balancea.
- Las instancias caídas se marcan como no disponibles y dejan de recibir tráfico.

---

## Stack Tecnológico

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.5.7 + Spring Cloud 2025.0.0 |
| Descubrimiento | Spring Cloud Netflix Eureka Server |
| Puerto | 8761 |

Es el módulo más liviano del sistema: no tiene base de datos, ni seguridad, ni dependencias de negocio. Solo el starter `spring-cloud-starter-netflix-eureka-server`.

---

## Configuración

Archivo: `src/main/resources/application.yml`

```yaml
spring:
  application:
    name: eureka-server

server:
  port: 8761

# El servidor no se registra a sí mismo ni descarga el registro:
# él ES el registro.
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

Las dos opciones `register-with-eureka: false` y `fetch-registry: false` son la clave: indican que esta instancia es el **servidor** (el registro), no un cliente que deba registrarse en otro Eureka.

---

## Quién se registra

| Servicio | Nombre en Eureka | Puerto |
|---|---|---|
| API Gateway | `APIGATEWAYSERVICE` | 8080 |
| IFit | `IFIT` | 8081 |
| Ronnie | `RONNIE` | 8082 |

Los nombres se registran en mayúsculas por convención de Eureka, a partir del `spring.application.name` de cada microservicio.

---

## Puesta en Marcha

### Prerrequisitos

- Java 21+
- Maven 3.9+

### Compilar y arrancar

```bash
cd eureka-server
mvn clean package -DskipTests
mvn spring-boot:run
```

El dashboard estará disponible en `http://localhost:8761`.

### Orden de arranque

Eureka Server debe arrancar **antes** que los servicios que se registran en él (API Gateway, IFit, Ronnie). Si un cliente arranca antes, reintentará el registro hasta que Eureka esté disponible.

```
1. Eureka Server  → localhost:8761   ← este módulo
2. API Gateway    → localhost:8080
3. IFit           → localhost:8081
4. Ronnie         → localhost:8082
```

---

## Autor

**Juan García Candón**  
Universidad de Cádiz — Escuela Superior de Ingeniería  
Trabajo Final de Grado (TFG), 2024–2025
