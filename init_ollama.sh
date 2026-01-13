#!/bin/bash

# ============================================================================
# SCRIPT: Inicializar Ollama en Docker
# ============================================================================
#
# Este script descarga el modelo llama3.1:8b en el contenedor de Ollama
# después de que el contenedor esté corriendo.
#
# Uso:
#   chmod +x init-ollama.sh
#   ./init-ollama.sh
#
# ============================================================================

set -e

echo "🚀 Inicializando Ollama..."
echo ""

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Verificar que Docker está corriendo
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Error: Docker no está corriendo${NC}"
    exit 1
fi

# Verificar que el contenedor de Ollama existe y está corriendo
if ! docker ps | grep -q ifit-ollama; then
    echo -e "${RED}❌ Error: Contenedor ifit-ollama no está corriendo${NC}"
    echo ""
    echo "Por favor, inicia Docker Compose primero:"
    echo "  docker-compose up -d ollama"
    exit 1
fi

echo -e "${GREEN}✓${NC} Contenedor de Ollama encontrado"
echo ""

# Esperar a que Ollama esté listo
echo -e "${YELLOW}⏳ Esperando a que Ollama esté listo...${NC}"
timeout=60
elapsed=0
while ! docker exec ifit-ollama curl -s http://localhost:11434/api/tags > /dev/null 2>&1; do
    sleep 2
    elapsed=$((elapsed + 2))
    if [ $elapsed -ge $timeout ]; then
        echo -e "${RED}❌ Timeout: Ollama no respondió después de ${timeout}s${NC}"
        exit 1
    fi
    echo -n "."
done
echo ""
echo -e "${GREEN}✓${NC} Ollama está listo"
echo ""

# Verificar si el modelo ya está descargado
echo -e "${YELLOW}🔍 Verificando modelos descargados...${NC}"
if docker exec ifit-ollama ollama list | grep -q "llama3.1:8b"; then
    echo -e "${GREEN}✓${NC} El modelo llama3.1:8b ya está descargado"
    echo ""
    docker exec ifit-ollama ollama list
    exit 0
fi

# Descargar el modelo
echo ""
echo -e "${YELLOW}📥 Descargando modelo llama3.1:8b...${NC}"
echo -e "${YELLOW}   (Esto puede tardar varios minutos, ~4.7GB)${NC}"
echo ""

docker exec ifit-ollama ollama pull llama3.1:8b

echo ""
echo -e "${GREEN}✓${NC} Modelo descargado exitosamente"
echo ""

# Verificar instalación
echo -e "${YELLOW}📊 Modelos disponibles:${NC}"
docker exec ifit-ollama ollama list

echo ""
echo -e "${GREEN}✅ Ollama configurado correctamente${NC}"
echo ""
echo "Puedes probar el modelo con:"
echo "  docker exec ifit-ollama ollama run llama3.1:8b \"Hola, ¿cómo estás?\""
