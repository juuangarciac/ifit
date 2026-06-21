#!/bin/bash

# ============================================================================
# iFit Tests Runner - Script interactivo para ejecutar tests
# ============================================================================
#
# Uso:
#   bash run-tests.sh           (desde cualquier carpeta)
#   chmod +x run-tests.sh && ./run-tests.sh
#
# ============================================================================

set -e

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Get script directory (el script está EN la carpeta test)
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
TEST_DIR="$SCRIPT_DIR"

# Check if package.json exists
if [ ! -f "$TEST_DIR/package.json" ]; then
    echo -e "${RED}❌ Error: package.json not found in $TEST_DIR${NC}"
    exit 1
fi

clear

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                 iFit Test Suite Runner                      ║${NC}"
echo -e "${BLUE}╠════════════════════════════════════════════════════════════╣${NC}"
echo -e "${BLUE}║  Selecciona los tests que deseas ejecutar:                 ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

PS3=$'\n'"$(echo -e ${YELLOW}Selecciona una opción [1-11]:${NC} )"

options=(
    "🧪 MOCKS: Tests de Ronnie con mocks (SIN API real) - 17 tests"
    "🔐 AUTH: Tests de autenticación (register, login, logout) - 17 tests"
    "👥 USERS: Tests de usuarios (CRUD) - 13 tests"
    "🤖 RONNIE: Tests de Ronnie real (requiere Groq API) - 11 tests"
    "🔄 TODOS: Ejecutar todos los tests - 58 tests"
    "👁️  WATCH: Modo watch (tests se recargan automáticamente)"
    "📊 UI: Interfaz visual de tests (requiere navegador)"
    "📈 COVERAGE: Reporte de cobertura"
    "✅ RONNIE+MOCKS: Tests de Ronnie + mocks - 28 tests"
    "🚀 FULL: Todos los tests (una sola ejecución) - 58 tests"
    "❌ SALIR"
)

select opt in "${options[@]}"
do
    case $REPLY in
        1)
            echo -e "${YELLOW}🚀 Ejecutando: Tests de mocks de Ronnie...${NC}"
            echo ""
            cd "$TEST_DIR" && npm run test:ronnie:mock
            break
            ;;
        2)
            echo -e "${YELLOW}🚀 Ejecutando: Tests de autenticación...${NC}"
            echo ""
            cd "$TEST_DIR" && npm run test:auth
            break
            ;;
        3)
            echo -e "${YELLOW}🚀 Ejecutando: Tests de usuarios...${NC}"
            echo ""
            cd "$TEST_DIR" && npm run test:users
            break
            ;;
        4)
            echo -e "${YELLOW}🚀 Ejecutando: Tests de Ronnie real...${NC}"
            echo ""
            echo -e "${RED}⚠️  Nota: Requiere Groq API KEY configurado${NC}"
            echo ""
            cd "$TEST_DIR" && npm run test:ronnie
            break
            ;;
        5)
            echo -e "${YELLOW}🚀 Ejecutando: TODOS los tests...${NC}"
            echo ""
            cd "$TEST_DIR" && npm run test:run
            break
            ;;
        6)
            echo -e "${YELLOW}🚀 Ejecutando: Modo watch (Presiona Ctrl+C para salir)...${NC}"
            echo ""
            cd "$TEST_DIR" && npm test
            break
            ;;
        7)
            echo -e "${YELLOW}🚀 Ejecutando: Interfaz visual de tests...${NC}"
            echo ""
            echo -e "${BLUE}💡 Se abrirá una ventana en el navegador (http://localhost:51204)${NC}"
            echo ""
            cd "$TEST_DIR" && npm run test:ui
            break
            ;;
        8)
            echo -e "${YELLOW}🚀 Ejecutando: Reporte de cobertura...${NC}"
            echo ""
            cd "$TEST_DIR" && npm run test:coverage
            echo ""
            echo -e "${GREEN}✅ Coverage report generado en: $TEST_DIR/coverage/index.html${NC}"
            break
            ;;
        9)
            echo -e "${YELLOW}🚀 Ejecutando: Tests de Ronnie (mocks + reales)...${NC}"
            echo ""
            cd "$TEST_DIR" && npm run test:ronnie:mock && npm run test:ronnie
            break
            ;;
        10)
            echo -e "${YELLOW}🚀 Ejecutando: FULL - Todos los tests una sola vez...${NC}"
            echo ""
            cd "$TEST_DIR" && npm run test:all
            break
            ;;
        11)
            echo -e "${GREEN}👋 ¡Hasta luego!${NC}"
            exit 0
            ;;
        *)
            echo -e "${RED}❌ Opción inválida. Intenta de nuevo.${NC}"
            ;;
    esac
done

# Ask if want to run more tests
echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
read -p "$(echo -e ${YELLOW}¿Ejecutar más tests? (s/n): ${NC})" -n 1 -r
echo ""

if [[ $REPLY =~ ^[Ss]$ ]]; then
    exec "$0"
else
    echo -e "${GREEN}✅ ¡Gracias por usar iFit Test Runner!${NC}"
    exit 0
fi
