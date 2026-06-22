# ============================================================================
# SCRIPT: Levanta el sistema iFit completo (Windows/PowerShell)
# ============================================================================
# Uso: .\start-services.ps1
# Verifica qué servicios están corriendo antes de levantar nuevos
# ============================================================================

$ErrorActionPreference = "Continue"

# Colores
$SUCCESS = "Green"
$ERROR = "Red"
$WARNING = "Yellow"
$INFO = "Cyan"

$ProjectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectDir

# ============================================================================
# FUNCIONES AUXILIARES
# ============================================================================

function Print-Header {
    param([string]$Text)
    Write-Host "========================================" -ForegroundColor $INFO
    Write-Host $Text -ForegroundColor $INFO
    Write-Host "========================================" -ForegroundColor $INFO
}

function Print-Success {
    param([string]$Text)
    Write-Host "✓ $Text" -ForegroundColor $SUCCESS
}

function Print-Error {
    param([string]$Text)
    Write-Host "✗ $Text" -ForegroundColor $ERROR
}

function Print-Warning {
    param([string]$Text)
    Write-Host "⚠ $Text" -ForegroundColor $WARNING
}

function Print-Info {
    param([string]$Text)
    Write-Host "ℹ $Text" -ForegroundColor $INFO
}

# Verificar si un puerto está en uso
function Test-PortOpen {
    param([int]$Port)
    try {
        $tcpClient = New-Object System.Net.Sockets.TcpClient
        $tcpClient.Connect("localhost", $Port)
        $tcpClient.Close()
        return $true
    }
    catch {
        return $false
    }
}

# Verificar si un contenedor Docker está corriendo
function Test-DockerRunning {
    param([string]$ContainerName)
    $result = & docker ps --format "{{.Names}}" 2>$null | Select-String "^$ContainerName$"
    return $result -ne $null
}

# ============================================================================
# CONFIGURACIÓN
# ============================================================================

$DockerServices = @("ifit-mysql", "ifit-keycloak")
$JavaServices = @(
    @{ Dir = "api-gateway"; Port = 8080; Name = "API Gateway" },
    @{ Dir = "ifit"; Port = 8081; Name = "iFit" },
    @{ Dir = "ronnie"; Port = 8082; Name = "Ronnie" },
    @{ Dir = "admin-panel"; Port = 8084; Name = "Admin Panel" }
)

# ============================================================================
# INICIO DEL SCRIPT
# ============================================================================

Print-Header "INICIANDO SERVICIOS iFIT"
Write-Host ""

# ============================================================================
# 1. VERIFICAR DOCKER
# ============================================================================

Print-Info "Verificando Docker..."
try {
    $dockerVersion = & docker --version 2>$null
    if ($LASTEXITCODE -ne 0) {
        throw "Docker no disponible"
    }
    Print-Success "Docker encontrado: $dockerVersion"
}
catch {
    Print-Error "Docker no está instalado o no es accesible"
    Write-Host "  Instala Docker Desktop desde: https://www.docker.com/products/docker-desktop"
    exit 1
}
Write-Host ""

# ============================================================================
# 2. LEVANTAR SERVICIOS DOCKER
# ============================================================================

Print-Header "Servicios Docker (MySQL, Keycloak)"

$allDockerRunning = $true
foreach ($service in $DockerServices) {
    if (Test-DockerRunning $service) {
        Print-Success "$service ya está corriendo"
    }
    else {
        $allDockerRunning = $false
        Print-Info "Levantando $service..."
    }
}

if (-not $allDockerRunning) {
    Print-Info "Ejecutando docker compose up -d..."
    & docker compose up -d
    Print-Success "Servicios Docker levantados"
    Print-Info "Esperando a que estén listos (30s)..."
    Start-Sleep -Seconds 30
}
else {
    Print-Info "Todos los servicios Docker ya están corriendo"
}

Write-Host ""

# ============================================================================
# 3. VERIFICAR JAVA
# ============================================================================

Print-Header "Verificando Java"
try {
    $javaVersion = & java -version 2>&1 | Select-String "version"
    Print-Success "Java encontrado: $javaVersion"
}
catch {
    Print-Error "Java no está instalado"
    Write-Host "  Instala Java 17+: https://www.oracle.com/java/technologies/downloads/"
    exit 1
}
Write-Host ""

