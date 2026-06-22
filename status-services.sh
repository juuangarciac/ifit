#!/bin/bash

# ============================================================================
# SCRIPT: Estado detallado de los servicios iFit
# ============================================================================
# Uso: ./status-services.sh
#   Muestra por cada servicio: estado, PID, RAM, CPU, uptime y health HTTP.
#   Además: recursos Docker, métricas del sistema y registro de Eureka.
#
# Consejo: para monitorización en vivo -> watch -n 2 ./status-services.sh
# ============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/lib-services.sh"

OK=0
FAIL=0

print_header "ESTADO DE SERVICIOS iFIT — $(date '+%Y-%m-%d %H:%M:%S')"
echo ""

# ── Servicios Docker ─────────────────────────────────────────────────────────
print_header "Docker (infraestructura)"
printf "  ${BOLD}%-12s %-8s %-10s %-9s %-18s %s${NC}\n" "SERVICIO" "ESTADO" "SALUD" "CPU" "MEMORIA" "UPTIME"
for entry in "${DOCKER_SERVICES[@]}"; do
    IFS=':' read -r container port name <<< "$entry"
    if is_docker_running "$container"; then
        OK=$((OK+1))
        health=$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}n/a{{end}}' "$container" 2>/dev/null)
        read -r cpu mem < <(docker stats --no-stream --format '{{.CPUPerc}}|{{.MemUsage}}' "$container" 2>/dev/null | awk -F'|' '{print $1" "$2}')
        uptime=$(docker ps --filter "name=^${container}$" --format '{{.Status}}' 2>/dev/null)
        local_color=$GREEN; [ "$health" = "unhealthy" ] && local_color=$YELLOW
        printf "  ${local_color}%-12s${NC} %-8s %-10s %-9s %-18s %s\n" "$name" "UP" "$health" "${cpu:-–}" "${mem:-–}" "${uptime#Up }"
    else
        FAIL=$((FAIL+1))
        printf "  ${RED}%-12s %-8s${NC}\n" "$name" "DOWN"
    fi
done
echo ""

# ── Servicios Java ───────────────────────────────────────────────────────────
print_header "Java (microservicios)"
printf "  ${BOLD}%-14s %-6s %-7s %-8s %-7s %-11s %s${NC}\n" "SERVICIO" "PUERTO" "PID" "RAM(MB)" "CPU%" "UPTIME" "HEALTH"
for entry in "${JAVA_SERVICES[@]}"; do
    IFS=':' read -r service_dir port service_name health <<< "$entry"
    if is_port_open "$port"; then
        OK=$((OK+1))
        pid=$(get_pid_for_port "$port")
        read -r ram cpu up < <(get_process_stats "$pid")
        # Health HTTP (si el servicio expone endpoint)
        hstr="—"
        if [ -n "$health" ]; then
            read -r code time <<< "$(http_health "http://localhost:${port}${health}")"
            if [ "$code" = "200" ]; then
                hstr="${GREEN}UP${NC} (${time}s)"
            elif [ "$code" = "000" ]; then
                hstr="${YELLOW}sin resp.${NC}"
            else
                hstr="${YELLOW}HTTP ${code}${NC}"
            fi
        fi
        printf "  ${GREEN}%-14s${NC} %-6s %-7s %-8s %-7s %-11s %b\n" \
            "$service_name" "$port" "${pid:-–}" "$ram" "$cpu" "$up" "$hstr"
    else
        FAIL=$((FAIL+1))
        printf "  ${RED}%-14s %-6s %-7s${NC}\n" "$service_name" "$port" "DOWN"
    fi
done
echo ""

# ── Registro de Eureka ───────────────────────────────────────────────────────
if is_port_open 8761 && command -v curl &>/dev/null; then
    print_header "Eureka — servicios registrados"
    apps=$(curl -s --max-time 4 -H "Accept: application/json" "http://localhost:8761/eureka/apps" 2>/dev/null)
    if [ -n "$apps" ]; then
        # Extrae pares app/status de forma tolerante (sin jq)
        echo "$apps" | grep -oE '"(app|status)":"[^"]+"' | paste - - 2>/dev/null \
            | sed -E 's/"app":"([^"]+)".*"status":"([^"]+)"/  • \1 → \2/' \
            | sort -u
        count=$(echo "$apps" | grep -oE '"instanceId":"[^"]+"' | wc -l)
        echo -e "  ${CYAN}Total instancias registradas: ${count}${NC}"
    else
        print_warning "Eureka responde pero no devolvió datos (¿aún arrancando?)"
    fi
    echo ""
fi

# ── Sistema ──────────────────────────────────────────────────────────────────
print_header "Sistema"
if command -v free &>/dev/null; then
    read -r mem_total mem_used mem_free < <(free -m | awk '/^Mem:/{print $2" "$3" "$4}')
    echo -e "  ${CYAN}RAM:${NC}   ${mem_used}MB usados / ${mem_total}MB totales (${mem_free}MB libres)"
fi
if command -v uptime &>/dev/null; then
    echo -e "  ${CYAN}Carga:${NC} $(uptime | sed -E 's/.*load average: //')"
fi
disk=$(df -h "$PROJECT_DIR" 2>/dev/null | awk 'NR==2{print $3" / "$2" ("$5" usado)"}')
[ -n "$disk" ] && echo -e "  ${CYAN}Disco:${NC} $disk"
echo ""

# ── Resumen ──────────────────────────────────────────────────────────────────
print_header "RESUMEN"
echo -e "  ${GREEN}Activos:${NC} $OK    ${RED}Caídos:${NC} $FAIL"
echo ""
if [ "$FAIL" -eq 0 ]; then
    print_success "Todos los servicios están operativos"
else
    print_warning "Hay $FAIL servicio(s) caído(s). Levántalos con ./start-services.sh"
    print_info "Diagnostica con: ./help-services.sh <servicio>"
fi
