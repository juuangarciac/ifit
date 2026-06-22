#!/bin/bash

# ============================================================================
# SCRIPT: Chuleta de comandos para gestionar el servidor iFit
# ============================================================================
# Uso:
#   ./help-services.sh                 Muestra TODA la referencia
#   ./help-services.sh <servicio>      Comandos centrados en un servicio
#                                      (eureka-server|api-gateway|ifit|ronnie|
#                                       admin-panel|mysql|keycloak)
# ============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/lib-services.sh"

TARGET="$1"

# Imprime el bloque de comandos para un servicio Java.
java_service_help() {
    local dir="$1" port="$2" name="$3" health="$4"
    local log; log="$(log_path_for "$dir")"

    echo -e "${BOLD}${GREEN}▌ $name${NC}  ${CYAN}(dir: $dir · puerto: $port)${NC}"
    echo -e "${BOLD}  Estado y recursos${NC}"
    echo "    ss -ltnp 'sport = :$port'                 # ¿escucha el puerto? + PID"
    echo "    ps -o pid,rss,pcpu,etime -p \$(ss -ltnpH 'sport = :$port' | grep -oP 'pid=\\K[0-9]+')   # RAM/CPU/uptime"
    echo "    watch -n 2 \"curl -s -o /dev/null -w '%{http_code} %{time_total}s\\n' http://localhost:$port${health:-/}\"   # ping continuo"
    echo -e "${BOLD}  Logs${NC}"
    echo "    tail -f $log                              # log en vivo"
    echo "    tail -n 200 $log                          # últimas 200 líneas"
    echo "    grep -iE 'error|exception|fail' $log      # buscar errores"
    echo "    grep -i 'started .* in' $log              # confirmar arranque OK"
    if [ -n "$health" ]; then
        echo -e "${BOLD}  Salud (actuator)${NC}"
        echo "    curl -s http://localhost:$port$health | jq .   # estado (o sin jq: | python3 -m json.tool)"
    fi
    echo -e "${BOLD}  Tráfico entrante (peticiones HTTP)${NC}"
    echo "    sudo tcpdump -i any -nn -A 'tcp port $port' | grep -E 'GET|POST|PUT|DELETE'   # ver requests crudas"
    echo "    ss -tnp 'dport = :$port or sport = :$port'   # conexiones activas hacia/desde el servicio"
    echo -e "${BOLD}  Control${NC}"
    echo "    ./restart-services.sh $dir                # reiniciar SOLO este servicio"
    echo "    kill \$(ss -ltnpH 'sport = :$port' | grep -oP 'pid=\\K[0-9]+')   # detenerlo"
    echo ""
}

# Imprime el bloque de comandos para un contenedor Docker.
docker_service_help() {
    local container="$1" port="$2" name="$3"
    echo -e "${BOLD}${GREEN}▌ $name${NC}  ${CYAN}(contenedor: $container · puerto: $port)${NC}"
    echo -e "${BOLD}  Estado y recursos${NC}"
    echo "    docker ps --filter name=$container                 # estado y uptime"
    echo "    docker stats $container                            # CPU/RAM en vivo"
    echo "    docker inspect --format '{{.State.Health.Status}}' $container   # healthcheck"
    echo -e "${BOLD}  Logs${NC}"
    echo "    docker logs -f $container                          # log en vivo"
    echo "    docker logs --tail 200 $container                  # últimas 200 líneas"
    echo "    docker logs $container 2>&1 | grep -iE 'error|warn' # errores"
    echo -e "${BOLD}  Control${NC}"
    echo "    docker restart $container                          # reiniciar"
    echo "    docker exec -it $container bash                     # entrar al contenedor"
    if [ "$container" = "ifit-mysql" ]; then
        echo -e "${BOLD}  MySQL${NC}"
        echo "    docker exec -it ifit-mysql mysql -uroot -proot ifit   # consola SQL"
        echo "    docker exec ifit-mysql mysqladmin -uroot -proot status # estado del motor"
    fi
    if [ "$container" = "ifit-keycloak" ]; then
        echo -e "${BOLD}  Keycloak${NC}"
        echo "    curl -s http://localhost:$port/health/ready        # readiness"
        echo "    Admin UI: http://localhost:$port  (admin / admin)"
    fi
    echo ""
}

print_general() {
    print_header "GESTIÓN GENERAL DEL SERVIDOR iFIT"
    echo ""
    echo -e "${BOLD}${CYAN}Scripts del proyecto${NC}"
    echo "    ./start-services.sh           Levanta todo (no relanza lo ya activo)"
    echo "    ./stop-services.sh            Detiene todo  (--java | --docker para parte)"
    echo "    ./restart-services.sh         Reinicia todo (o '<servicio>' para uno solo)"
    echo "    ./status-services.sh          Estado detallado (PID/RAM/CPU/health)"
    echo "    watch -n 2 ./status-services.sh   Monitor en vivo del estado global"
    echo ""
    echo -e "${BOLD}${CYAN}Visión global del sistema${NC}"
    echo "    htop                          Monitor interactivo de CPU/RAM (apt install htop)"
    echo "    docker stats                  CPU/RAM de todos los contenedores en vivo"
    echo "    tail -f /tmp/*.log            TODOS los logs Java a la vez"
    echo "    ss -ltnp                      Todos los puertos en escucha + PID"
    echo "    df -h / && free -m            Disco y memoria del servidor"
    echo "    journalctl -f                 Log del sistema en vivo"
    echo ""
    echo -e "${BOLD}${CYAN}Red y peticiones${NC}"
    echo "    sudo tcpdump -i any -nn 'tcp port 8080'   Espiar tráfico del Gateway"
    echo "    ss -tn state established                   Conexiones activas"
    echo "    curl -sI http://localhost:8080            Comprobar que el Gateway responde"
    echo ""
}

# ── Lógica principal ─────────────────────────────────────────────────────────
if [ -z "$TARGET" ]; then
    print_general
    print_header "COMANDOS POR SERVICIO JAVA"
    echo ""
    for entry in "${JAVA_SERVICES[@]}"; do
        IFS=':' read -r dir port name health <<< "$entry"
        java_service_help "$dir" "$port" "$name" "$health"
    done
    print_header "COMANDOS POR SERVICIO DOCKER"
    echo ""
    for entry in "${DOCKER_SERVICES[@]}"; do
        IFS=':' read -r container port name <<< "$entry"
        docker_service_help "$container" "$port" "$name"
    done
    print_info "Sugerencia: ./help-services.sh ifit   (para ver solo un servicio)"
    exit 0
fi

# Servicio concreto
case "$TARGET" in
    eureka-server|api-gateway|ifit|ronnie|admin-panel)
        for entry in "${JAVA_SERVICES[@]}"; do
            IFS=':' read -r dir port name health <<< "$entry"
            [ "$dir" = "$TARGET" ] && { print_header "AYUDA — $name"; echo ""; java_service_help "$dir" "$port" "$name" "$health"; }
        done
        ;;
    mysql|ifit-mysql)
        print_header "AYUDA — MySQL"; echo ""; docker_service_help "ifit-mysql" "3306" "MySQL" ;;
    keycloak|ifit-keycloak)
        print_header "AYUDA — Keycloak"; echo ""; docker_service_help "ifit-keycloak" "9090" "Keycloak" ;;
    *)
        print_error "Servicio '$TARGET' no reconocido"
        print_info "Opciones: eureka-server, api-gateway, ifit, ronnie, admin-panel, mysql, keycloak"
        exit 1
        ;;
esac
