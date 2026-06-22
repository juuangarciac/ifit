# 🖥️ Configuración del Servidor VPS (Arsys) — Puesta en Producción de iFit

Guía completa de cómo se ha desplegado el sistema iFit en un VPS de **Arsys**,
incluyendo la arquitectura de despliegue, los scripts de gestión, la
configuración de Keycloak para acceso por HTTP y los puntos pendientes para un
entorno de producción "serio".

> Este documento es un registro operativo: describe **cómo está montado el
> servidor hoy** y los pasos para reproducirlo desde cero.

---

## 1. Infraestructura

| Elemento | Valor |
|----------|-------|
| Proveedor | **Arsys** (VPS) |
| Sistema operativo | **Ubuntu 22.04 LTS** |
| RAM recomendada | **8 GB** (4 GB se queda muy justo con 5 servicios Java + MySQL + Keycloak) |
| Java | **OpenJDK 21** (el proyecto está en `<java.version>21</java.version>`) |
| Acceso | SSH (`ssh root@TU_IP_ARSYS`) |

### Modelo de despliegue
- **En Docker** (solo infraestructura): **MySQL** y **Keycloak**.
- **En local con Maven** (`mvn spring-boot:run`): los 5 microservicios Java
  (Eureka, API Gateway, iFit, Ronnie, Admin Panel).

Es decir, el `docker-compose.yml` **solo** levanta MySQL y Keycloak; el resto se
ejecuta como procesos Java mediante los scripts de gestión.

---

## 2. Mapa de puertos

| Servicio | Puerto | Dónde corre | Notas |
|----------|--------|-------------|-------|
| MySQL | `3306` | Docker | Expuesto; acceso desde Workbench (root/root) |
| Keycloak | `9090` | Docker | OAuth2 / admin console |
| Eureka Server | `8761` | Java (local) | Service discovery; arranca el primero |
| API Gateway | `8080` | Java (local) | Punto de entrada único |
| iFit | `8081` | Java (local) | Lógica de negocio (+ Swagger en `/swagger-ui.html`) |
| Ronnie | `8082` | Java (local) | Motor de IA (+ Swagger en `/swagger-ui.html`) |
| Admin Panel | `8090` | Java (local) | Interfaz Vaadin de administración |

> **Firewall de Arsys:** hay que abrir los puertos que se consuman desde fuera:
> `8080`, `8081`, `8082`, `8090`, `9090` (y `3306` solo si conectas la BD en
> remoto). Si un puerto no abre, el servicio no será accesible aunque esté
> levantado.

---

## 3. Puesta en marcha desde cero

### 3.1. Instalar dependencias en el VPS
```bash
apt update && apt upgrade -y
curl -fsSL https://get.docker.com | sh        # Docker
apt install -y openjdk-21-jdk maven git       # Java 21, Maven, Git
```

### 3.2. Clonar el repositorio (HTTPS + Personal Access Token)
GitHub ya no acepta contraseña por HTTPS. Genera un **Personal Access Token**
(scope `repo`) en https://github.com/settings/tokens y clona así:
```bash
git clone https://USUARIO:TOKEN@github.com/USUARIO/ifit.git
cd ifit
```

### 3.3. Crear el fichero `.env`
> ⚠️ **Importante:** usa un *here-doc* con `'EOF'` entre comillas para que el
> shell **no** interprete `$`, comillas ni caracteres especiales. Pegar el `.env`
> directamente en la terminal a veces lo corrompe (caracteres de escape como
> `\x1b`); si `cat .env` sale vacío o raro, recréalo con `nano .env`.

```bash
cat > .env << 'EOF'
DB_USERNAME=root
DB_PASSWORD=root
DB_ROOT_PASSWORD=root
DB_NAME=ifit
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin
KEYCLOAK_CLIENT_ID=springboot-ifit-client
KEYCLOAK_REALM=ifit-realm
KEYCLOAK_CLIENT_SECRET=<secret-del-cliente>
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=<correo>
MAIL_PASSWORD=<app-password-de-gmail>
GROQ_API_KEY=<clave-groq>
GROQ_MODEL_NAME=llama-3.3-70b-versatile
GROQ_ROUTINE_MODEL_NAME=openai/gpt-oss-120b
LLM_MODEL=llama-3.3-70b-versatile
EOF
```

> **Nota técnica clave:** `docker compose` lee el `.env` automáticamente, pero
> `mvn spring-boot:run` **NO**. Por eso los scripts de arranque cargan y exportan
> el `.env` (`load_env` en `lib-services.sh`) antes de lanzar los servicios Java.
> Sin esto, fallan con `Could not resolve placeholder 'MAIL_USERNAME'` (o
> `GROQ_API_KEY`, etc.).

