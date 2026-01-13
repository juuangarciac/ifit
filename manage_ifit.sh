#!/bin/bash

# ============================================================================
# SCRIPT: Gestión del Sistema IFit con Docker
# ============================================================================
#
# Script de ayuda para gestionar todos los contenedores del sistema IFit.
#
# Uso:
#   chmod +x manage-ifit.sh
#   ./manage-ifit.sh [comando]
#
# ============================================================================

set -e

# Colores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

# Banner
print_banner() {
    echo ""
    echo -e "${BLUE}╔════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║                                            ║${NC}"
    echo -e "${BLUE}║         🏋️  SISTEMA IFIT 🏋️               ║${NC}"
    echo -e "${BLUE}║         Gestión de Contenedores           ║${NC}"
    echo -e "${BLUE}║                                            ║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════════╝${NC}"
    echo ""
}

# Función: Mostrar ayuda
show_help() {
    print_banner
    echo "Comandos disponibles:"
    echo ""
    echo "  ${GREEN}start${NC}      - Iniciar todo el sistema"
    echo "  ${GREEN}stop${NC}       - Detener todo el sistema"
    echo "  ${GREEN}restart${NC}    - Reiniciar todo el sistema"
    echo "  ${GREEN}status${NC}     - Ver estado de todos los servicios"
    echo "  ${GREEN}logs${NC}       - Ver logs de todos los servicios"
    echo "  ${GREEN}build${NC}      - Reconstruir todas las imágenes"
    echo "  ${GREEN}clean${NC}      - Limpiar contenedores y volúmenes"
    echo "  ${GREEN}reset${NC}      - Reset completo (⚠️ elimina datos)"
    echo "  ${GREEN}init-ollama${NC} - Inicializar Ollama con el modelo"
    echo "  ${GREEN}health${NC}     - Verificar salud de servicios"
    echo ""
    echo "Ejemplos:"
    echo "  ./manage-ifit.sh start"
    echo "  ./manage-ifit.sh logs gateway"
    echo "  ./manage-ifit.sh restart ronnie"
    echo ""
}

# Función: Iniciar sistema
start_system() {
    print_banner
    echo -e "${YELLOW}🚀 Iniciando sistema IFit...${NC}"
    echo ""
    
    # Construir si es la primera vez
    if [ ! -f ".docker-built" ]; then
        echo -e "${YELLOW}📦 Primera ejecución: construyendo imágenes...${NC}"
        docker-compose build
        touch .docker-built
        echo ""
    fi
    
    # Iniciar servicios en orden
    echo -e "${YELLOW}⬆️  Iniciando servicios base...${NC}"
    docker-compose up -d mysql keycloak ollama
    echo ""
    
    echo -e "${YELLOW}⏳ Esperando a que servicios base estén listos (30s)...${NC}"
    sleep 30
    echo ""
    
    echo -e "${YELLOW}⬆️  Iniciando Eureka...${NC}"
    docker-compose up -d eureka
    echo ""
    
    echo -e "${YELLOW}⏳ Esperando a que Eureka esté listo (20s)...${NC}"
    sleep 20
    echo ""
    
    echo -e "${YELLOW}⬆️  Iniciando microservicios...${NC}"
    docker-compose up -d gateway authapi ifit ronnie
    echo ""
    
    echo -e "${GREEN}✅ Sistema iniciado${NC}"
    echo ""
    echo "Servicios disponibles en:"
    echo "  - Eureka:     http://localhost:8761"
    echo "  - Gateway:    http://localhost:8080"
    echo "  - AuthAPI:    http://localhost:8083"
    echo "  - IFIT:       http://localhost:8081"
    echo "  - RONNIE:     http://localhost:8082"
    echo "  - Keycloak:   http://localhost:9090"
    echo "  - Ollama:     http://localhost:11434"
    echo ""
    echo "Para ver logs: ./manage-ifit.sh logs"
    echo ""
}

# Función: Detener sistema
stop_system() {
    print_banner
    echo -e "${YELLOW}⏹️  Deteniendo sistema IFit...${NC}"
    docker-compose stop
    echo -e "${GREEN}✅ Sistema detenido${NC}"
    echo ""
}

