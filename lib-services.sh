#!/bin/bash

# ============================================================================
# LIBRERÍA COMÚN - Gestión de servicios iFit
# ============================================================================
# Este fichero NO se ejecuta directamente. Lo importan (source) los demás
# scripts: start/stop/restart/status/help-services.sh
#
# Centraliza: colores, helpers y la DEFINICIÓN ÚNICA de servicios para que
# los puertos no se desincronicen entre scripts.
# ============================================================================

# ── Colores ─────────────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

# ── Directorio raíz del proyecto (donde vive este fichero) ───────────────────
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# ── Carpeta de logs ──────────────────────────────────────────────────────────
LOG_DIR="${IFIT_LOG_DIR:-/tmp}"

# ============================================================================
# DEFINICIÓN ÚNICA DE SERVICIOS
# ============================================================================
# Docker:  "contenedor:puerto:Nombre legible"
DOCKER_SERVICES=(
    "ifit-mysql:3306:MySQL"
    "ifit-keycloak:9090:Keycloak"
)

# Java:    "directorio:puerto:Nombre legible:ruta_health"
#          ruta_health vacía = el servicio no expone /actuator/health
#          El ORDEN importa: eureka y gateway primero (los demás se registran).
JAVA_SERVICES=(
    "eureka-server:8761:Eureka Server:/"
    "api-gateway:8080:API Gateway:"
    "ifit:8081:iFit:/actuator/health"
    "ronnie:8082:Ronnie:/actuator/health"
    "admin-panel:8090:Admin Panel:"
)

# ============================================================================
# IMPRESIÓN
# ============================================================================
print_header()  { echo -e "${BLUE}========================================${NC}"; echo -e "${BLUE}${BOLD}$1${NC}"; echo -e "${BLUE}========================================${NC}"; }
print_success() { echo -e "${GREEN}✓ $1${NC}"; }
print_error()   { echo -e "${RED}✗ $1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠ $1${NC}"; }
print_info()    { echo -e "${CYAN}ℹ $1${NC}"; }

# ============================================================================
# HELPERS DE ESTADO
# ============================================================================

# ¿Hay algo escuchando en el puerto? Usa varios métodos según disponibilidad.
is_port_open() {
    local port="$1"
    if command -v nc &>/dev/null; then
        nc -z localhost "$port" 2>/dev/null && return 0 || return 1
    elif command -v ss &>/dev/null; then
        ss -ltnH "sport = :$port" 2>/dev/null | grep -q . && return 0 || return 1
    else
        # Fallback puro bash con /dev/tcp
        (echo > "/dev/tcp/localhost/$port") 2>/dev/null && return 0 || return 1
    fi
}

# ¿Contenedor Docker corriendo?
is_docker_running() {
    docker ps --format '{{.Names}}' 2>/dev/null | grep -q "^$1$"
}

# PID del proceso que escucha en un puerto (cadena vacía si ninguno).
get_pid_for_port() {
    local port="$1" pid=""
    if command -v ss &>/dev/null; then
        pid=$(ss -ltnpH "sport = :$port" 2>/dev/null | grep -oP 'pid=\K[0-9]+' | head -1)
    fi
    if [ -z "$pid" ] && command -v lsof &>/dev/null; then
        pid=$(lsof -ti "tcp:$port" -sTCP:LISTEN 2>/dev/null | head -1)
    fi
    echo "$pid"
}

# Recursos de un proceso: imprime "RAM_MB CPU% UPTIME" (o "- - -").
get_process_stats() {
    local pid="$1"
    if [ -z "$pid" ] || ! kill -0 "$pid" 2>/dev/null; then
        echo "- - -"; return
    fi
    local rss pcpu etime
    read -r rss pcpu etime < <(ps -p "$pid" -o rss=,pcpu=,etime= 2>/dev/null)
    if [ -z "$rss" ]; then echo "- - -"; return; fi
    echo "$((rss / 1024)) ${pcpu} ${etime}"
}

# Health HTTP: imprime "código tiempo_s" (código 000 = sin respuesta).
http_health() {
    local url="$1"
    if command -v curl &>/dev/null; then
        curl -s -o /dev/null -w "%{http_code} %{time_total}" --max-time 4 "$url" 2>/dev/null || echo "000 0"
    else
        echo "n/a n/a"
    fi
}

# Ruta del log de un servicio Java a partir de su directorio.
log_path_for() { echo "${LOG_DIR}/$1.log"; }

# Carga y EXPORTA las variables del .env del proyecto.
# Necesario porque 'mvn spring-boot:run' (a diferencia de docker compose) NO
# lee el .env, y la app usa placeholders como ${MAIL_USERNAME}, ${GROQ_API_KEY}.
# Devuelve 0 si lo cargó, 1 si no existe el fichero.
load_env() {
    local env_file="${PROJECT_DIR}/.env"
    if [ -f "$env_file" ]; then
        set -a                 # exporta automáticamente todo lo que se defina
        # shellcheck disable=SC1090
        source "$env_file"
        set +a
        return 0
    fi
    return 1
}
