#!/bin/bash

# ============================================================================
# SCRIPT: Levanta el sistema iFit completo
# ============================================================================
# Uso: ./start-services.sh
# Verifica qué servicios están corriendo antes de levantar nuevos
# ============================================================================

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ============================================================================
# FUNCIONES AUXILIARES
# ============================================================================

print_header() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

# Verificar si un puerto está en uso
is_port_open() {
    nc -z localhost "$1" 2>/dev/null
    return $?
}

# Verificar si un contenedor Docker está corriendo
is_docker_running() {
    docker ps --format '{{.Names}}' | grep -q "^$1$"
    return $?
}

# ============================================================================
# CONFIGURACIÓN
# ============================================================================

DOCKER_SERVICES=("ifit-mysql" "ifit-keycloak")
JAVA_SERVICES=(
    "api-gateway:8080:API Gateway"
    "ifit:8081:iFit"
    "ronnie:8082:Ronnie"
    "admin-panel:8084:Admin Panel"
)

# ============================================================================
# INICIO DEL SCRIPT
# ============================================================================

print_header "INICIANDO SERVICIOS iFIT"
echo ""

# ============================================================================
# 1. VERIFICAR DOCKER
# ============================================================================

print_info "Verificando Docker..."
if ! command -v docker &> /dev/null; then
    print_error "Docker no está instalado"
    echo "  Instala Docker desde: https://docs.docker.com/get-docker/"
    exit 1
fi
print_success "Docker encontrado"
echo ""

# ============================================================================
# 2. LEVANTAR SERVICIOS DOCKER
# ============================================================================

print_header "Servicios Docker (MySQL, Keycloak)"

all_docker_running=true
for service in "${DOCKER_SERVICES[@]}"; do
    if is_docker_running "$service"; then
        print_success "$service ya está corriendo"
    else
        all_docker_running=false
        print_info "Levantando $service..."
    fi
done

if [ "$all_docker_running" = false ]; then
    print_info "Ejecutando docker-compose up -d..."
    docker-compose up -d
    print_success "Servicios Docker levantados"
    print_info "Esperando a que estén listos (30s)..."
    sleep 30
else
    print_info "Todos los servicios Docker ya están corriendo"
fi

echo ""

# ============================================================================
# 3. VERIFICAR JAVA
# ============================================================================

print_header "Verificando Java"
if ! command -v java &> /dev/null; then
    print_error "Java no está instalado"
    echo "  Instala Java 17+: apt install openjdk-17-jdk"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | grep "version" | head -1)
print_success "Java encontrado: $JAVA_VERSION"
echo ""

# ============================================================================
# 4. LEVANTAR SERVICIOS JAVA
# ============================================================================

print_header "Servicios Java"

for service_config in "${JAVA_SERVICES[@]}"; do
    IFS=':' read -r service_dir port service_name <<< "$service_config"

    if is_port_open "$port"; then
        print_success "$service_name (puerto $port) ya está corriendo"
    else
        print_info "Compilando y levantando $service_name..."

        if [ ! -d "$service_dir" ]; then
            print_error "Directorio $service_dir no encontrado"
            continue
        fi

        cd "$service_dir"

        # Compilar
        if ! mvn clean package -q -DskipTests 2>/dev/null; then
            print_error "Error compilando $service_name"
            cd "$PROJECT_DIR"
            continue
        fi

        # Ejecutar en background
        nohup mvn spring-boot:run > /tmp/${service_dir}.log 2>&1 &
        SERVICE_PID=$!

        print_info "  PID: $SERVICE_PID"
        print_info "  Log: tail -f /tmp/${service_dir}.log"

        # Esperar a que esté listo
        print_info "  Esperando a que esté listo..."
        for i in {1..30}; do
            if is_port_open "$port"; then
                print_success "$service_name está listo"
                break
            fi
            if [ $i -eq 30 ]; then
                print_warning "$service_name tardó más de 30s en iniciar"
            fi
            sleep 1
        done

        cd "$PROJECT_DIR"
    fi
done

echo ""

# ============================================================================
# 5. RESUMEN Y STATUS
# ============================================================================

print_header "RESUMEN DE SERVICIOS"
echo ""

print_info "Docker Services:"
for service in "${DOCKER_SERVICES[@]}"; do
    if is_docker_running "$service"; then
        echo -e "  ${GREEN}✓${NC} $service"
    else
        echo -e "  ${RED}✗${NC} $service"
    fi
done

echo ""
print_info "Java Services:"
for service_config in "${JAVA_SERVICES[@]}"; do
    IFS=':' read -r service_dir port service_name <<< "$service_config"

    if is_port_open "$port"; then
        echo -e "  ${GREEN}✓${NC} $service_name (http://localhost:$port)"
    else
        echo -e "  ${RED}✗${NC} $service_name (puerto $port)"
    fi
done

echo ""

# ============================================================================
# 6. INFORMACIÓN ÚTIL
# ============================================================================

print_header "ACCESO A SERVICIOS"
echo ""
echo "  🔐 Keycloak:      http://localhost:9090"
echo "  🏠 Admin Panel:    http://localhost:8084"
echo "  🔌 iFit API:       http://localhost:8081"
echo "  🤖 Ronnie:         http://localhost:8082"
echo "  🌐 API Gateway:    http://localhost:8080"
echo ""

print_info "Ver logs:"
echo "  docker logs -f ifit-mysql"
echo "  docker logs -f ifit-keycloak"
echo "  tail -f /tmp/api-gateway.log"
echo "  tail -f /tmp/ifit.log"
echo "  tail -f /tmp/ronnie.log"
echo "  tail -f /tmp/admin-panel.log"
echo ""

print_info "Detener servicios:"
echo "  docker-compose down"
echo "  pkill -f 'spring-boot:run'"
echo ""

print_success "¡Sistema iFit levantado exitosamente!"