# Función: Reiniciar sistema
restart_system() {
    print_banner
    echo -e "${YELLOW}🔄 Reiniciando sistema IFit...${NC}"
    
    if [ -n "$1" ]; then
        echo "Reiniciando servicio: $1"
        docker-compose restart "$1"
    else
        docker-compose restart
    fi
    
    echo -e "${GREEN}✅ Sistema reiniciado${NC}"
    echo ""
}

# Función: Ver estado
show_status() {
    print_banner
    echo -e "${YELLOW}📊 Estado de servicios:${NC}"
    echo ""
    docker-compose ps
    echo ""
}

# Función: Ver logs
show_logs() {
    if [ -n "$1" ]; then
        echo -e "${YELLOW}📜 Logs de $1:${NC}"
        docker-compose logs -f --tail=100 "$1"
    else
        echo -e "${YELLOW}📜 Logs de todos los servicios:${NC}"
        docker-compose logs -f --tail=50
    fi
}

# Función: Reconstruir imágenes
rebuild_images() {
    print_banner
    echo -e "${YELLOW}🔨 Reconstruyendo imágenes...${NC}"
    echo ""
    docker-compose build --no-cache
    rm -f .docker-built
    echo ""
    echo -e "${GREEN}✅ Imágenes reconstruidas${NC}"
    echo ""
}

# Función: Limpiar
clean_system() {
    print_banner
    echo -e "${RED}⚠️  ADVERTENCIA: Esto detendrá y eliminará contenedores${NC}"
    echo -e "${YELLOW}   (Los volúmenes se mantendrán)${NC}"
    echo ""
    read -p "¿Continuar? (y/N): " -n 1 -r
    echo ""
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker-compose down
        rm -f .docker-built
        echo -e "${GREEN}✅ Sistema limpiado${NC}"
    else
        echo "Operación cancelada"
    fi
    echo ""
}

# Función: Reset completo
reset_system() {
    print_banner
    echo -e "${RED}⚠️  ADVERTENCIA: RESET COMPLETO${NC}"
    echo -e "${RED}   Esto eliminará:${NC}"
    echo -e "${RED}   - Todos los contenedores${NC}"
    echo -e "${RED}   - Todas las imágenes de IFit${NC}"
    echo -e "${RED}   - Todos los volúmenes (⚠️ DATOS DE BD)${NC}"
    echo ""
    read -p "¿Estás SEGURO? Escribe 'RESET' para confirmar: " confirmation
    
    if [ "$confirmation" = "RESET" ]; then
        echo ""
        echo -e "${YELLOW}🗑️  Eliminando todo...${NC}"
        docker-compose down -v --rmi local
        rm -f .docker-built
        echo ""
        echo -e "${GREEN}✅ Reset completo realizado${NC}"
    else
        echo "Operación cancelada"
    fi
    echo ""
}

# Función: Inicializar Ollama
init_ollama() {
    print_banner
    echo -e "${YELLOW}🤖 Inicializando Ollama...${NC}"
    echo ""
    
    if [ -f "init-ollama.sh" ]; then
        chmod +x init-ollama.sh
        ./init-ollama.sh
    else
        echo -e "${RED}❌ Error: Script init-ollama.sh no encontrado${NC}"
    fi
}

# Función: Health check
health_check() {
    print_banner
    echo -e "${YELLOW}🏥 Verificando salud de servicios...${NC}"
    echo ""
    
    services=("mysql:3306" "keycloak:9090" "ollama:11434" "eureka:8761" "gateway:8080" "authapi:8083" "ifit:8081" "ronnie:8082")
    
    for service in "${services[@]}"; do
        name=$(echo $service | cut -d: -f1)
        port=$(echo $service | cut -d: -f2)
        
        if nc -z localhost $port 2>/dev/null; then
            echo -e "  ${GREEN}✓${NC} $name (puerto $port) - OK"
        else
            echo -e "  ${RED}✗${NC} $name (puerto $port) - NO RESPONDE"
        fi
    done
    echo ""
}

# Main
case "$1" in
    start)
        start_system
        ;;
    stop)
        stop_system
        ;;
    restart)
        restart_system "$2"
        ;;
    status)
        show_status
        ;;
    logs)
        show_logs "$2"
        ;;
    build)
        rebuild_images
        ;;
    clean)
        clean_system
        ;;
    reset)
        reset_system
        ;;
    init-ollama)
        init_ollama
        ;;
    health)
        health_check
        ;;
    *)
        show_help
        ;;
esac
