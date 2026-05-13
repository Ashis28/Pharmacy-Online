# ============================================================
# PharmaOnline - Start All Microservices
# ============================================================
# Usage: powershell -ExecutionPolicy Bypass -File start-all-services.ps1
#
# Prerequisites:
#   - MySQL running on port 3306 (password: yosker)
#   - RabbitMQ running on port 5672
#   - All JARs built (run: mvn package -DskipTests in each service)
# ============================================================

param(
    [string]$DbPass = "yosker",
    [string]$DbUser = "root",
    [string]$DbHost = "localhost"
)

$BASE = $PSScriptRoot
$JAVA_ARGS = @("-DDB_PASS=$DbPass", "-DDB_USER=$DbUser", "-DDB_HOST=$DbHost")

function Start-Svc($name, $jar, $port) {
    $running = netstat -ano 2>&1 | Select-String ":$port .*LISTENING"
    if ($running) {
        Write-Host "  [$name] Already running on port $port" -ForegroundColor Yellow
        return
    }
    $jarPath = Join-Path $BASE $jar
    if (-not (Test-Path $jarPath)) {
        Write-Host "  [$name] JAR not found: $jarPath" -ForegroundColor Red
        return
    }
    $logDir = Join-Path $BASE "$name\logs"
    New-Item -ItemType Directory -Path $logDir -Force | Out-Null
    $args = $JAVA_ARGS + @("-jar", $jarPath)
    Start-Process -FilePath "java" -ArgumentList $args -NoNewWindow `
        -RedirectStandardOutput "$logDir\startup.log" `
        -RedirectStandardError  "$logDir\startup-err.log"
    Write-Host "  [$name] Starting on port $port..." -ForegroundColor Cyan
}

Write-Host ""
Write-Host "╔══════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║   PharmaOnline Microservices Startup     ║" -ForegroundColor Green
Write-Host "╚══════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""

# Step 1: Core infrastructure (Eureka already running assumed)
Write-Host "► Starting core services..." -ForegroundColor White
Start-Svc "auth-service"         "auth-service\target\auth-service-1.0.0.jar"                 9091
Start-Svc "catalog-service"      "catalog-service\target\catalog-service-1.0.0.jar"           9092
Start-Svc "order-service"        "order-service\target\order-service-1.0.0.jar"               9093
Start-Svc "admin-service"        "admin-service\target\admin-service-1.0.0.jar"               9094
Start-Svc "payment-service"      "payment-service\target\payment-service-1.0.0.jar"           9095
Start-Svc "notification-service" "notification-service\target\notification-service-1.0.0.jar" 9096

Write-Host ""
Write-Host "  Waiting 40s for services to register with Eureka..." -ForegroundColor Gray
Start-Sleep -Seconds 40

# Step 2: Gateway (needs services registered first)
Write-Host "► Starting gateway..." -ForegroundColor White
Start-Svc "gateway-service" "gateway-service\target\gateway-service-1.0.0.jar" 8888

Write-Host ""
Write-Host "  Waiting 15s for gateway..." -ForegroundColor Gray
Start-Sleep -Seconds 15

# Status report
Write-Host ""
Write-Host "╔══════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║              Service Status              ║" -ForegroundColor Green
Write-Host "╚══════════════════════════════════════════╝" -ForegroundColor Green

$services = [ordered]@{
    8761 = "Eureka Server"
    9091 = "Auth Service"
    9092 = "Catalog Service"
    9093 = "Order Service"
    9094 = "Admin Service"
    9095 = "Payment Service"
    9096 = "Notification Service"
    8888 = "API Gateway"
}

$allUp = $true
foreach ($port in $services.Keys) {
    $r = netstat -ano 2>&1 | Select-String ":$port .*LISTENING"
    if ($r) {
        Write-Host "  ✅ Port $port - $($services[$port])" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Port $port - $($services[$port]) (not running)" -ForegroundColor Red
        $allUp = $false
    }
}

Write-Host ""
if ($allUp) {
    Write-Host "  🎉 All services running!" -ForegroundColor Green
} else {
    Write-Host "  ⚠️  Some services failed. Check logs in <service>\logs\startup.log" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "  API Gateway : http://localhost:8888" -ForegroundColor Cyan
Write-Host "  Eureka UI   : http://localhost:8761" -ForegroundColor Cyan
Write-Host "  Frontend    : http://localhost:4200  (run: cd pharma-frontend && npm start)" -ForegroundColor Cyan
Write-Host ""
