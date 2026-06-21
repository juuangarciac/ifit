# ============================================================================
# iFit Tests Runner - Script interactivo para ejecutar tests (PowerShell)
# ============================================================================
#
# Uso:
#   .\run-tests.ps1           (desde cualquier carpeta)
#
# ============================================================================

# Colors
$Green = "Green"
$Blue = "Blue"
$Yellow = "Yellow"
$Red = "Red"
$White = "White"

# Get script directory (el script está EN la carpeta test)
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$testDir = $scriptDir

# Check if package.json exists
if (-not (Test-Path (Join-Path $testDir "package.json"))) {
    Write-Host "❌ Error: package.json not found in $testDir" -ForegroundColor Red
    exit 1
}

Clear-Host

Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Blue
Write-Host "║                 iFit Test Suite Runner                      ║" -ForegroundColor Blue
Write-Host "╠════════════════════════════════════════════════════════════╣" -ForegroundColor Blue
Write-Host "║  Selecciona los tests que deseas ejecutar:                 ║" -ForegroundColor Blue
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Blue
Write-Host ""

$options = @(
    "🧪 MOCKS: Tests de Ronnie con mocks (SIN API real) - 17 tests",
    "🔐 AUTH: Tests de autenticación (register, login, logout) - 17 tests",
    "👥 USERS: Tests de usuarios (CRUD) - 13 tests",
    "🤖 RONNIE: Tests de Ronnie real (requiere Groq API) - 11 tests",
    "🔄 TODOS: Ejecutar todos los tests - 58 tests",
    "👁️  WATCH: Modo watch (tests se recargan automáticamente)",
    "📊 UI: Interfaz visual de tests (requiere navegador)",
    "📈 COVERAGE: Reporte de cobertura",
    "✅ RONNIE+MOCKS: Tests de Ronnie + mocks - 28 tests",
    "🚀 FULL: Todos los tests (una sola ejecución) - 58 tests",
    "❌ SALIR"
)

for ($i = 0; $i -lt $options.Count; $i++) {
    Write-Host "$($i+1). $($options[$i])"
}

Write-Host ""
$choice = Read-Host "Selecciona una opción [1-$($options.Count)]"

switch ($choice) {
    "1" {
        Write-Host "🚀 Ejecutando: Tests de mocks de Ronnie..." -ForegroundColor Yellow
        Write-Host ""
        Set-Location $testDir
        npm run test:ronnie:mock
    }
    "2" {
        Write-Host "🚀 Ejecutando: Tests de autenticación..." -ForegroundColor Yellow
        Write-Host ""
        Set-Location $testDir
        npm run test:auth
    }
    "3" {
        Write-Host "🚀 Ejecutando: Tests de usuarios..." -ForegroundColor Yellow
        Write-Host ""
        Set-Location $testDir
        npm run test:users
    }
    "4" {
        Write-Host "🚀 Ejecutando: Tests de Ronnie real..." -ForegroundColor Yellow
        Write-Host ""
        Write-Host "⚠️  Nota: Requiere Groq API KEY configurado" -ForegroundColor Red
        Write-Host ""
        Set-Location $testDir
        npm run test:ronnie
    }
    "5" {
        Write-Host "🚀 Ejecutando: TODOS los tests..." -ForegroundColor Yellow
        Write-Host ""
        Set-Location $testDir
        npm run test:run
    }
    "6" {
        Write-Host "🚀 Ejecutando: Modo watch (Presiona Ctrl+C para salir)..." -ForegroundColor Yellow
        Write-Host ""
        Set-Location $testDir
        npm test
    }
    "7" {
        Write-Host "🚀 Ejecutando: Interfaz visual de tests..." -ForegroundColor Yellow
        Write-Host ""
        Write-Host "💡 Se abrirá una ventana en el navegador (http://localhost:51204)" -ForegroundColor Blue
        Write-Host ""
        Set-Location $testDir
        npm run test:ui
    }
    "8" {
        Write-Host "🚀 Ejecutando: Reporte de cobertura..." -ForegroundColor Yellow
        Write-Host ""
        Set-Location $testDir
        npm run test:coverage
        Write-Host ""
        Write-Host "✅ Coverage report generado en: $testDir\coverage\index.html" -ForegroundColor Green
    }
    "9" {
        Write-Host "🚀 Ejecutando: Tests de Ronnie (mocks + reales)..." -ForegroundColor Yellow
        Write-Host ""
        Set-Location $testDir
        npm run test:ronnie:mock
        npm run test:ronnie
    }
    "10" {
        Write-Host "🚀 Ejecutando: FULL - Todos los tests una sola vez..." -ForegroundColor Yellow
        Write-Host ""
        Set-Location $testDir
        npm run test:all
    }
    "11" {
        Write-Host "👋 ¡Hasta luego!" -ForegroundColor Green
        exit 0
    }
    default {
        Write-Host "❌ Opción inválida. Intenta de nuevo." -ForegroundColor Red
    }
}

# Ask if want to run more tests
Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Blue
$again = Read-Host "¿Ejecutar más tests? (s/n)"

if ($again -eq "s" -or $again -eq "S") {
    & $MyInvocation.MyCommand.Path
} else {
    Write-Host "✅ ¡Gracias por usar iFit Test Runner!" -ForegroundColor Green
    exit 0
}
