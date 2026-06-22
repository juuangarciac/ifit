#!/bin/bash

# ============================================================================
# SCRIPT: Verifica el estado de todos los servicios iFit
# ============================================================================
# Uso: ./status-services.sh
# Muestra el estado de Docker, Java y salud de puertos
# ============================================================================

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# Verificar si un puerto está abierto
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
# INICIO
# ============================================================================

print_header "ESTADO DE SERVICIOS iFIT"
echo ""

# ============================================================================
# SERVICIOS DOCKER
# ============================================================================

print_header "Servicios Docker"

DOCKER_SERVICES=("ifit-mysql" "ifit-keycloak")
DOCKER_OK=0
DOCKER_FAIL=0

for service in "${DOCKER_SERVICES[@]}"; do
    if is_docker_running "$service"; then
        print_success "$service está corriendo"
        ((DOCKER_OK++))
    else
        print_error "$service NO está corriendo"
        ((DOCKER_FAIL++))
    fi
done

echo ""

# ============================================================================
# SERVICIOS JAVA
# ============================================================================

print_header "Servicios Java (Puertos)"

JAVA_SERVICES=(
    "API Gateway:8080"
    "iFit:8081"
    "Ronnie:8082"
    "Admin Panel:8090"
)

JAVA_OK=0
JAVA_FAIL=0

for service_config in "${JAVA_SERVICES[@]}"; do
    IFS=':' read -r service_name port <<< "$service_config"

    if is_port_open "$port"; then
        print_success "$service_name (puerto $port) está corriendo"
        ((JAVA_OK++))
    else
        print_error "$service_name (puerto $port) NO está corriendo"
        ((JAVA_FAIL++))
    fi
done

echo ""

# ============================================================================
# OTROS SERVICIOS
# ============================================================================

print_header "Otros Servicios"

if is_port_open 9090; then
    print_success "Keycloak Admin (puerto 9090) está corriendo"
else
    print_warning "Keycloak Admin (puerto 9090) NO está corriendo"
fi

if is_port_open 3306; then
    print_success "MySQL (puerto 3306) está corriendo"
else
    print_warning "MySQL (puerto 3306) NO está corriendo"
fi

echo ""

# ============================================================================
# RESUMEN
# ============================================================================

print_header "RESUMEN"

echo -e "${BLUE}Docker Services:${NC}"
echo -e "  ${GREEN}Corriendo:${NC} $DOCKER_OK"
echo -e "  ${RED}Parados:${NC} $DOCKER_FAIL"

echo ""
echo -e "${BLUE}Java Services:${NC}"
echo -e "  ${GREEN}Corriendo:${NC} $JAVA_OK"
echo -e "  ${RED}Parados:${NC} $JAVA_FAIL"

echo ""

# ============================================================================
# COMANDOS ÚTILES
# ============================================================================

print_header "COMANDOS ÚTILES"

echo ""
print_info "Ver logs en tiempo real:"
echo "  tail -f /tmp/api-gateway.log"
echo "  tail -f /tmp/ifit.log"
echo "  tail -f /tmp/ronnie.log"
echo "  tail -f /tmp/admin-panel.log"
echo "  docker logs -f ifit-mysql"
echo "  docker logs -f ifit-keycloak"

echo ""
print_info "Reiniciar servicios:"
echo "  pkill -f 'spring-boot:run'    # Detiene todos los Java"
echo "  docker compose restart        # Reinicia Docker"
echo "  ./start-services.sh           # Levanta todo de nuevo"

echo ""
print_info "Acceso a servicios:"
echo "  🔐 Keycloak:      http://localhost:9090"
echo "  🏠 Admin Panel:    http://localhost:8090"
echo "  🔌 iFit API:       http://localhost:8081"
echo "  🤖 Ronnie:         http://localhost:8082"
echo "  🌐 API Gateway:    http://localhost:8080"

echo ""

if [ $DOCKER_FAIL -eq 0 ] && [ $JAVA_FAIL -eq 0 ]; then
    print_success "Todos los servicios están corriendo ✓"
else
    print_warning "Algunos servicios no están corriendo"
fi
