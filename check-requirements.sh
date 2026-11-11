#!/bin/bash
# ==============================================================================
# CSTU NEXUS - Tech Stack Verification Script for macOS / Linux / Git Bash
# ==============================================================================

echo "=========================================="
echo "  CSTU NEXUS - Tech Stack Verification"
echo "=========================================="
echo ""

ALL_PASSED=1

# 1. Git
echo -n "[1/4] Checking Git... "
if command -v git >/dev/null 2>&1; then
    echo "[PASS] ($(git --version))"
else
    echo "[FAIL] (Please install Git from https://git-scm.com/downloads)"
    ALL_PASSED=0
fi

# 2. Java 17
echo -n "[2/4] Checking Java JDK 17... "
if command -v java >/dev/null 2>&1; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    if [[ "$JAVA_VERSION" == 17* ]]; then
        echo "[PASS] (Java version $JAVA_VERSION)"
    else
        echo "[WARNING] Found Java version $JAVA_VERSION (Project requires Java 17)"
        echo "      -> Download JDK 17: https://adoptium.net/temurin/releases/?version=17"
        ALL_PASSED=0
    fi
else
    echo "[FAIL] (Java 17 not found. Please install JDK 17 and check JAVA_HOME)"
    ALL_PASSED=0
fi

# 3. Docker
echo -n "[3/4] Checking Docker Desktop... "
if command -v docker >/dev/null 2>&1; then
    echo "[PASS] ($(docker --version))"
else
    echo "[FAIL] (Please install Docker Desktop from https://www.docker.com/products/docker-desktop/)"
    ALL_PASSED=0
fi

# 4. Docker Compose
echo -n "[4/4] Checking Docker Compose... "
if docker compose version >/dev/null 2>&1; then
    echo "[PASS] ($(docker compose version))"
else
    echo "[FAIL] (Docker Compose not found or Docker is not running)"
    ALL_PASSED=0
fi

# Check .env
echo ""
echo "------------------------------------------"
echo -n "Checking .env configuration... "
if [ -f ".env" ]; then
    echo "[FOUND]"
else
    echo "[NOT FOUND] (Don't forget to copy .env.example to .env and configure TU_API_APPLICATION_KEY)"
fi
echo "------------------------------------------"
echo ""

if [ $ALL_PASSED -eq 1 ]; then
    echo "[SUCCESS] All required tools are ready for CSTU NEXUS!"
else
    echo "[WARNING] Some tools are missing or mismatched. Please see REQUIREMENTS.md for setup instructions."
fi
echo ""
