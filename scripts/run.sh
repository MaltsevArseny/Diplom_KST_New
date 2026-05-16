#!/usr/bin/env bash
# ============================================================
# DigitalHub — Сборка и запуск
# Платформа: Linux / macOS
# Требования: Java JDK 21+, Apache Maven 3.9+
# ============================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

echo "================================================"
echo "  DigitalHub — Сборка и запуск"
echo "================================================"
echo

# Проверяем Java
if ! command -v java &>/dev/null; then
    echo "[ОШИБКА] Java не найдена! Установите Java 21+:"
    echo "  Ubuntu/Debian : sudo apt install openjdk-21-jdk"
    echo "  macOS (brew)  : brew install openjdk@21"
    exit 1
fi

JAVA_VER=$(java -version 2>&1 | head -1 | sed 's/.*version "\([0-9]*\).*/\1/')
if [ "$JAVA_VER" -lt 21 ] 2>/dev/null; then
    echo "[ПРЕДУПРЕЖДЕНИЕ] Обнаружена Java $JAVA_VER. Рекомендуется Java 21+."
fi

# Проверяем Maven
if ! command -v mvn &>/dev/null; then
    echo "[ОШИБКА] Maven не найден! Добавьте mvn в PATH."
    echo "  Ubuntu/Debian : sudo apt install maven"
    echo "  macOS (brew)  : brew install maven"
    exit 1
fi

echo "[1/2] Сборка проекта (mvn clean package)..."
mvn clean package -q
echo "[OK] Сборка успешна."

echo "[2/2] Копирование JAR и запуск..."
mkdir -p dist
cp -f target/techhaven-1.0.0.jar dist/DigitalHub.jar

echo "[OK] Запуск приложения из dist/..."
# cd в dist/ — AppPaths определит dataDir как папку рядом с JAR (dist/),
# поэтому БД и product_images создадутся именно там, без -Ddb.path.
cd dist
java -Dfile.encoding=UTF-8 -jar DigitalHub.jar
