#!/bin/bash

# ============================================================================
# SCRIPT: Reinicia los servicios iFit
# ============================================================================
# Uso:
#   ./restart-services.sh              Reinicia todo (Java + Docker)
#   ./restart-services.sh <servicio>   Reinicia solo un servicio Java
#                                      (eureka-server|api-gateway|ifit|ronnie|admin-panel)
# ============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/lib-services.sh"
cd "$PROJECT_DIR" || exit 1

TARGET="$1"

# Cargar .env para que Spring resuelva placeholders al relanzar con Maven
load_env || print_warning ".env no encontrado: posibles placeholders sin resolver"

# ── Reinicio de un único servicio Java ───────────────────────────────────────
if [ -n "$TARGET" ]; then
    # Buscar el servicio en la definición
    found=""
    for entry in "${JAVA_SERVICES[@]}"; do
        IFS=':' read -r service_dir port service_name health <<< "$entry"
        if [ "$service_dir" = "$TARGET" ]; then found="$entry"; break; fi
    done

    if [ -z "$found" ]; then
        print_error "Servicio '$TARGET' no reconocido"
        print_info "Disponibles: eureka-server, api-gateway, ifit, ronnie, admin-panel"
        exit 1
    fi

    IFS=':' read -r service_dir port service_name health <<< "$found"
    print_header "REINICIANDO $service_name"
    echo ""

    # Parar el proceso que ocupa el puerto
    pid=$(get_pid_for_port "$port")
    if [ -n "$pid" ]; then
        print_info "Deteniendo $service_name (PID $pid)..."
        kill "$pid" 2>/dev/null
        sleep 3
        # Si sigue vivo, forzar
        kill -0 "$pid" 2>/dev/null && kill -9 "$pid" 2>/dev/null
        print_success "$service_name detenido"
    else
        print_info "$service_name no estaba corriendo"
    fi

    # Compilar y arrancar
    print_info "Compilando $service_name..."
    cd "$PROJECT_DIR/$service_dir" || exit 1
    if ! mvn clean package -q -DskipTests 2>/dev/null; then
        print_error "Error compilando $service_name"
        exit 1
    fi
    local_log="$(log_path_for "$service_dir")"
    nohup mvn spring-boot:run > "$local_log" 2>&1 &
    print_success "$service_name lanzado (PID $!)   Log: tail -f $local_log"
    cd "$PROJECT_DIR" || exit 1
    echo ""
    print_info "Verifica con: ./status-services.sh"
    exit 0
fi

# ── Reinicio completo ────────────────────────────────────────────────────────
print_header "REINICIANDO TODO EL SISTEMA iFIT"
echo ""

print_info "Paso 1/2 — Deteniendo servicios..."
echo ""
if [ -f "$SCRIPT_DIR/stop-services.sh" ]; then
    bash "$SCRIPT_DIR/stop-services.sh"
else
    pkill -f 'spring-boot:run' 2>/dev/null
    docker compose down 2>/dev/null
    sleep 2
fi
echo ""

print_info "Paso 2/2 — Levantando servicios..."
echo ""
if [ -f "$SCRIPT_DIR/start-services.sh" ]; then
    bash "$SCRIPT_DIR/start-services.sh"
else
    print_error "start-services.sh no encontrado"
    exit 1
fi