### 3.4. Levantar todo
```bash
chmod +x *.sh
./start-services.sh
```

---

## 4. Scripts de gestión

Todos viven en la raíz del repo y comparten una **librería común**
(`lib-services.sh`) donde se define **una sola vez** la lista de servicios y sus
puertos (para que no se desincronicen entre scripts).

### `lib-services.sh` (librería, no se ejecuta directamente)
- Colores y helpers de impresión.
- Definición única de servicios Docker y Java (nombre, puerto, ruta de health).
- Helpers: `is_port_open`, `is_docker_running`, `get_pid_for_port`,
  `get_process_stats`, `http_health`, y `load_env` (carga/exporta el `.env`).

### `start-services.sh` — Arranca el sistema
- Verifica Docker y Java.
- Carga el `.env`.
- Levanta MySQL + Keycloak con `docker compose up -d` (si no están ya).
- Compila (`mvn clean package -DskipTests`) y lanza cada servicio Java en segundo
  plano con `nohup`, esperando a que abra su puerto.
- **No relanza** lo que ya esté activo.
- Argumentos:
  - `./start-services.sh` → todo
  - `./start-services.sh --docker` → solo MySQL y Keycloak
  - `./start-services.sh --java` → solo los servicios Java
  - `./start-services.sh <servicio>` → uno solo (`eureka-server`, `api-gateway`,
    `ifit`, `ronnie`, `admin-panel`)

### `stop-services.sh` — Detiene el sistema
- Mata los procesos `spring-boot:run` y baja Docker (`docker compose down`).
- Argumentos: `--java` (solo Java) o `--docker` (solo contenedores).

### `restart-services.sh` — Reinicia
- `./restart-services.sh` → reinicia todo (stop + start).
- `./restart-services.sh <servicio>` → reinicia **solo** ese servicio Java
  (recompila y relanza), sin tocar el resto.

### `status-services.sh` — Estado detallado
Muestra, por cada servicio:
- Docker: estado, salud (healthcheck), CPU, memoria y uptime.
- Java: puerto, **PID, RAM, CPU, uptime** y **health HTTP** (código + tiempo).
- **Servicios registrados en Eureka**.
- Métricas del sistema (RAM, carga, disco).

> Monitor en vivo: `watch -n 2 ./status-services.sh`

### `help-services.sh` — Chuleta de comandos
- `./help-services.sh` → referencia completa.
- `./help-services.sh <servicio>` → comandos centrados en un servicio.
Incluye cómo: ver estado/recursos, logs en vivo, buscar errores, **health/ping
continuo**, **espiar tráfico HTTP** (`tcpdump`), conexiones activas y control
individual.

### Logs
- Servicios Java: `/tmp/<servicio>.log` (p. ej. `tail -f /tmp/ifit.log`).
- Docker: `docker logs -f ifit-mysql` / `docker logs -f ifit-keycloak`.

---

## 5. Keycloak: acceso por HTTP (sin SSL todavía)

Por defecto Keycloak exige HTTPS para todo acceso que no sea `localhost`
(`sslRequired=external`), lo que **bloquea el acceso por IP pública sin
certificado**. Mientras no haya dominio + SSL, se ha configurado acceso HTTP:

### Cambios ya aplicados en el repo
- **`ifit-realm-export.json`**: `sslRequired` cambiado de `external` a `none`
  (el realm `ifit-realm` permite HTTP).
- **`ifit-realm-export.json`**: el secret del cliente `springboot-ifit-client`
  estaba enmascarado como `**********`, lo que hacía que Keycloak generara uno
  aleatorio en cada importación. Se ha **fijado el valor real** para que el
  despliegue sea reproducible.
- **`docker-compose.yml`** (servicio `keycloak`):
  - `command: start-dev --import-realm` → **auto-importa** el realm al arrancar.
  - Volumen del fichero del realm montado en `/opt/keycloak/data/import/`.
  - Volumen **`keycloak_data`** en `/opt/keycloak/data` → **persiste** realms,
    usuarios y cambios entre reinicios (antes `start-dev` usaba H2 efímero).
  - Se quitó `KC_PROXY: edge` (estorbaba en HTTP directo sin proxy).

### Paso manual necesario (realm `master`)
El realm `master` (el del admin console) **no** está en el export, así que su
`sslRequired` hay que ajustarlo una vez por línea de comandos:
```bash
docker exec -it ifit-keycloak /opt/keycloak/bin/kcadm.sh config credentials \
  --server http://localhost:9090 --realm master --user admin --password admin

docker exec -it ifit-keycloak /opt/keycloak/bin/kcadm.sh update realms/master -s sslRequired=NONE
docker exec -it ifit-keycloak /opt/keycloak/bin/kcadm.sh update realms/ifit-realm -s sslRequired=NONE
```
Con el volumen `keycloak_data`, estos cambios **persisten** entre reinicios.

