# ==============================================================================
# CSTU NEXUS - Tech Stack Verification Script for Windows (PowerShell)
# ==============================================================================
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  CSTU NEXUS - Tech Stack Verification" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

$allPassed = $true

# 1. Git
Write-Host "[1/4] Checking Git..." -NoNewline
try {
    $gitVer = git --version 2>$null
    if ($LASTEXITCODE -eq 0 -and $gitVer) {
        Write-Host " [PASS] ($gitVer)" -ForegroundColor Green
    } else {
        Write-Host " [FAIL]" -ForegroundColor Red
        Write-Host "      -> Please install Git from https://git-scm.com/downloads" -ForegroundColor Yellow
        $allPassed = $false
    }
} catch {
    Write-Host " [FAIL] Not found" -ForegroundColor Red
    $allPassed = $false
}

# 2. Java 17
Write-Host "[2/4] Checking Java JDK 17..." -NoNewline
try {
    $javaVerOutput = java -version 2>&1 | Out-String
    if ($javaVerOutput -match 'version "(17\.[0-9_]+)"') {
        $foundVer = $matches[1]
        Write-Host " [PASS] (Java version $foundVer)" -ForegroundColor Green
    } elseif ($javaVerOutput -match 'version "([0-9\._]+)"') {
        $foundVer = $matches[1]
        Write-Host " [WARNING] Found Java version $foundVer (Project requires Java 17)" -ForegroundColor Yellow
        Write-Host "      -> Download JDK 17: https://adoptium.net/temurin/releases/?version=17" -ForegroundColor Yellow
        $allPassed = $false
    } else {
        Write-Host " [FAIL]" -ForegroundColor Red
        Write-Host "      -> Java 17 not found. Please install JDK 17 and check JAVA_HOME." -ForegroundColor Yellow
        $allPassed = $false
    }
} catch {
    Write-Host " [FAIL] Not found" -ForegroundColor Red
    $allPassed = $false
}

# 3. Docker
Write-Host "[3/4] Checking Docker Desktop..." -NoNewline
try {
    $dockerVer = docker --version 2>$null
    if ($LASTEXITCODE -eq 0 -and $dockerVer) {
        Write-Host " [PASS] ($dockerVer)" -ForegroundColor Green
    } else {
        Write-Host " [FAIL]" -ForegroundColor Red
        Write-Host "      -> Please install Docker Desktop: https://www.docker.com/products/docker-desktop/" -ForegroundColor Yellow
        $allPassed = $false
    }
} catch {
    Write-Host " [FAIL] Not found" -ForegroundColor Red
    $allPassed = $false
}

# 4. Docker Compose
Write-Host "[4/4] Checking Docker Compose..." -NoNewline
try {
    $composeVer = docker compose version 2>$null
    if ($LASTEXITCODE -eq 0 -and $composeVer) {
        Write-Host " [PASS] ($composeVer)" -ForegroundColor Green
    } else {
        Write-Host " [FAIL]" -ForegroundColor Red
        Write-Host "      -> Docker Compose not found or Docker Desktop is not running." -ForegroundColor Yellow
        $allPassed = $false
    }
} catch {
    Write-Host " [FAIL] Not found" -ForegroundColor Red
    $allPassed = $false
}

# Check .env
Write-Host ""
Write-Host "------------------------------------------" -ForegroundColor DarkGray
Write-Host "Checking .env configuration..." -NoNewline
if (Test-Path ".env") {
    Write-Host " [FOUND]" -ForegroundColor Green
} else {
    Write-Host " [NOT FOUND]" -ForegroundColor Yellow
    Write-Host "  -> Don't forget to copy .env.example to .env and configure TU_API_APPLICATION_KEY" -ForegroundColor Yellow
}
Write-Host "------------------------------------------" -ForegroundColor DarkGray
Write-Host ""

if ($allPassed) {
    Write-Host "[SUCCESS] All required tools are ready for CSTU NEXUS!" -ForegroundColor Green
} else {
    Write-Host "[WARNING] Some tools are missing or mismatched. Please see REQUIREMENTS.md for setup instructions." -ForegroundColor Yellow
}
Write-Host ""