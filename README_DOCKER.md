# 🚀 Guía de Implementación: Contenerización del Sistema IFit

## 📋 Índice
1. [Preparación](#preparacion)
2. [Paso a Paso](#paso-a-paso)
3. [Verificación](#verificacion)
4. [Uso Diario](#uso-diario)
5. [Troubleshooting](#troubleshooting)

---

## 🛠️ Preparación {#preparacion}

### Requisitos Previos

✅ Docker Desktop instalado y corriendo  
✅ 8GB RAM mínimo (16GB recomendado)  
✅ 20GB espacio en disco libre  
✅ Todos tus microservicios compilando sin errores

### Verificar Docker

```bash
docker --version
# Debería mostrar: Docker version 24.x.x

docker-compose --version
# Debería mostrar: Docker Compose version 2.x.x

# Test rápido
docker run hello-world
```

---

## 📝 Paso a Paso {#paso-a-paso}

### Paso 1: Organizar tu Proyecto

Tu estructura debe quedar así:

```
IFit/  (carpeta raíz)
├── eureka-server/
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile                    ← Crear
│   ├── .dockerignore                 ← Crear
│   └── application-docker.properties ← Crear en src/main/resources/
│
├── api-gateway/
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile                    ← Crear
│   ├── .dockerignore                 ← Crear
│   └── application-docker.properties ← Crear en src/main/resources/
│
├── authapi/
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile                    ← Crear
│   ├── .dockerignore                 ← Crear
│   └── application-docker.properties ← Crear en src/main/resources/
│
├── ifit/
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile                    ← Crear
│   ├── .dockerignore                 ← Crear
│   └── application-docker.properties ← Crear en src/main/resources/
│
├── ronnie/
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile                    ← Crear
│   ├── .dockerignore                 ← Crear
│   └── application-docker.properties ← Crear en src/main/resources/
│
├── docker-compose.yml                ← Crear (raíz del proyecto)
├── manage-ifit.sh                    ← Crear (raíz del proyecto)
├── init-ollama.sh                    ← Crear (raíz del proyecto)
└── README-DOCKER.md                  ← Este archivo
```

### Paso 2: Copiar Archivos Base

```bash
# Desde la carpeta donde descomprimiste el ZIP de ayuda:

# 1. Copiar Dockerfile a CADA microservicio
cp Dockerfile.microservicio /ruta/a/eureka-server/Dockerfile
cp Dockerfile.microservicio /ruta/a/api-gateway/Dockerfile
cp Dockerfile.microservicio /ruta/a/authapi/Dockerfile
cp Dockerfile.microservicio /ruta/a/ifit/Dockerfile
cp Dockerfile.microservicio /ruta/a/ronnie/Dockerfile

# 2. Copiar .dockerignore a CADA microservicio
cp .dockerignore /ruta/a/eureka-server/
cp .dockerignore /ruta/a/api-gateway/
cp .dockerignore /ruta/a/authapi/
cp .dockerignore /ruta/a/ifit/
cp .dockerignore /ruta/a/ronnie/

# 3. Copiar docker-compose.yml a la raíz
cp docker-compose.yml /ruta/a/IFit/

# 4. Copiar scripts a la raíz
cp manage-ifit.sh /ruta/a/IFit/
cp init-ollama.sh /ruta/a/IFit/

# 5. Dar permisos a scripts
chmod +x /ruta/a/IFit/manage-ifit.sh
chmod +x /ruta/a/IFit/init-ollama.sh
```

### Paso 3: Configurar Cada Microservicio

Para **CADA microservicio**, necesitas crear `application-docker.properties`:

#### 3.1 Eureka Server

Crear: `eureka-server/src/main/resources/application-docker.properties`

```properties
spring.application.name=eureka-server
server.port=8761

# Eureka standalone
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
eureka.server.enable-self-preservation=false

# Logging
logging.level.root=INFO

# Actuator
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always
```

#### 3.2 API Gateway

Crear: `api-gateway/src/main/resources/application-docker.properties`

```properties
spring.application.name=ApiGatewayService
server.port=8080

# Eureka
eureka.client.service-url.defaultZone=http://eureka:8761/eureka/
eureka.instance.prefer-ip-address=true
eureka.instance.hostname=gateway

# OAuth2
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://keycloak:9090/realms/ifit-realm
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://keycloak:9090/realms/ifit-realm/protocol/openid-connect/certs

# Logging
logging.level.org.springframework.cloud.gateway=DEBUG
logging.level.org.springframework.security=DEBUG

# Actuator
management.endpoints.web.exposure.include=health,info,gateway
management.endpoint.health.show-details=always
```

#### 3.3 AuthAPI

Crear: `authapi/src/main/resources/application-docker.properties`

```properties
spring.application.name=AUTHAPI
server.port=8083
server.servlet.context-path=/ifit/keycloak

# Eureka
eureka.client.service-url.defaultZone=http://eureka:8761/eureka/
eureka.instance.prefer-ip-address=true
eureka.instance.hostname=authapi

# OAuth2
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://keycloak:9090/realms/ifit-realm
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://keycloak:9090/realms/ifit-realm/protocol/openid-connect/certs

# Keycloak Client
keycloak.token-url=http://keycloak:9090/realms/ifit-realm/protocol/openid-connect/token
keycloak.client-id=springboot-ifit-client
keycloak.client-secret=XIDVoNDSGHGDYTcbto1RdZlk0xNli8OD

# Logging
logging.level.org.springframework.security=DEBUG

# Actuator
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always
```

#### 3.4 IFIT

Crear: `ifit/src/main/resources/application-docker.properties`

```properties
spring.application.name=IFIT
server.port=8081
server.servlet.context-path=/ifit/api/v1

# Eureka
eureka.client.service-url.defaultZone=http://eureka:8761/eureka/
eureka.instance.prefer-ip-address=true
eureka.instance.hostname=ifit

# Database
spring.datasource.url=jdbc:mysql://mysql:3306/Ronnie
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update

# Logging
logging.level.com.ifit=DEBUG

# Actuator
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always
```

#### 3.5 RONNIE

Crear: `ronnie/src/main/resources/application-docker.properties`

```properties
spring.application.name=RONNIE
server.port=8082
server.servlet.context-path=/ifit/aimodels/api/v1

# Eureka
eureka.client.service-url.defaultZone=http://eureka:8761/eureka/
eureka.instance.prefer-ip-address=true
eureka.instance.hostname=ronnie

# Database
spring.datasource.url=jdbc:mysql://mysql:3306/Ronnie
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update

# Ollama
ollama.base-url=http://ollama:11434
ollama.model-name=llama3.1:8b
ollama.timeout=120

# Logging
logging.level.com.ifit=DEBUG

# Actuator
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always
```

### Paso 4: Verificar Configuración

```bash
cd IFit/  # Tu carpeta raíz

# Verificar que todos los archivos necesarios existen
ls -la eureka-server/Dockerfile
ls -la api-gateway/Dockerfile
ls -la authapi/Dockerfile
ls -la ifit/Dockerfile
ls -la ronnie/Dockerfile
ls -la docker-compose.yml
ls -la manage-ifit.sh
```

### Paso 5: Primera Ejecución

```bash
# Desde la carpeta raíz IFit/

# Usar el script de gestión
./manage-ifit.sh start

# O manualmente:
docker-compose build
docker-compose up -d
```

**Esto tomará tiempo la primera vez:**
- Descarga imágenes base (~2-3 GB)
- Compila todos los microservicios
- Inicia todos los contenedores
- Espera: 5-10 minutos

### Paso 6: Inicializar Ollama

```bash
# Descargar el modelo (solo una vez)
./init-ollama.sh

# O manualmente:
docker exec ifit-ollama ollama pull llama3.1:8b
```

---

## ✅ Verificación {#verificacion}

### Verificar que Todo Está Corriendo

```bash
# Ver estado de contenedores
./manage-ifit.sh status

# O manualmente:
docker-compose ps
```

**Todos deben estar "Up" y "healthy":**
```
NAME              STATUS
ifit-mysql        Up (healthy)
ifit-keycloak     Up (healthy)
ifit-ollama       Up (healthy)
ifit-eureka       Up (healthy)
ifit-gateway      Up (healthy)
ifit-authapi      Up (healthy)
ifit-service      Up (healthy)
ifit-ronnie       Up (healthy)
```

### Tests de Conectividad

```bash
# 1. Eureka
curl http://localhost:8761

# 2. Gateway Health
curl http://localhost:8080/actuator/health

# 3. Login (debe funcionar)
curl -X POST http://localhost:8080/ifit/keycloak/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'

# 4. Ollama
curl http://localhost:11434/api/tags
```

### Ver Logs

```bash
# Todos los servicios
./manage-ifit.sh logs

# Un servicio específico
./manage-ifit.sh logs gateway
./manage-ifit.sh logs ronnie

# O manualmente:
docker-compose logs -f gateway
```

---

## 🔄 Uso Diario {#uso-diario}

### Comandos Comunes

```bash
# Iniciar todo
./manage-ifit.sh start

# Detener todo
./manage-ifit.sh stop

# Reiniciar todo
./manage-ifit.sh restart

# Reiniciar solo un servicio
./manage-ifit.sh restart gateway

# Ver estado
./manage-ifit.sh status

# Ver logs en tiempo real
./manage-ifit.sh logs gateway

# Verificar salud
./manage-ifit.sh health
```

### Flujo de Desarrollo

**1. Hacer cambios en el código:**
```bash
# Editar tu código en src/
vim ronnie/src/main/java/...
```

**2. Reconstruir solo ese servicio:**
```bash
docker-compose build ronnie
docker-compose up -d ronnie
```

**3. Ver logs:**
```bash
docker-compose logs -f ronnie
```

### Después de Cambios Importantes

Si cambias dependencias (pom.xml) o configuración importante:

```bash
# Reconstruir todo sin caché
./manage-ifit.sh build

# Reiniciar
./manage-ifit.sh restart
```

---

## 🔧 Troubleshooting {#troubleshooting}

### Problema 1: "Port already in use"

**Síntoma:**
```
Error: bind: address already in use
```

**Solución:**
```bash
# Ver qué está usando el puerto
lsof -i :8080

# Opción 1: Matar el proceso
kill -9 PID

# Opción 2: Cambiar puerto en docker-compose.yml
ports:
  - "8081:8080"  # Usar 8081 en tu máquina
```

### Problema 2: Servicio no se registra en Eureka

**Síntoma:** En Eureka Dashboard no aparece tu servicio.

**Solución:**
```bash
# Ver logs del servicio
docker-compose logs gateway

# Verificar que application-docker.properties existe
ls -la gateway/src/main/resources/application-docker.properties

# Verificar que Eureka está corriendo
curl http://localhost:8761
```

### Problema 3: MySQL Connection Refused

**Síntoma:**
```
Unable to connect to MySQL
```

**Solución:**
```bash
# Verificar que MySQL está healthy
docker-compose ps mysql

# Ver logs de MySQL
docker-compose logs mysql

# Esperar más tiempo (MySQL tarda en iniciar)
docker-compose restart ifit
```

### Problema 4: Out of Memory

**Síntoma:**
```
java.lang.OutOfMemoryError
```

**Solución:** Editar `docker-compose.yml`:
```yaml
services:
  ronnie:
    environment:
      - JAVA_OPTS=-Xmx1g -Xms512m  # Aumentar memoria
```

### Problema 5: Ollama no responde

**Síntoma:** Ronnie no puede conectarse a Ollama.

**Solución:**
```bash
# Verificar Ollama
docker exec ifit-ollama ollama list

# Re-descargar modelo
./init-ollama.sh

# Verificar conectividad
docker exec ifit-ronnie curl http://ollama:11434/api/tags
```

### Reset Completo

Si nada funciona:

```bash
# ⚠️ ESTO ELIMINA TODO (incluida la BD)
./manage-ifit.sh reset

# Luego volver a empezar
./manage-ifit.sh start
./init-ollama.sh
```

---

## 📊 Arquitectura Final

```
┌─────────────────────────────────────────────────────────┐
│                     DOCKER NETWORK                      │
│                      (ifit-network)                     │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐             │
│  │  MySQL   │  │ Keycloak │  │  Ollama  │             │
│  │  :3306   │  │  :9090   │  │  :11434  │             │
│  └──────────┘  └──────────┘  └──────────┘             │
│                                                         │
│  ┌──────────┐                                          │
│  │  Eureka  │                                          │
│  │  :8761   │                                          │
│  └──────────┘                                          │
│       ↑                                                 │
│  ┌────┴─────────────────────────────┐                 │
│  │                                   │                 │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌────────┐│
│  │ Gateway  │  │ AuthAPI  │  │   IFIT   │  │ RONNIE ││
│  │  :8080   │  │  :8083   │  │  :8081   │  │ :8082  ││
│  └──────────┘  └──────────┘  └──────────┘  └────────┘│
│                                                         │
└─────────────────────────────────────────────────────────┘
       │             │             │             │
       ↓             ↓             ↓             ↓
    localhost     localhost    localhost    localhost
      :8080         :8083        :8081        :8082
```

---

## 🎯 Ventajas del Sistema Contenerizado

✅ **Un solo comando** para levantar todo  
✅ **Consistencia** entre desarrollo y producción  
✅ **Aislamiento** cada servicio en su contenedor  
✅ **Fácil de compartir** con tu equipo/tribunal  
✅ **Documentación viva** (docker-compose.yml)  
✅ **Rollback fácil** si algo falla  
✅ **Perfecto para demos** del TFG  

---

## 📝 Para tu Memoria del TFG

Puedes incluir:

1. **Diagrama de arquitectura** (el de arriba)
2. **Dockerfile explicado** con comentarios
3. **docker-compose.yml** como ejemplo de orquestación
4. **Ventajas de contenerización** para el proyecto
5. **Scripts de automatización** (manage-ifit.sh)

Esto demuestra:
- ✅ Conocimiento de DevOps moderno
- ✅ Arquitectura cloud-native
- ✅ Automatización de despliegues
- ✅ Buenas prácticas de desarrollo

---

¡Éxito con tu TFG! 🚀🎓