### Obtener el client secret real
El admin console muestra el secret como asteriscos. Para verlo de verdad:
```bash
docker exec -it ifit-keycloak /opt/keycloak/bin/kcadm.sh get \
  clients -r ifit-realm -q clientId=springboot-ifit-client --fields id,secret
```
Ese valor debe coincidir con `KEYCLOAK_CLIENT_SECRET` del `.env`. Si lo cambias,
reinicia: `./restart-services.sh ifit && ./restart-services.sh ronnie`.

---

## 6. Acceso a la base de datos

Configuración actual: conexión **directa** desde MySQL Workbench al puerto `3306`.

| Campo | Valor |
|-------|-------|
| Hostname | `TU_IP_ARSYS` |
| Port | `3306` |
| Username | `root` |
| Password | `root` |
| Default Schema | `ifit` |

> ⚠️ **Seguridad:** el `3306` está abierto a internet con credenciales
> `root/root`. Aceptable para pruebas de TFG, pero **no para producción real**.
> Alternativa segura: túnel SSH (Workbench → "Standard TCP/IP over SSH") y cerrar
> el `3306` al exterior.

---

## 7. Documentación de APIs

El Admin Panel incluye un módulo **Documentación** que genera enlaces (con el
host correcto) a:
- Swagger UI / OpenAPI JSON de **iFit** (`:8081`) y **Ronnie** (`:8082`).
- Consolas de **Eureka** (`:8761`) y **Keycloak** (`:9090`).

Rutas directas:
- iFit Swagger: `http://TU_IP:8081/swagger-ui.html`
- Ronnie Swagger: `http://TU_IP:8082/swagger-ui.html`

---

## 8. Problemas encontrados y soluciones (histórico)

| Problema | Causa | Solución |
|----------|-------|----------|
| `docker-compose: command not found` | Docker moderno usa `docker compose` (subcomando) | Scripts actualizados a `docker compose` |
| Errores de compilación `class X is public, should be declared in a file named X.java` | Ficheros `*DTO.java` con clase `*Dto` (Linux es *case-sensitive*) | Renombrados todos a `*Dto.java` |
| `Could not resolve placeholder 'MAIL_USERNAME' / 'GROQ_API_KEY'` | `mvn spring-boot:run` no lee el `.env` | `load_env` en los scripts exporta el `.env` antes de arrancar |
| `cat .env` vacío / `unexpected character "\x1b"` | El `.env` se corrompió al pegarlo en la terminal | Recrear con `nano` o here-doc `'EOF'` |
| Keycloak pide HTTPS | `sslRequired=external` bloquea HTTP no-localhost | `sslRequired=NONE` en realms + ajustes de `docker-compose` |
| Client secret cambia en cada importación | Estaba como `**********` en el export | Fijado el valor real en `ifit-realm-export.json` |
| `status-services.sh` se cortaba tras la 1ª línea | `set -e` + `((VAR++))` devuelve exit 1 con contador a 0 | Eliminado `set -e`, usado `VAR=$((VAR+1))` |

---

## 9. Pendiente para producción "seria"

Lo que falta para considerarlo un release de producción robusto:

1. **HTTPS** con el dominio (en trámite) + **Let's Encrypt** + **Nginx** como
   reverse proxy delante de los servicios. Después, volver a subir `sslRequired`
   en Keycloak.
2. **Arranque automático tras reboot:** hoy MySQL y Keycloak vuelven solos
   (`restart: unless-stopped`), pero **los servicios Java NO**. Convendría
   empaquetar los `.jar` y crear **servicios systemd** (o usar los `.jar` en vez
   de `mvn spring-boot:run`).
3. **Cerrar el `3306`** al exterior y/o cambiar credenciales de MySQL.
4. **Sacar los secrets del repositorio** (`.env`, secret del realm, claves de
   Groq/Gmail) a un gestor de secretos o variables de entorno del servidor.

---

## 10. Resumen de comandos rápidos

```bash
# Arrancar / parar / reiniciar
./start-services.sh                 # todo
./start-services.sh ronnie          # solo un servicio
./stop-services.sh                  # parar todo
./restart-services.sh ifit          # reiniciar un servicio

# Estado y diagnóstico
./status-services.sh                # estado detallado
watch -n 2 ./status-services.sh     # monitor en vivo
./help-services.sh ifit             # comandos útiles de un servicio

# Logs
tail -f /tmp/ifit.log
docker logs -f ifit-keycloak

# Docker (infraestructura)
docker compose up -d
docker compose down
docker ps
```
