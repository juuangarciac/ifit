#!/bin/bash

# ============================================================================
# SCRIPT: Detiene los servicios iFit
# ============================================================================
# Uso:
#   ./stop-services.sh            Detiene Java + Docker
#   ./stop-services.sh --java     Detiene solo los servicios Java
#   ./stop-services.sh --docker   Detiene solo Docker (MySQL, Keycloak)
# ============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/lib-services.sh"
cd "$PROJECT_DIR" || exit 1

MODE="${1:-all}"

print_header "DETENIENDO SERVICIOS iFIT"
echo ""

# ── Servicios Java ───────────────────────────────────────────────────────────
if [ "$MODE" = "all" ] || [ "$MODE" = "--java" ]; then
    print_info "Deteniendo servicios Java..."
    # spring-boot:run lanza un proceso hijo (fork). Matamos ambos patrones.
    if pkill -f 'spring-boot:run' 2>/dev/null; then
        print_success "Procesos 'spring-boot:run' señalizados"
    else
        print_info "No había procesos 'spring-boot:run'"
    fi
    # Forks de Spring Boot (java ... .jar lanzado por el plugin)
    pkill -f 'spring-boot.run.fork' 2>/dev/null
    sleep 2

    # Verificar que los puertos quedaron libres
    for entry in "${JAVA_SERVICES[@]}"; do
        IFS=':' read -r service_dir port service_name health <<< "$entry"
        if is_port_open "$port"; then
            print_warning "$service_name sigue escuchando en $port (PID $(get_pid_for_port "$port"))"
        fi
    done
    echo ""
fi

# ── Docker ───────────────────────────────────────────────────────────────────
if [ "$MODE" = "all" ] || [ "$MODE" = "--docker" ]; then
    print_info "Deteniendo Docker Compose..."
    if docker compose down 2>/dev/null; then
        print_success "Docker detenido (MySQL y Keycloak)"
    else
        print_error "Error deteniendo Docker (¿docker compose disponible?)"
    fi
    echo ""
fi

print_header "RESUMEN"
echo ""
print_success "Operación de parada completada (modo: $MODE)"
echo ""
print_info "Volver a levantar:  ./start-services.sh"
print_info "Reiniciar:          ./restart-services.sh"
print_info "Ver estado:         ./status-services.sh"
echo ""
