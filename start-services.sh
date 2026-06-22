#!/bin/bash

# ============================================================================
# SCRIPT: Levanta el sistema iFit completo
# ============================================================================
# Uso: ./start-services.sh
# Verifica qué servicios están corriendo antes de levantar nuevos
# (no relanza los que ya están activos).
# ============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/lib-services.sh"
cd "$PROJECT_DIR" || exit 1

START_WAIT="${IFIT_START_WAIT:-60}"   # segundos máx. de espera por servicio Java

print_header "INICIANDO SERVICIOS iFIT"
echo ""

# ── 1. Verificar Docker ──────────────────────────────────────────────────────
print_info "Verificando Docker..."
if ! command -v docker &>/dev/null; then
    print_error "Docker no está instalado"
    echo "  Instala Docker desde: https://docs.docker.com/get-docker/"
    exit 1
fi
print_success "Docker encontrado"
echo ""

# ── 2. Levantar servicios Docker ─────────────────────────────────────────────
print_header "Servicios Docker (MySQL, Keycloak)"

all_docker_running=true
for entry in "${DOCKER_SERVICES[@]}"; do
    IFS=':' read -r container port name <<< "$entry"
    if is_docker_running "$container"; then
        print_success "$container ya está corriendo"
    else
        all_docker_running=false
        print_info "$container no está corriendo, se levantará"
    fi
done

if [ "$all_docker_running" = false ]; then
    print_info "Ejecutando docker compose up -d..."
    docker compose up -d
    print_success "Servicios Docker levantados"
    print_info "Esperando a que estén listos (30s)..."
    sleep 30
else
    print_info "Todos los servicios Docker ya están corriendo"
fi
echo ""

# ── 3. Verificar Java ────────────────────────────────────────────────────────
print_header "Verificando Java"
if ! command -v java &>/dev/null; then
    print_error "Java no está instalado"
    echo "  Instala Java 21+: apt install openjdk-21-jdk"
    exit 1
fi
print_success "Java encontrado: $(java -version 2>&1 | head -1)"
echo ""

# ── 4. Levantar servicios Java ───────────────────────────────────────────────
print_header "Servicios Java"

for entry in "${JAVA_SERVICES[@]}"; do
    IFS=':' read -r service_dir port service_name health <<< "$entry"

    if is_port_open "$port"; then
        print_success "$service_name (puerto $port) ya está corriendo"
        continue
    fi

    print_info "Compilando y levantando $service_name..."

    if [ ! -d "$PROJECT_DIR/$service_dir" ]; then
        print_error "Directorio $service_dir no encontrado, se omite"
        continue
    fi

    cd "$PROJECT_DIR/$service_dir" || continue

    # Compilar
    if ! mvn clean package -q -DskipTests 2>/dev/null; then
        print_error "Error compilando $service_name (revisa: mvn -f $service_dir clean package)"
        cd "$PROJECT_DIR" || exit 1
        continue
    fi

    # Ejecutar en background
    local_log="$(log_path_for "$service_dir")"
    nohup mvn spring-boot:run > "$local_log" 2>&1 &
    print_info "  PID: $!   Log: tail -f $local_log"

    # Esperar a que el puerto abra
    print_info "  Esperando a que esté listo (máx ${START_WAIT}s)..."
    ready=false
    for ((i=1; i<=START_WAIT; i++)); do
        if is_port_open "$port"; then
            print_success "$service_name está listo (${i}s)"
            ready=true
            break
        fi
        sleep 1
    done
    [ "$ready" = false ] && print_warning "$service_name no abrió el puerto en ${START_WAIT}s (sigue compilando/arrancando; revisa el log)"

    cd "$PROJECT_DIR" || exit 1
done
echo ""

# ── 5. Resumen ───────────────────────────────────────────────────────────────
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
print_info "Consulta el estado detallado con: ./status-services.sh"
print_info "Comandos útiles por servicio:       ./help-services.sh"
echo ""
print_success "Arranque finalizado"
