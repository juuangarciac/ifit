#!/bin/bash

# ============================================================================
# SCRIPT: Detiene todos los servicios iFit
# ============================================================================
# Uso: ./stop-services.sh
# Detiene Docker y procesos Java
# ============================================================================

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

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

# ============================================================================
# INICIO
# ============================================================================

print_header "DETENIENDO SERVICIOS iFIT"
echo ""

# ============================================================================
# DETENER SERVICIOS JAVA
# ============================================================================

print_info "Deteniendo servicios Java..."
if pkill -f 'spring-boot:run' 2>/dev/null; then
    print_success "Procesos Java detenidos"
    sleep 2
else
    print_info "No había procesos Java corriendo"
fi

echo ""

# ============================================================================
# DETENER DOCKER
# ============================================================================

print_info "Deteniendo Docker Compose..."
if docker compose down 2>/dev/null; then
    print_success "Docker detenido (MySQL y Keycloak)"
else
    print_error "Error deteniendo Docker"
fi

echo ""

# ============================================================================
# RESUMEN
# ============================================================================

print_header "RESUMEN"
echo ""
print_success "Todos los servicios han sido detenidos"
echo ""
print_info "Para volver a levantarlos:"
echo "  ./start-services.sh"
echo ""
print_info "O reiniciar directamente:"
echo "  ./restart-services.sh"
echo ""
