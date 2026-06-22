#!/bin/bash

# ============================================================================
# SCRIPT: Reinicia todos los servicios iFit
# ============================================================================
# Uso: ./restart-services.sh
# Detiene y levanta de nuevo todos los servicios
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

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

# ============================================================================
# INICIO
# ============================================================================

print_header "REINICIANDO SERVICIOS iFIT"
echo ""

# ============================================================================
# EJECUTAR STOP
# ============================================================================

print_info "Deteniendo servicios actuales..."
echo ""

if [ -f "./stop-services.sh" ]; then
    chmod +x ./stop-services.sh
    ./stop-services.sh
else
    print_info "Script stop-services.sh no encontrado, deteniendo manualmente..."
    pkill -f 'spring-boot:run' 2>/dev/null
    docker compose down 2>/dev/null
    sleep 2
fi

echo ""

# ============================================================================
# EJECUTAR START
# ============================================================================

print_info "Levantando servicios nuevamente..."
echo ""

if [ -f "./start-services.sh" ]; then
    chmod +x ./start-services.sh
    ./start-services.sh
else
    print_error "Script start-services.sh no encontrado"
    exit 1
fi

echo ""

print_header "REINICIO COMPLETADO"
echo ""
print_success "Sistema iFit reiniciado exitosamente"
echo ""
print_info "Verificar estado con:"
echo "  ./status-services.sh"
echo ""
