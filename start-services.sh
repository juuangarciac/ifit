#!/bin/bash

# ============================================================================
# SCRIPT: Levanta el sistema iFit
# ============================================================================
# Uso:
#   ./start-services.sh                Levanta TODO (Docker + Java)
#   ./start-services.sh --docker       Levanta solo Docker (MySQL, Keycloak)
#   ./start-services.sh --java         Levanta solo los servicios Java
#   ./start-services.sh <servicio>     Levanta solo un servicio Java
#                                      (eureka-server|api-gateway|ifit|ronnie|admin-panel)
#
# No relanza lo que ya esté activo.
# ============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/lib-services.sh"
cd "$PROJECT_DIR" || exit 1

START_WAIT="${IFIT_START_WAIT:-60}"   # segundos máx. de espera por servicio Java
ARG="${1:-all}"

# ── Levanta los contenedores Docker ──────────────────────────────────────────
start_docker() {
    print_header "Servicios Docker (MySQL, Keycloak)"
    if ! command -v docker &>/dev/null; then
        print_error "Docker no está instalado"
        return 1
    fi

    local all_running=true
    for entry in "${DOCKER_SERVICES[@]}"; do
        IFS=':' read -r container port name <<< "$entry"
        if is_docker_running "$container"; then
            print_success "$container ya está corriendo"
        else
            all_running=false
            print_info "$container no está corriendo, se levantará"
        fi
    done

    if [ "$all_running" = false ]; then
        print_info "Ejecutando docker compose up -d..."
        docker compose up -d
        print_success "Servicios Docker levantados"
        print_info "Esperando a que estén listos (30s)..."
        sleep 30
    else
        print_info "Todos los servicios Docker ya están corriendo"
    fi
    echo ""
}

# ── Levanta un único servicio Java (recibe la entrada de JAVA_SERVICES) ───────
start_one_java() {
    local entry="$1"
    IFS=':' read -r service_dir port service_name health <<< "$entry"

    if is_port_open "$port"; then
        print_success "$service_name (puerto $port) ya está corriendo"
        return 0
    fi

    print_info "Compilando y levantando $service_name..."
    if [ ! -d "$PROJECT_DIR/$service_dir" ]; then
        print_error "Directorio $service_dir no encontrado, se omite"
        return 1
    fi

    cd "$PROJECT_DIR/$service_dir" || return 1
    if ! mvn clean package -q -DskipTests 2>/dev/null; then
        print_error "Error compilando $service_name (prueba: mvn -f $service_dir clean package)"
        cd "$PROJECT_DIR" || exit 1
        return 1
    fi

    local local_log; local_log="$(log_path_for "$service_dir")"
    nohup mvn spring-boot:run > "$local_log" 2>&1 &
    print_info "  PID: $!   Log: tail -f $local_log"

    print_info "  Esperando a que esté listo (máx ${START_WAIT}s)..."
    local ready=false
    for ((i=1; i<=START_WAIT; i++)); do
        if is_port_open "$port"; then
            print_success "$service_name está listo (${i}s)"
            ready=true
            break
        fi
        sleep 1
    done
    [ "$ready" = false ] && print_warning "$service_name no abrió el puerto en ${START_WAIT}s (sigue arrancando; revisa el log)"

    cd "$PROJECT_DIR" || exit 1
}

# ── Levanta TODOS los servicios Java (en orden) ──────────────────────────────
start_all_java() {
    print_header "Verificando Java"
    if ! command -v java &>/dev/null; then
        print_error "Java no está instalado (apt install openjdk-21-jdk)"
        return 1
    fi
    print_success "Java encontrado: $(java -version 2>&1 | head -1)"
    echo ""

    print_header "Servicios Java"
    for entry in "${JAVA_SERVICES[@]}"; do
        start_one_java "$entry"
    done
    echo ""
}

# ── Resumen final ────────────────────────────────────────────────────────────
print_summary() {
    print_header "RESUMEN"
    echo ""
    for entry in "${DOCKER_SERVICES[@]}"; do
        IFS=':' read -r container port name <<< "$entry"
        if is_docker_running "$container"; then echo -e "  ${GREEN}✓${NC} $name ($container)"; else echo -e "  ${RED}✗${NC} $name ($container)"; fi
    done
    for entry in "${JAVA_SERVICES[@]}"; do
        IFS=':' read -r service_dir port service_name health <<< "$entry"
        if is_port_open "$port"; then echo -e "  ${GREEN}✓${NC} $service_name (http://localhost:$port)"; else echo -e "  ${RED}✗${NC} $service_name (puerto $port)"; fi
    done
    echo ""
    print_info "Estado detallado:  ./status-services.sh"
    print_info "Comandos útiles:   ./help-services.sh"
    echo ""
}

# ============================================================================
# LÓGICA PRINCIPAL
# ============================================================================
print_header "INICIANDO SERVICIOS iFIT"
echo ""

case "$ARG" in
    all)
        start_docker
        start_all_java
        print_summary
        ;;
    --docker)
        start_docker
        print_summary
        ;;
    --java)
        start_all_java
        print_summary
        ;;
    eureka-server|api-gateway|ifit|ronnie|admin-panel)
        # Buscar la definición del servicio y levantarlo
        found=""
        for entry in "${JAVA_SERVICES[@]}"; do
            IFS=':' read -r service_dir port service_name health <<< "$entry"
            [ "$service_dir" = "$ARG" ] && { found="$entry"; break; }
        done
        if [ -z "$found" ]; then
            print_error "Servicio '$ARG' no encontrado en la configuración"
            exit 1
        fi
        print_header "Levantando solo: $ARG"
        echo ""
        start_one_java "$found"
        echo ""
        print_info "Estado detallado: ./status-services.sh"
        ;;
    *)
        print_error "Argumento '$ARG' no reconocido"
        echo ""
        print_info "Uso:"
        echo "  ./start-services.sh                Levanta todo"
        echo "  ./start-services.sh --docker       Solo Docker"
        echo "  ./start-services.sh --java         Solo servicios Java"
        echo "  ./start-services.sh <servicio>     eureka-server|api-gateway|ifit|ronnie|admin-panel"
        exit 1
        ;;
esac

print_success "Arranque finalizado"