# ============================================================================
# 4. LEVANTAR SERVICIOS JAVA
# ============================================================================

Print-Header "Servicios Java"

foreach ($service in $JavaServices) {
    $serviceDir = $service.Dir
    $port = $service.Port
    $serviceName = $service.Name

    if (Test-PortOpen $port) {
        Print-Success "$serviceName (puerto $port) ya está corriendo"
    }
    else {
        Print-Info "Compilando y levantando $serviceName..."

        if (-not (Test-Path $serviceDir)) {
            Print-Error "Directorio $serviceDir no encontrado"
            continue
        }

        Push-Location $serviceDir

        # Compilar
        Write-Host "  Compilando..." -ForegroundColor $INFO
        & mvn clean package -q -DskipTests 2>$null
        if ($LASTEXITCODE -ne 0) {
            Print-Error "Error compilando $serviceName"
            Pop-Location
            continue
        }

        # Ejecutar en background
        Write-Host "  Levantando $serviceName..." -ForegroundColor $INFO
        $logFile = "$env:TEMP\${serviceDir}.log"
        Start-Process -NoNewWindow -RedirectStandardOutput $logFile -RedirectStandardError $logFile `
            -FileName "mvn" -ArgumentList "spring-boot:run" -PassThru | Out-Null

        Print-Info "  Log: $logFile"

        # Esperar a que esté listo
        Print-Info "  Esperando a que esté listo..."
        $ready = $false
        for ($i = 1; $i -le 30; $i++) {
            if (Test-PortOpen $port) {
                Print-Success "$serviceName está listo"
                $ready = $true
                break
            }
            Start-Sleep -Seconds 1
        }

        if (-not $ready) {
            Print-Warning "$serviceName tardó más de 30s en iniciar. Revisar log: $logFile"
        }

        Pop-Location
    }
}

Write-Host ""

# ============================================================================
# 5. RESUMEN Y STATUS
# ============================================================================

Print-Header "RESUMEN DE SERVICIOS"
Write-Host ""

Print-Info "Docker Services:"
foreach ($service in $DockerServices) {
    if (Test-DockerRunning $service) {
        Write-Host "  ✓ $service" -ForegroundColor $SUCCESS
    }
    else {
        Write-Host "  ✗ $service" -ForegroundColor $ERROR
    }
}

Write-Host ""
Print-Info "Java Services:"
foreach ($service in $JavaServices) {
    $serviceDir = $service.Dir
    $port = $service.Port
    $serviceName = $service.Name

    if (Test-PortOpen $port) {
        Write-Host "  ✓ $serviceName (http://localhost:$port)" -ForegroundColor $SUCCESS
    }
    else {
        Write-Host "  ✗ $serviceName (puerto $port)" -ForegroundColor $ERROR
    }
}

Write-Host ""

# ============================================================================
# 6. INFORMACIÓN ÚTIL
# ============================================================================

Print-Header "ACCESO A SERVICIOS"
Write-Host ""
Write-Host "  🔐 Keycloak:      http://localhost:9090"
Write-Host "  🏠 Admin Panel:    http://localhost:8084"
Write-Host "  🔌 iFit API:       http://localhost:8081"
Write-Host "  🤖 Ronnie:         http://localhost:8082"
Write-Host "  🌐 API Gateway:    http://localhost:8080"
Write-Host ""

Print-Info "Ver logs:"
Write-Host "  docker logs -f ifit-mysql"
Write-Host "  docker logs -f ifit-keycloak"
Write-Host "  Get-Content `$env:TEMP\api-gateway.log -Tail 20 -Wait"
Write-Host "  Get-Content `$env:TEMP\ifit.log -Tail 20 -Wait"
Write-Host "  Get-Content `$env:TEMP\ronnie.log -Tail 20 -Wait"
Write-Host "  Get-Content `$env:TEMP\admin-panel.log -Tail 20 -Wait"
Write-Host ""

Print-Info "Detener servicios:"
Write-Host "  docker compose down"
Write-Host "  Get-Process java | Stop-Process -Force"
Write-Host ""

Print-Success "¡Sistema iFit levantado exitosamente!"
